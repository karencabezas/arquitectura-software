import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule
  ],
  template: `
    <div class="login-wrapper">
      <mat-card class="login-card">
        <mat-card-header>
          <div class="logo-area">
            <mat-icon class="logo-icon">emergency</mat-icon>
            <h1>AeroRescue</h1>
            <p>Plataforma de coordinación de drones</p>
          </div>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Usuario</mat-label>
              <input matInput formControlName="username" autocomplete="username" />
              <mat-icon matPrefix>person</mat-icon>
              <mat-error *ngIf="form.get('username')?.hasError('required')">
                El usuario es requerido
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Contraseña</mat-label>
              <input matInput [type]="hidePassword() ? 'password' : 'text'"
                     formControlName="password" autocomplete="current-password" />
              <mat-icon matPrefix>lock</mat-icon>
              <button mat-icon-button matSuffix type="button"
                      (click)="hidePassword.set(!hidePassword())">
                <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="form.get('password')?.hasError('required')">
                La contraseña es requerida
              </mat-error>
            </mat-form-field>

            @if (errorMsg()) {
              <div class="error-banner">
                <mat-icon>error_outline</mat-icon>
                {{ errorMsg() }}
              </div>
            }

            <button mat-raised-button color="primary" type="submit"
                    class="full-width submit-btn"
                    [disabled]="form.invalid || loading()">
              @if (loading()) {
                <mat-spinner diameter="20" />
              } @else {
                Iniciar sesión
              }
            </button>
          </form>
        </mat-card-content>

        <mat-card-footer>
          <p class="hint">Usuarios demo: admin | operator.lima.norte | supervisor.lima</p>
          <p class="hint">Contraseña: <strong>Admin123!</strong></p>
        </mat-card-footer>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-wrapper {
      display: flex; align-items: center; justify-content: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #1a237e 0%, #0d47a1 50%, #01579b 100%);
    }
    .login-card { width: 420px; padding: 24px; border-radius: 16px; }
    .logo-area { text-align: center; width: 100%; padding: 16px 0; }
    .logo-icon { font-size: 56px; width: 56px; height: 56px; color: #1565c0; }
    h1 { margin: 8px 0 4px; font-size: 28px; font-weight: 700; color: #1a237e; }
    p { margin: 0; color: #546e7a; font-size: 14px; }
    .full-width { width: 100%; margin-bottom: 8px; }
    .submit-btn { margin-top: 16px; height: 48px; font-size: 16px; }
    .error-banner {
      display: flex; align-items: center; gap: 8px;
      background: #ffebee; color: #c62828; border-radius: 8px;
      padding: 10px 16px; margin-bottom: 12px; font-size: 14px;
    }
    .hint { font-size: 12px; color: #90a4ae; text-align: center; margin: 4px 0; }
    mat-card-footer { padding: 8px 0 0; }
  `]
})
export class LoginComponent {
  form = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  loading    = signal(false);
  errorMsg   = signal('');
  hidePassword = signal(true);

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {}

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorMsg.set('');

    const { username, password } = this.form.value;
    this.auth.login(username!, password!).subscribe({
      next: () => {
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMsg.set('Credenciales incorrectas. Intente de nuevo.');
      }
    });
  }
}
