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
import { DroneService } from '../../../core/auth/api.service';

@Component({
  selector: 'app-drone-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule, MatSnackBarModule
  ],
  template: `
    <div class="form-container">
      <h2><mat-icon>add_circle</mat-icon> Registrar Drone</h2>
      <mat-card>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Nombre / Código</mat-label>
              <input matInput formControlName="name" placeholder="DR-005" />
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Tipo de drone</mat-label>
              <mat-select formControlName="type">
                <mat-option value="RESCUE">🚁 Rescate</mat-option>
                <mat-option value="MEDICAL">🏥 Médico</mat-option>
                <mat-option value="SEARCH">🔍 Búsqueda</mat-option>
                <mat-option value="FIRE">🔥 Incendio</mat-option>
              </mat-select>
            </mat-form-field>

            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Latitud base</mat-label>
                <input matInput type="number" formControlName="lat" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Longitud base</mat-label>
                <input matInput type="number" formControlName="lng" />
              </mat-form-field>
            </div>

            @if (errorMsg()) {
              <div class="error-banner">
                <mat-icon>error_outline</mat-icon> {{ errorMsg() }}
              </div>
            }

            <div class="form-actions">
              <button mat-button type="button" (click)="router.navigate(['/drones'])">Cancelar</button>
              <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || loading()">
                <mat-icon>save</mat-icon> Registrar
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .form-container { max-width: 600px; }
    h2 { display: flex; align-items: center; gap: 8px; color: #1a237e; margin-bottom: 16px; }
    .full-width { width: 100%; margin-bottom: 8px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    mat-form-field { width: 100%; }
    .form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }
    .error-banner {
      display: flex; align-items: center; gap: 8px;
      background: #ffebee; color: #c62828; border-radius: 8px; padding: 10px 16px;
    }
  `]
})
export class DroneFormComponent {
  loading  = signal(false);
  errorMsg = signal('');

  form = this.fb.group({
    name: ['', Validators.required],
    type: ['RESCUE', Validators.required],
    lat:  [-12.0464, Validators.required],
    lng:  [-77.0428, Validators.required]
  });

  constructor(
    private fb: FormBuilder,
    private droneService: DroneService,
    public router: Router,
    private snack: MatSnackBar
  ) {}

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    const { name, type, lat, lng } = this.form.value;
    this.droneService.register(name!, type!, lat!, lng!).subscribe({
      next: () => {
        this.loading.set(false);
        this.snack.open('Drone registrado exitosamente', 'OK', { duration: 3000 });
        this.router.navigate(['/drones']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err.error?.message ?? 'Error al registrar el drone');
      }
    });
  }
}
