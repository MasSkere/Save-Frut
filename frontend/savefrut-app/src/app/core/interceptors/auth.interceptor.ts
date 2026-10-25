import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/** Con el backend expuesto por ngrok (plan gratis), evita que ngrok responda con su página de aviso en lugar de la API. */
const NGROK_HEADER = { 'ngrok-skip-browser-warning': 'true' };

const conToken = (req: HttpRequest<unknown>, token: string | null) =>
  req.clone({ setHeaders: token ? { ...NGROK_HEADER, Authorization: `Bearer ${token}` } : NGROK_HEADER });

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
