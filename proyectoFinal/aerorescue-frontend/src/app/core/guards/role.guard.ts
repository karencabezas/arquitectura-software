import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { UserRole } from '../../shared/models/models';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const allowedRoles: UserRole[] = route.data['roles'] ?? [];

  if (auth.hasRole(...allowedRoles)) return true;

  router.navigate(['/']);
  return false;
};
