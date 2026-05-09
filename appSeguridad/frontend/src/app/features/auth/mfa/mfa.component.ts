import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-mfa',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-brand">Auth<span>App</span></div>
        <div class="auth-subtitle">Verificación en dos pasos</div>

        <div style="text-align:center; margin-bottom:28px;">
          <div style="font-size:40px; margin-bottom:12px;">🔐</div>
          <p style="color:var(--text-muted); font-size:12px; line-height:1.8">
            Abre <strong style="color:var(--text)">Google Authenticator</strong><br>
            e ingresa el código de 6 dígitos
          </p>
        </div>

        @if (error()) {
          <div class="alert alert--error">{{ error() }}</div>
        }

        <div class="form-group">
          <label>Código TOTP</label>
          <input type="text" class="totp-input" [(ngModel)]="totpCode"
                 placeholder="000000" maxlength="6" inputmode="numeric"
                 (keyup.enter)="onValidate()" (ngModelChange)="onCodeChange($event)" />
        </div>

        <button class="btn btn--primary btn--full" (click)="onValidate()"
                [disabled]="loading() || totpCode.length !== 6">
          @if (loading()) { <span class="spinner"></span> Validando... }
          @else { Verificar código → }
        </button>

        <div class="text-center mt-16">
          <button class="btn btn--ghost btn--sm" (click)="goBack()">← Volver al login</button>
        </div>
      </div>
    </div>
  `
})
export class MfaComponent implements OnInit {
  totpCode = '';
  loading  = signal(false);
  error    = signal('');
  private preAuthToken = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    const token = sessionStorage.getItem('preAuthToken');
    if (!token) { this.router.navigate(['/login']); return; }
    this.preAuthToken = token;
  }

  onCodeChange(val: string): void {
    if (val.length === 6) this.onValidate();
  }

  onValidate(): void {
    if (this.totpCode.length !== 6) return;
    this.loading.set(true);
    this.error.set('');

    this.auth.validateMfa({ preAuthToken: this.preAuthToken, totpCode: this.totpCode }).subscribe({
      next: () => {
        sessionStorage.removeItem('preAuthToken');
        this.loading.set(false);
        this.router.navigate(['/productos']);
      },
      error: (err: { error?: { message?: string } }) => {
        this.loading.set(false);
        this.error.set(err.error?.message || 'Código inválido. Intenta de nuevo.');
        this.totpCode = '';
      }
    });
  }

  goBack(): void {
    sessionStorage.removeItem('preAuthToken');
    this.router.navigate(['/login']);
  }
}
