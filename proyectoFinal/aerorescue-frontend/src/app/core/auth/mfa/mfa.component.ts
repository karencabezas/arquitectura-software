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
import { WebSocketService } from '../../websocket/websocket.service';

@Component({
  selector: 'app-mfa',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule
  ],
  template: `
    <div class="mfa-wrapper">
      <mat-card class="mfa-card">
        <mat-card-header>
          <div class="header-area">
            <mat-icon class="mfa-icon">security</mat-icon>
            <h2>Verificación en dos pasos</h2>
            <p>Ingresa el código de 6 dígitos de Google Authenticator</p>
          </div>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onVerify()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Código TOTP</mat-label>
              <input matInput formControlName="code" type="text"
                     maxlength="6" placeholder="000000"
                     class="totp-input" autocomplete="one-time-code" />
              <mat-icon matPrefix>pin</mat-icon>
              <mat-hint>El código se renueva cada 30 segundos</mat-hint>
              <mat-error *ngIf="form.get('code')?.hasError('required')">
                El código es requerido
              </mat-error>
              <mat-error *ngIf="form.get('code')?.hasError('pattern')">
                Debe ser un código de 6 dígitos
              </mat-error>
            </mat-form-field>

            @if (errorMsg()) {
              <div class="error-banner">
                <mat-icon>error_outline</mat-icon>
                {{ errorMsg() }}
              </div>
            }

            <button mat-raised-button color="primary" type="submit"
                    class="full-width verify-btn" [disabled]="form.invalid || loading()">
              @if (loading()) {
                <mat-spinner diameter="20" />
              } @else {
                Verificar
              }
            </button>

            <button mat-button type="button" class="full-width back-btn"
                    (click)="goBack()">
              Volver al login
            </button>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .mfa-wrapper {
      display: flex; align-items: center; justify-content: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #1a237e 0%, #0d47a1 50%, #01579b 100%);
    }
    .mfa-card { width: 380px; padding: 24px; border-radius: 16px; }
    .header-area { text-align: center; width: 100%; padding: 16px 0; }
    .mfa-icon { font-size: 48px; width: 48px; height: 48px; color: #1565c0; }
    h2 { margin: 8px 0 4px; font-size: 22px; font-weight: 600; color: #1a237e; }
    p { margin: 0; color: #546e7a; font-size: 13px; }
    .full-width { width: 100%; }
    .totp-input { font-size: 24px; letter-spacing: 8px; text-align: center; }
    .verify-btn { margin-top: 16px; height: 48px; font-size: 16px; }
    .back-btn { margin-top: 8px; color: #546e7a; }
    .error-banner {
      display: flex; align-items: center; gap: 8px;
      background: #ffebee; color: #c62828; border-radius: 8px;
      padding: 10px 16px; margin: 12px 0; font-size: 14px;
    }
  `]
})
export class MfaComponent {
  form = this.fb.group({
    code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  loading  = signal(false);
  errorMsg = signal('');

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private ws: WebSocketService,
    private router: Router
  ) {}

  onVerify(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorMsg.set('');

    const code = parseInt(this.form.value.code!, 10);
    this.auth.verifyMfa(code).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.ws.connect(res.accessToken);
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.loading.set(false);
        this.errorMsg.set('Código incorrecto o expirado. Intente de nuevo.');
      }
    });
  }

  goBack(): void {
    this.auth.logout();
  }
}
