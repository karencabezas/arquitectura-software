import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LoginResponse } from '../../../core/models/models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-brand">Auth<span>App</span></div>
        <div class="auth-subtitle">Sistema de autenticación segura con MFA</div>

        @if (error()) {
          <div class="alert alert--error">{{ error() }}</div>
        }

        <div class="form-group">
          <label>Email</label>
          <input type="email" [(ngModel)]="email" placeholder="admin@authapp.com"
                 (keyup.enter)="onLogin()" />
        </div>

        <div class="form-group">
          <label>Contraseña</label>
          <input type="password" [(ngModel)]="password" placeholder="••••••••"
                 (keyup.enter)="onLogin()" />
        </div>

        <button class="btn btn--primary btn--full" (click)="onLogin()" [disabled]="loading()">
          @if (loading()) { <span class="spinner"></span> Verificando... }
          @else { Iniciar sesión → }
        </button>

        <div class="text-center mt-16">
          <span class="text-muted">¿Sin cuenta? </span>
          <a routerLink="/register" class="text-accent" style="text-decoration:none;font-size:12px">
            Crear cuenta
          </a>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  email    = '';
  password = '';
  loading  = signal(false);
  error    = signal('');

  constructor(private auth: AuthService, private router: Router) {}

  onLogin(): void {
    if (!this.email || !this.password) {
      this.error.set('Ingresa tu email y contraseña');
      return;
    }
    this.loading.set(true);
    this.error.set('');

    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: (res: LoginResponse) => {
        this.loading.set(false);
        sessionStorage.setItem('preAuthToken', res.preAuthToken);
        if (res.mfaRequired) {
          this.router.navigate(['/mfa']);
        } else {
          this.router.navigate(['/mfa-setup']);
        }
      },
      error: (err: { error?: { message?: string } }) => {
        this.loading.set(false);
        this.error.set(err.error?.message || 'Credenciales inválidas');
      }
    });
  }
}
