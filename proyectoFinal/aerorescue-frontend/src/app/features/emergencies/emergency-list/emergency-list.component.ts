import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EmergencyService } from '../../../core/auth/api.service';
import { WebSocketService } from '../../../core/websocket/websocket.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Emergency } from '../../../shared/models/models';

@Component({
  selector: 'app-emergency-list',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatTableModule, MatCardModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatTooltipModule, MatSnackBarModule
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h2><mat-icon>crisis_alert</mat-icon> Emergencias</h2>
        @if (auth.hasRole('ADMIN','SUPERVISOR','OPERATOR')) {
          <button mat-raised-button color="warn" routerLink="/emergencies/new">
            <mat-icon>add</mat-icon> Nueva emergencia
          </button>
        }
      </div>

      <mat-card>
        <mat-card-content>
          <table mat-table [dataSource]="emergencies()" class="full-table">

            <ng-container matColumnDef="priority">
              <th mat-header-cell *matHeaderCellDef>Prioridad</th>
              <td mat-cell *matCellDef="let e">
                <mat-chip [class]="'priority-' + e.priority.toLowerCase()">
                  {{ e.priority }}
                </mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="type">
              <th mat-header-cell *matHeaderCellDef>Tipo</th>
              <td mat-cell *matCellDef="let e">
                <mat-icon>{{ typeIcon(e.type) }}</mat-icon> {{ e.type }}
              </td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Estado</th>
              <td mat-cell *matCellDef="let e">
                <mat-chip [class]="'status-' + e.status.toLowerCase()">
                  {{ e.status }}
                </mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="region">
              <th mat-header-cell *matHeaderCellDef>Región</th>
              <td mat-cell *matCellDef="let e">{{ e.region }}</td>
            </ng-container>

            <ng-container matColumnDef="address">
              <th mat-header-cell *matHeaderCellDef>Dirección</th>
              <td mat-cell *matCellDef="let e">{{ e.address }}</td>
            </ng-container>

            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>Creada</th>
              <td mat-cell *matCellDef="let e">
                {{ e.createdAt | date:'dd/MM HH:mm' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Acciones</th>
              <td mat-cell *matCellDef="let e">
                @if (auth.hasRole('ADMIN','SUPERVISOR') && auth.clearanceLevel() >= 3) {
                  <button mat-icon-button color="warn"
                          matTooltip="Escalar prioridad"
                          (click)="escalate(e.id)">
                    <mat-icon>trending_up</mat-icon>
                  </button>
                }
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns;"
                [class.critical-row]="row.priority === 'CRITICAL'"></tr>
          </table>

          @if (emergencies().length === 0) {
            <div class="empty-state">
              <mat-icon>crisis_alert</mat-icon>
              <p>No hay emergencias registradas</p>
            </div>
          }
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; }
    .page-header {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: 20px;
    }
    h2 { display: flex; align-items: center; gap: 8px; font-size: 22px; color: #1a237e; margin: 0; }
    .full-table { width: 100%; }
    th { font-weight: 700; color: #37474f; }
    .critical-row { background: #fff8e1; }

    .priority-critical { background: #c62828 !important; color: white !important; }
    .priority-high     { background: #e65100 !important; color: white !important; }
    .priority-medium   { background: #f57f17 !important; color: white !important; }
    .priority-low      { background: #558b2f !important; color: white !important; }

    .status-pending    { background: #90a4ae !important; color: white !important; }
    .status-assigned   { background: #1565c0 !important; color: white !important; }
    .status-in_progress{ background: #2e7d32 !important; color: white !important; }
    .status-critical   { background: #c62828 !important; color: white !important; }
    .status-resolved   { background: #4caf50 !important; color: white !important; }
    .status-cancelled  { background: #9e9e9e !important; color: white !important; }

    .empty-state {
      text-align: center; padding: 40px; color: #90a4ae;
      display: flex; flex-direction: column; align-items: center; gap: 8px;
    }
    .empty-state mat-icon { font-size: 48px; width: 48px; height: 48px; }
  `]
})
export class EmergencyListComponent implements OnInit, OnDestroy {
  private subs: Subscription[] = [];
  emergencies = signal<Emergency[]>([]);
  columns = ['priority','type','status','region','address','createdAt','actions'];

  constructor(
    public auth: AuthService,
    private emergencyService: EmergencyService,
    private ws: WebSocketService,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.load();
    this.subs.push(
      this.ws.emergencyEvents$.subscribe(() => this.load())
    );
  }

  load(): void {
    this.emergencyService.getAll().subscribe(list => this.emergencies.set(list));
  }

  escalate(id: string): void {
    this.emergencyService.escalate(id).subscribe({
      next: () => this.snack.open('Emergencia escalada', 'OK', { duration: 3000 }),
      error: (err) => this.snack.open(err.error?.message ?? 'Sin permisos', 'OK', { duration: 4000 })
    });
  }

  typeIcon(type: string): string {
    const icons: Record<string,string> = {
      RESCUE: 'directions_run', MEDICAL: 'medical_services',
      SEARCH: 'search', FIRE: 'local_fire_department'
    };
    return icons[type] ?? 'help';
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }
}
