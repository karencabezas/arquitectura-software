import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { MfaSetupResponse } from '../../../core/models/models';

@Component({
  selector: 'app-mfa-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="auth-page">
      <div class="auth-card" style="width:480px">
        <div class="auth-brand">Auth<span>App</span></div>
        <div class="auth-subtitle">Configurar autenticación en dos pasos</div>

        @if (step() === 'loading') {
          <div style="text-align:center;padding:40px 0;color:var(--text-muted)">
            <span class="spinner" style="border-color:rgba(0,229,160,0.3);border-top-color:var(--accent);width:24px;height:24px"></span>
            <p style="margin-top:12px;font-size:12px">Generando código QR...</p>
          </div>
        }

        @if (step() === 'scan') {
          <div style="text-align:center">
            <div style="display:flex;align-items:center;gap:10px;margin-bottom:20px;text-align:left">
              <div style="width:24px;height:24px;border-radius:50%;background:var(--accent);color:#000;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;flex-shrink:0">1</div>
              <p style="font-size:12px;color:var(--text-muted)">Descarga <strong style="color:var(--text)">Google Authenticator</strong> si aún no lo tienes.</p>
            </div>
            <div style="display:flex;align-items:center;gap:10px;margin-bottom:24px;text-align:left">
              <div style="width:24px;height:24px;border-radius:50%;background:var(--accent);color:#000;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;flex-shrink:0">2</div>
              <p style="font-size:12px;color:var(--text-muted)">Escanea el código QR con la app.</p>
            </div>

            <div style="background:white;padding:16px;border-radius:8px;display:inline-block;margin-bottom:20px">
              <img [src]="qrImageUri()" alt="QR Code" style="width:180px;height:180px;display:block" />
            </div>

            <div style="background:var(--bg-input);border:1px solid var(--border);border-radius:6px;padding:10px;margin-bottom:24px">
              <p style="font-size:10px;color:var(--text-muted);margin-bottom:4px">Clave manual:</p>
              <code style="font-size:13px;color:var(--accent);letter-spacing:2px">{{ secret() }}</code>
            </div>

            <div style="display:flex;align-items:center;gap:10px;margin-bottom:20px;text-align:left">
              <div style="width:24px;height:24px;border-radius:50%;background:var(--accent);color:#000;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;flex-shrink:0">3</div>
              <p style="font-size:12px;color:var(--text-muted)">Ingresa el código de 6 dígitos para confirmar.</p>
            </div>

            @if (error()) { <div class="alert alert--error">{{ error() }}</div> }

            <div class="form-group">
              <label>Código de confirmación</label>
              <input type="text" class="totp-input" [(ngModel)]="confirmCode"
                     placeholder="000000" maxlength="6" inputmode="numeric"
                     (keyup.enter)="onConfirm()" />
            </div>

            <button class="btn btn--primary btn--full" (click)="onConfirm()"
                    [disabled]="loadingConfirm() || confirmCode.length !== 6">
              @if (loadingConfirm()) { <span class="spinner"></span> Confirmando... }
              @else { Activar MFA → }
            </button>
          </div>
        }

        @if (step() === 'done') {
          <div style="text-align:center;padding:20px 0">
            <div style="font-size:48px;margin-bottom:16px">✅</div>
            <h3 style="font-family:var(--font-display);font-size:20px;margin-bottom:8px">¡MFA Activado!</h3>
            <p style="color:var(--text-muted);font-size:12px;margin-bottom:24px">
              En cada inicio de sesión se te pedirá el código.
            </p>
            <button class="btn btn--primary" (click)="continue()">Ir a productos →</button>
          </div>
        }
      </div>
    </div>
  `
})
export class MfaSetupComponent implements OnInit {
  step           = signal<'loading' | 'scan' | 'done'>('loading');
  qrImageUri     = signal('');
  secret         = signal('');
  error          = signal('');
  confirmCode    = '';
  loadingConfirm = signal(false);
  private preAuthToken = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    const token = sessionStorage.getItem('preAuthToken');
    if (!token) { this.router.navigate(['/login']); return; }
    this.preAuthToken = token;

    this.auth.setupMfa(token).subscribe({
      next: (res: MfaSetupResponse) => {
        this.qrImageUri.set(res.qrImageUri);
        this.secret.set(res.secret);
        this.step.set('scan');
      },
      error: () => this.router.navigate(['/login'])
    });
  }

  onConfirm(): void {
    if (this.confirmCode.length !== 6) return;
    this.loadingConfirm.set(true);
    this.error.set('');

    this.auth.confirmMfaSetup(this.preAuthToken, this.confirmCode).subscribe({
      next: () => {
        sessionStorage.removeItem('preAuthToken');
        this.loadingConfirm.set(false);
        this.step.set('done');
      },
      error: (err: { error?: { message?: string } }) => {
        this.loadingConfirm.set(false);
        this.error.set(err.error?.message || 'Código incorrecto. Intenta de nuevo.');
        this.confirmCode = '';
      }
    });
  }

  continue(): void { this.router.navigate(['/productos']); }
}
