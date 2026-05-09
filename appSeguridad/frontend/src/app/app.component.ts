import { Component, computed } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    @if (auth.isLoggedIn()) {
      <div class="app-shell">
        <!-- Topbar -->
        <header class="topbar">
          <div class="topbar__brand">Auth<span>App</span></div>
          <div class="topbar__user">
            <span>{{ auth.currentUser()?.email }}</span>
            @for (role of auth.currentUser()?.roles; track role) {
              <span class="badge">{{ role }}</span>
            }
            <button class="btn-logout" (click)="auth.logout()">Salir</button>
          </div>
        </header>

        <!-- Sidebar -->
        <nav class="sidebar">
          <div class="sidebar__section">
            <div class="sidebar__label">General</div>
            <a class="nav-item" routerLink="/productos" routerLinkActive="active">
              <span class="nav-icon">⬡</span> Productos
            </a>
          </div>

          @if (auth.isAdmin()) {
            <div class="sidebar__section">
              <div class="sidebar__label">Administración</div>
              <a class="nav-item" routerLink="/admin/roles" routerLinkActive="active">
                <span class="nav-icon">◈</span> Roles
              </a>
              <a class="nav-item" routerLink="/admin/permisos" routerLinkActive="active">
                <span class="nav-icon">◇</span> Permisos
              </a>
              <a class="nav-item" routerLink="/admin/usuarios" routerLinkActive="active">
                <span class="nav-icon">◉</span> Usuarios
              </a>
            </div>
          }
        </nav>

        <!-- Main -->
        <main class="main-content">
          <router-outlet />
        </main>
      </div>
    } @else {
      <router-outlet />
    }
  `
})
export class AppComponent {
  constructor(public auth: AuthService) {}
}
