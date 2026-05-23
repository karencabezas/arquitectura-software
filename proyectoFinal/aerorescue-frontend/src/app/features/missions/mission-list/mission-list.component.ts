import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MissionService } from '../../../core/auth/api.service';
import { WebSocketService } from '../../../core/websocket/websocket.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Mission, MissionOutcome } from '../../../shared/models/models';

@Component({
  selector: 'app-mission-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatCardModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatTooltipModule,
    MatSnackBarModule
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h2><mat-icon>task_alt</mat-icon> Misiones</h2>
        <div class="header-badges">
          @if (unattended().length > 0) {
            <div class="unattended-badge">
              <mat-icon>warning</mat-icon>
              {{ unattended().length }} sin cobertura
            </div>
          }
          @if (awaiting().length > 0) {
            <div class="awaiting-badge">
              <mat-icon>hourglass_empty</mat-icon>
              {{ awaiting().length }} buscando reemplazo
            </div>
          }
        </div>
      </div>

      <mat-card>
        <mat-card-content>
          <table mat-table [dataSource]="missions()" class="full-table">

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Estado</th>
              <td mat-cell *matCellDef="let m">
                <mat-chip [class]="'status-' + m.status.toLowerCase()">{{ m.status }}</mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="priority">
              <th mat-header-cell *matHeaderCellDef>Prioridad</th>
              <td mat-cell *matCellDef="let m">
                <mat-chip [class]="'priority-' + (m.priority || 'medium').toLowerCase()">{{ m.priority }}</mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="missionType">
              <th mat-header-cell *matHeaderCellDef>Tipo</th>
              <td mat-cell *matCellDef="let m">
                <mat-icon>{{ typeIcon(m.missionType) }}</mat-icon> {{ m.missionType }}
              </td>
            </ng-container>

            <ng-container matColumnDef="emergencyId">
              <th mat-header-cell *matHeaderCellDef>Emergencia</th>
              <td mat-cell *matCellDef="let m">
                <code>{{ m.emergencyId | slice:0:8 }}...</code>
              </td>
            </ng-container>

            <ng-container matColumnDef="droneId">
              <th mat-header-cell *matHeaderCellDef>Drone</th>
              <td mat-cell *matCellDef="let m">
                @if (m.droneId) {
                  <span class="drone-id"><mat-icon>flight</mat-icon>{{ m.droneId | slice:0:8 }}...</span>
                } @else {
                  <span class="no-drone">Sin drone</span>
                }
              </td>
            </ng-container>

            <ng-container matColumnDef="outcome">
              <th mat-header-cell *matHeaderCellDef>Resultado</th>
              <td mat-cell *matCellDef="let m">
                @if (m.outcome) {
                  <mat-chip [class]="'outcome-' + m.outcome.toLowerCase()">{{ m.outcome }}</mat-chip>
                }
              </td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Acciones</th>
              <td mat-cell *matCellDef="let m">
                <div class="action-buttons">
                  @if (canReassign(m)) {
                    <button mat-icon-button color="accent" matTooltip="Reasignar" (click)="reassign(m.id)">
                      <mat-icon>sync</mat-icon>
                    </button>
                  }
                  @if (canClose(m)) {
                    <button mat-icon-button color="primary" matTooltip="Cerrar como éxito" (click)="close(m.id, 'SUCCESS')">
                      <mat-icon>check_circle</mat-icon>
                    </button>
                    <button mat-icon-button matTooltip="Resuelto sin drone" (click)="close(m.id, 'RESOLVED_WITHOUT_DRONE')">
                      <mat-icon>cancel</mat-icon>
                    </button>
                  }
                </div>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns;"
                [class.unattended-row]="row.status === 'UNATTENDED'"
                [class.awaiting-row]="row.status === 'AWAITING_REPLACEMENT'">
            </tr>
          </table>

          @if (missions().length === 0) {
            <div class="empty-state">
              <mat-icon>task_alt</mat-icon>
              <p>No hay misiones registradas</p>
            </div>
          }
        </mat-card-content>
      </mat-card>

      @if (unattended().length > 0 && auth.clearanceLevel() >= 2) {
        <mat-card class="unattended-panel">
          <mat-card-header>
            <mat-card-title>
              <mat-icon color="warn">warning</mat-icon>
              Misiones sin cobertura — acción requerida
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            @for (m of unattended(); track m.id) {
              <div class="unattended-item">
                <div><strong>Tipo:</strong> {{ m.missionType }} | <strong>Prioridad:</strong> {{ m.priority }}</div>
                <div class="unattended-actions">
                  <button mat-stroked-button color="primary" (click)="close(m.id, 'RESOLVED_WITHOUT_DRONE')">
                    Resuelto sin drone
                  </button>
                  <button mat-raised-button color="warn" (click)="close(m.id, 'TIMEOUT')">
                    Cerrar como TIMEOUT
                  </button>
                </div>
              </div>
            }
          </mat-card-content>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h2 { display: flex; align-items: center; gap: 8px; font-size: 22px; color: #1a237e; margin: 0; }
    .header-badges { display: flex; gap: 12px; }
    .unattended-badge, .awaiting-badge {
      display: flex; align-items: center; gap: 6px;
      padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 600;
    }
    .unattended-badge { background: #ffebee; color: #c62828; }
    .awaiting-badge { background: #fff3e0; color: #e65100; }
    .full-table { width: 100%; }
    th { font-weight: 700; color: #37474f; }
    .unattended-row { background: #fff8e1; }
    .awaiting-row { background: #e3f2fd; }
    .action-buttons { display: flex; gap: 4px; }
    .drone-id { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #1565c0; }
    .drone-id mat-icon { font-size: 16px; width: 16px; height: 16px; }
    .no-drone { color: #c62828; font-size: 12px; font-style: italic; }
    code { background: #f5f5f5; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
    .status-pending              { background: #90a4ae !important; color: white !important; }
    .status-assigned             { background: #1565c0 !important; color: white !important; }
    .status-in_progress          { background: #2e7d32 !important; color: white !important; }
    .status-awaiting_replacement { background: #e65100 !important; color: white !important; }
    .status-reassigned           { background: #6a1b9a !important; color: white !important; }
    .status-unattended           { background: #c62828 !important; color: white !important; }
    .status-completed            { background: #4caf50 !important; color: white !important; }
    .status-failed               { background: #37474f !important; color: white !important; }
    .priority-critical { background: #c62828 !important; color: white !important; }
    .priority-high     { background: #e65100 !important; color: white !important; }
    .priority-medium   { background: #f57f17 !important; color: white !important; }
    .priority-low      { background: #558b2f !important; color: white !important; }
    .outcome-success                { background: #2e7d32 !important; color: white !important; }
    .outcome-resolved_without_drone { background: #1565c0 !important; color: white !important; }
    .outcome-timeout                { background: #e65100 !important; color: white !important; }
    .outcome-drone_failure          { background: #c62828 !important; color: white !important; }
    .unattended-panel { margin-top: 20px; border: 2px solid #c62828 !important; border-radius: 12px !important; }
    .unattended-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
    .unattended-actions { display: flex; gap: 8px; }
    .empty-state { text-align: center; padding: 40px; color: #90a4ae; display: flex; flex-direction: column; align-items: center; gap: 8px; }
    .empty-state mat-icon { font-size: 48px; width: 48px; height: 48px; }
  `]
})
export class MissionListComponent implements OnInit, OnDestroy {
  private subs: Subscription[] = [];
  missions = signal<Mission[]>([]);
  columns = ['status','priority','missionType','emergencyId','droneId','outcome','actions'];

  unattended = () => this.missions().filter(m => m.status === 'UNATTENDED');
  awaiting   = () => this.missions().filter(m => m.status === 'AWAITING_REPLACEMENT');

  canReassign = (m: Mission) =>
    ['IN_PROGRESS','AWAITING_REPLACEMENT','ASSIGNED'].includes(m.status)
    && this.auth.clearanceLevel() >= 3;

  canClose = (m: Mission) =>
    !['COMPLETED','FAILED'].includes(m.status)
    && this.auth.clearanceLevel() >= 2;

  constructor(
    public auth: AuthService,
    private missionService: MissionService,
    private ws: WebSocketService,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.load();
    this.subs.push(this.ws.missionEvents$.subscribe(() => this.load()));
  }

  load(): void {
    this.missionService.getAll().subscribe(list => this.missions.set(list));
  }

  reassign(id: string): void {
    this.missionService.reassign(id, 'MANUAL').subscribe({
      next: () => this.snack.open('Reasignación iniciada', 'OK', { duration: 3000 }),
      error: (err) => this.snack.open(err.error?.message ?? 'Sin permisos suficientes', 'OK', { duration: 4000 })
    });
  }

  close(id: string, outcome: MissionOutcome): void {
    this.missionService.close(id, outcome).subscribe({
      next: () => this.snack.open(`Misión cerrada: ${outcome}`, 'OK', { duration: 3000 }),
      error: (err) => this.snack.open(err.error?.message ?? 'Error al cerrar misión', 'OK', { duration: 4000 })
    });
  }

  typeIcon(type: string): string {
    const m: Record<string,string> = {
      RESCUE: 'directions_run', MEDICAL: 'medical_services',
      SEARCH: 'search', FIRE: 'local_fire_department'
    };
    return m[type] ?? 'help';
  }

  ngOnDestroy(): void { this.subs.forEach(s => s.unsubscribe()); }
}
