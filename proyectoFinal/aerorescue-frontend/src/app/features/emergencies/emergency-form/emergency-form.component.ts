import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmergencyService } from '../../../core/auth/api.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-emergency-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule,
    MatSnackBarModule, MatProgressSpinnerModule
  ],
  template: `
    <div class="form-container">
      <h2><mat-icon>add_circle</mat-icon> Nueva Emergencia</h2>

      @if (auth.role() === 'OPERATOR') {
        <div class="abac-info">
          <mat-icon>info</mat-icon>
          <span>Como Operador, solo puedes crear emergencias en tu región:
            <strong>{{ auth.region() }}</strong></span>
        </div>
      }

      <mat-card>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Prioridad</mat-label>
                <mat-select formControlName="priority">
                  <mat-option value="LOW">Baja</mat-option>
                  <mat-option value="MEDIUM">Media</mat-option>
                  <mat-option value="HIGH">Alta</mat-option>
                  @if (auth.clearanceLevel() >= 3) {
                    <mat-option value="CRITICAL">Crítica (requiere clearance ≥ 3)</mat-option>
                  }
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Tipo</mat-label>
                <mat-select formControlName="type">
                  @for (t of availableTypes(); track t.value) {
                    <mat-option [value]="t.value">{{ t.label }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Región</mat-label>
                <mat-select formControlName="region">
                  @for (r of regions; track r) {
                    <mat-option [value]="r"
                      [disabled]="auth.role() === 'OPERATOR' && r !== auth.region()">
                      {{ r }}
                    </mat-option>
                  }
                </mat-select>
                @if (auth.role() === 'OPERATOR') {
                  <mat-hint>Solo tu región está disponible (ABAC-01)</mat-hint>
                }
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Dirección</mat-label>
                <input matInput formControlName="address" />
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Latitud</mat-label>
                <input matInput type="number" formControlName="lat" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Longitud</mat-label>
                <input matInput type="number" formControlName="lng" />
              </mat-form-field>
            </div>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Descripción</mat-label>
              <textarea matInput formControlName="description" rows="3"></textarea>
            </mat-form-field>

            @if (errorMsg()) {
              <div class="error-banner">
                <mat-icon>error_outline</mat-icon>
                {{ errorMsg() }}
              </div>
            }

            <div class="form-actions">
              <button mat-button type="button" (click)="router.navigate(['/emergencies'])">
                Cancelar
              </button>
              <button mat-raised-button color="warn" type="submit"
                      [disabled]="form.invalid || loading()">
                @if (loading()) {
                  <mat-spinner diameter="20" />
                } @else {
                  <ng-container>
                    <mat-icon>save</mat-icon> Registrar emergencia
                  </ng-container>
                }
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .form-container { max-width: 800px; }
    h2 { display: flex; align-items: center; gap: 8px; color: #1a237e; margin-bottom: 16px; }
    .abac-info {
      display: flex; align-items: center; gap: 8px;
      background: #e3f2fd; color: #1565c0; border-radius: 8px;
      padding: 10px 16px; margin-bottom: 16px; font-size: 14px;
    }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 8px; }
    .full-width { width: 100%; }
    mat-form-field { width: 100%; }
    .form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }
    .error-banner {
      display: flex; align-items: center; gap: 8px;
      background: #ffebee; color: #c62828; border-radius: 8px;
      padding: 10px 16px; margin-bottom: 12px;
    }
  `]
})
export class EmergencyFormComponent {
  loading  = signal(false);
  errorMsg = signal('');

  regions = ['LIMA_NORTE','LIMA_SUR','LIMA_ESTE','LIMA_CENTRO','CALLAO'];

  availableTypes = () => {
    const all = [
      { value: 'RESCUE', label: '🚁 Rescate' },
      { value: 'MEDICAL', label: '🏥 Médica' },
      { value: 'SEARCH', label: '🔍 Búsqueda' },
      { value: 'FIRE', label: '🔥 Incendio' }
    ];
    const allowed = this.auth.user()?.allowed_mission_types ?? [];
    return all.filter(t => allowed.includes(t.value));
  };

  form = this.fb.group({
    priority:    ['HIGH', Validators.required],
    type:        ['RESCUE', Validators.required],
    address:     ['', Validators.required],
    region:      [this.auth.region() ?? 'LIMA_NORTE', Validators.required],
    lat:         [-12.0464, Validators.required],
    lng:         [-77.0428, Validators.required],
    description: ['']
  });

  constructor(
    private fb: FormBuilder,
    public auth: AuthService,
    private emergencyService: EmergencyService,
    public router: Router,
    private snack: MatSnackBar
  ) {}

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorMsg.set('');

    this.emergencyService.create(this.form.value as any).subscribe({
      next: () => {
        this.loading.set(false);
        this.snack.open('Emergencia registrada exitosamente', 'OK', { duration: 3000 });
        this.router.navigate(['/emergencies']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err.error?.message ?? 'Error al registrar la emergencia');
      }
    });
  }
}
