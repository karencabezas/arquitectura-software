import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./core/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'mfa',
    loadComponent: () => import('./core/auth/mfa/mfa.component').then(m => m.MfaComponent)
  },
  {
    path: '',
    loadComponent: () => import('./layouts/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'emergencies',
        loadComponent: () => import('./features/emergencies/emergency-list/emergency-list.component').then(m => m.EmergencyListComponent)
      },
      {
        path: 'emergencies/new',
        loadComponent: () => import('./features/emergencies/emergency-form/emergency-form.component').then(m => m.EmergencyFormComponent),
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'SUPERVISOR', 'OPERATOR'] }
      },
      {
        path: 'drones',
        loadComponent: () => import('./features/drones/drone-list/drone-list.component').then(m => m.DroneListComponent)
      },
      {
        path: 'drones/new',
        loadComponent: () => import('./features/drones/drone-form/drone-form.component').then(m => m.DroneFormComponent),
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'DRONE_TECH'] }
      },
      {
        path: 'missions',
        loadComponent: () => import('./features/missions/mission-list/mission-list.component').then(m => m.MissionListComponent)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
