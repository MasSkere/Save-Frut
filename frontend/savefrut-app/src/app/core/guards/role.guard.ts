import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../../shared/models/auth.model';

export const roleGuard = (allowedRoles: UserRole[]): CanActivateFn => (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }
  if (authService.hasRole(allowedRoles)) {
    return true;
  }
  return router.createUrlTree([authService.rutaInicio()]);
};

export const adminGuard: CanActivateFn = roleGuard(['ADMIN']);
export const vendedorGuard: CanActivateFn = roleGuard(['VENDEDOR']);
export const clienteGuard: CanActivateFn = roleGuard(['CLIENTE']);
