import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

const conToken = (req: HttpRequest<unknown>, token: string | null) =>
  token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

/**
 * Agrega el JWT a cada petición. Si el access token expiró (401), intenta una vez
 * renovarlo con el refresh token (cookie HttpOnly) y repite la petición original.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const esAuth = req.url.includes('/auth/');

  return next(conToken(req, authService.accessToken())).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || esAuth || !authService.isAuthenticated()) {
        return throwError(() => error);
      }
      return authService.refresh().pipe(
        switchMap(nuevoToken => next(conToken(req, nuevoToken))),
        catchError(refreshError => {
          authService.limpiarSesion();
          router.navigate(['/login'], { queryParams: { expirada: 1 } });
          return throwError(() => refreshError);
        })
      );
    })
  );
};
