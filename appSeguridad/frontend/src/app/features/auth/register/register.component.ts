import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-brand">Auth<span>App</span></div>
        <div class="auth-subtitle">Crear nueva cuenta</div>

        @if (success()) {
          <div class="alert alert--success">
            ✓ Cuenta creada. Redirigiendo al login...
          </div>
        }

        @if (error()) {
          <div class="alert alert--error">{{ error() }}</div>
        }

        <div class="form-group">
          <label>Email</label>
          <input type="email" [(ngModel)]="email"
                 placeholder="tu@email.com"
                 (keyup.enter)="onRegister()"
                 [disabled]="loading() || success()" />
        </div>

        <div class="form-group">
          <label>Contraseña</label>
          <input type="password" [(ngModel)]="password"
                 placeholder="Mínimo 8 caracteres"
                 (keyup.enter)="onRegister()"
                 [disabled]="loading() || success()" />
        </div>

        <div class="form-group">
          <label>Confirmar contraseña</label>
          <input type="password" [(ngModel)]="confirmPassword"
                 placeholder="Repite la contraseña"
                 (keyup.enter)="onRegister()"
                 [disabled]="loading() || success()" />
        </div>

        <button class="btn btn--primary btn--full"
                (click)="onRegister()"
                [disabled]="loading() || success()">
          @if (loading()) { <span class="spinner"></span> Creando cuenta... }
          @else { Crear cuenta → }
        </button>

        <div class="text-center mt-16">
          <span class="text-muted">¿Ya tienes cuenta? </span>
          <a routerLink="/login" class="text-accent" style="text-decoration:none;font-size:12px">
            Iniciar sesión
          </a>
        </div>
      </div>
    </div>
  `
})
export class RegisterComponent {
  email           = '';
  password        = '';
  confirmPassword = '';
  loading         = signal(false);
  error           = signal('');
  success         = signal(false);

  constructor(private auth: AuthService, private router: Router) {}

  onRegister(): void {
    this.error.set('');

    if (!this.email || !this.password || !this.confirmPassword) {
      this.error.set('Todos los campos son obligatorios');
      return;
    }

    if (!this.email.includes('@')) {
      this.error.set('Ingresa un email válido');
      return;
    }

    if (this.password.length < 8) {
      this.error.set('La contraseña debe tener al menos 8 caracteres');
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.error.set('Las contraseñas no coinciden');
      return;
    }

    this.loading.set(true);

    this.auth.register(this.email, this.password).subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set(true);
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err: { error?: { message?: string } }) => {
        this.loading.set(false);
        this.error.set(err.error?.message || 'Error al crear la cuenta');
      }
    });
  }
}
