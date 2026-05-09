import { Routes } from '@angular/router';
import { authGuard, adminGuard, publicGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'productos', pathMatch: 'full' },

  // ── Auth (público) ──
  {
    path: 'login',
    canActivate: [publicGuard],
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [publicGuard],
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'mfa',
    loadComponent: () => import('./features/auth/mfa/mfa.component').then(m => m.MfaComponent)
  },
  {
    path: 'mfa-setup',
    loadComponent: () => import('./features/auth/mfa-setup/mfa-setup.component').then(m => m.MfaSetupComponent)
  },

  // ── Protegidas ──
  {
    path: 'productos',
    canActivate: [authGuard],
    loadComponent: () => import('./features/products/products.component').then(m => m.ProductsComponent)
  },

  // ── Solo ADMIN ──
  {
    path: 'admin/roles',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/roles/roles.component').then(m => m.RolesComponent)
  },
  {
    path: 'admin/usuarios',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/users/users.component').then(m => m.UsersComponent)
  },
  {
    path: 'admin/permisos',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/permissions/permissions.component').then(m => m.PermissionsComponent)
  },

  { path: '**', redirectTo: 'productos' }
];
