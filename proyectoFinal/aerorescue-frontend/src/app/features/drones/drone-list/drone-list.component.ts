import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DroneService } from '../../../core/auth/api.service';
import { WebSocketService } from '../../../core/websocket/websocket.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Drone, TelemetryRequest } from '../../../shared/models/models';

@Component({
  selector: 'app-drone-list',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatTableModule, MatCardModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatProgressBarModule,
    MatTooltipModule, MatDialogModule, MatSnackBarModule
  ],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h2><mat-icon>flight</mat-icon> Flota de drones</h2>
        @if (auth.hasRole('ADMIN','DRONE_TECH')) {
          <button mat-raised-button color="primary" routerLink="/drones/new">
            <mat-icon>add</mat-icon> Registrar drone
          </button>
        }
      </div>

      <div class="drones-grid">
        @for (drone of drones(); track drone.id) {
          <mat-card class="drone-card" [class.offline]="drone.status === 'OFFLINE'">
            <mat-card-header>
              <mat-icon mat-card-avatar [class]="'status-icon-' + drone.status.toLowerCase()">
                {{ droneIcon(drone.status) }}
              </mat-icon>
              <mat-card-title>{{ drone.name }}</mat-card-title>
              <mat-card-subtitle>
                <mat-chip [class]="'type-' + drone.type.toLowerCase()">{{ drone.type }}</mat-chip>
              </mat-card-subtitle>
            </mat-card-header>

            <mat-card-content>
              <div class="battery-section">
                <div class="battery-label">
                  <mat-icon>battery_{{ batteryIcon(drone.batteryPercentage) }}</mat-icon>
                  <span>{{ drone.batteryPercentage }}%</span>
                  @if (drone.batteryStatus !== 'NORMAL') {
                    <mat-chip class="battery-alert" [class]="'bat-' + drone.batteryStatus.toLowerCase()">
                      {{ drone.batteryStatus }}
                    </mat-chip>
                  }
                </div>
                <mat-progress-bar mode="determinate"
                  [value]="drone.batteryPercentage"
                  [color]="batteryColor(drone.batteryPercentage)" />
              </div>

              <div class="info-row">
                <mat-icon>location_on</mat-icon>
                <span>{{ drone.lat | number:'1.4-4' }}, {{ drone.lng | number:'1.4-4' }}</span>
              </div>

              @if (drone.missionId) {
                <div class="info-row mission">
                  <mat-icon>task_alt</mat-icon>
                  <span>Misión activa</span>
                </div>
              }

              <div class="info-row">
                <mat-icon>schedule</mat-icon>
                <span>{{ drone.lastTelemetryAt | date:'HH:mm:ss' }}</span>
              </div>
            </mat-card-content>

            <mat-card-actions>
              <mat-chip [class]="'status-' + drone.status.toLowerCase()">
                {{ drone.status }}
              </mat-chip>

              <!-- Demo: simular telemetría -->
              @if (auth.hasRole('ADMIN','DRONE_TECH')) {
                <button mat-icon-button matTooltip="Simular telemetría (demo)"
                        (click)="simulateTelemetry(drone)">
                  <mat-icon>send</mat-icon>
                </button>
              }
            </mat-card-actions>
          </mat-card>
        }
      </div>

      @if (drones().length === 0) {
        <div class="empty-state">
          <mat-icon>flight_off</mat-icon>
          <p>No hay drones registrados</p>
        </div>
      }
    </div>
  `,
  styles: [`
    .page-container { max-width: 1400px; }
    .page-header {
      display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
    }
    h2 { display: flex; align-items: center; gap: 8px; font-size: 22px; color: #1a237e; margin: 0; }

    .drones-grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;
    }
    .drone-card { border-radius: 12px !important; transition: box-shadow 0.2s; }
    .drone-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.15) !important; }
    .drone-card.offline { opacity: 0.7; border: 2px solid #c62828; }

    .status-icon-available   { color: #2e7d32; font-size: 36px; width: 36px; height: 36px; }
    .status-icon-on_mission  { color: #1565c0; font-size: 36px; width: 36px; height: 36px; }
    .status-icon-returning   { color: #e65100; font-size: 36px; width: 36px; height: 36px; }
    .status-icon-offline     { color: #c62828; font-size: 36px; width: 36px; height: 36px; }
    .status-icon-maintenance { color: #78909c; font-size: 36px; width: 36px; height: 36px; }
    .status-icon-inactive    { color: #bdbdbd; font-size: 36px; width: 36px; height: 36px; }

    .battery-section { margin: 8px 0; }
    .battery-label {
      display: flex; align-items: center; gap: 6px; margin-bottom: 4px; font-size: 14px;
    }
    .battery-alert { font-size: 10px; height: 18px; }
    .bat-low      { background: #e65100 !important; color: white !important; }
    .bat-critical { background: #c62828 !important; color: white !important; }

    .info-row {
      display: flex; align-items: center; gap: 6px;
      font-size: 12px; color: #546e7a; margin-top: 4px;
    }
    .info-row mat-icon { font-size: 16px; width: 16px; height: 16px; }
    .info-row.mission { color: #1565c0; font-weight: 600; }

    mat-card-actions { padding: 8px 16px; display: flex; align-items: center; gap: 8px; }
    .status-available   { background: #2e7d32 !important; color: white !important; }
    .status-on_mission  { background: #1565c0 !important; color: white !important; }
    .status-returning   { background: #e65100 !important; color: white !important; }
    .status-offline     { background: #c62828 !important; color: white !important; }
    .status-maintenance { background: #78909c !important; color: white !important; }
    .status-inactive    { background: #bdbdbd !important; }

    .type-rescue  { background: #1565c0 !important; color: white !important; }
    .type-medical { background: #2e7d32 !important; color: white !important; }
    .type-search  { background: #6a1b9a !important; color: white !important; }
    .type-fire    { background: #c62828 !important; color: white !important; }

    .empty-state {
      text-align: center; padding: 60px; color: #90a4ae;
      display: flex; flex-direction: column; align-items: center; gap: 12px;
    }
    .empty-state mat-icon { font-size: 64px; width: 64px; height: 64px; }
  `]
})
export class DroneListComponent implements OnInit, OnDestroy {
  private subs: Subscription[] = [];
  drones = signal<Drone[]>([]);

  constructor(
    public auth: AuthService,
    private droneService: DroneService,
    private ws: WebSocketService,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.load();
    this.subs.push(this.ws.droneEvents$.subscribe(() => this.load()));
  }

  load(): void {
    this.droneService.getAll().subscribe(list => this.drones.set(list));
  }

  simulateTelemetry(drone: Drone): void {
    // Demo: simula datos con batería que va bajando
    const telemetry: TelemetryRequest = {
      lat: drone.lat + (Math.random() - 0.5) * 0.01,
      lng: drone.lng + (Math.random() - 0.5) * 0.01,
      altitudeMeters: 50 + Math.random() * 50,
      batteryPercentage: Math.max(5, drone.batteryPercentage - Math.floor(Math.random() * 5)),
      status: drone.status,
      missionId: drone.missionId
    };
    this.droneService.sendTelemetry(drone.id, telemetry).subscribe({
      next: () => this.snack.open(`Telemetría enviada para ${drone.name}`, 'OK', { duration: 2000 }),
      error: () => this.snack.open('Error al enviar telemetría', 'OK', { duration: 3000 })
    });
  }

  droneIcon(status: string): string {
    const m: Record<string,string> = {
      AVAILABLE: 'flight_land', ON_MISSION: 'flight',
      RETURNING: 'flight_takeoff', OFFLINE: 'flight_off',
      MAINTENANCE: 'build', INACTIVE: 'power_off'
    };
    return m[status] ?? 'flight';
  }

  batteryIcon(pct: number): string {
    if (pct > 80) return 'full';
    if (pct > 50) return '6_bar';
    if (pct > 20) return '3_bar';
    return 'alert';
  }

  batteryColor(pct: number): 'primary' | 'accent' | 'warn' {
    if (pct < 20) return 'warn';
    if (pct < 50) return 'accent';
    return 'primary';
  }

  ngOnDestroy(): void { this.subs.forEach(s => s.unsubscribe()); }
}
