import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { EmergencyService, DroneService, MissionService } from '../../core/auth/api.service';
import { WebSocketService } from '../../core/websocket/websocket.service';
import { Emergency, Drone, Mission, KafkaEvent } from '../../shared/models/models';

interface LiveEvent {
  icon: string;
  color: string;
  message: string;
  time: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule, MatIconModule, MatChipsModule,
    MatDividerModule, MatListModule, MatBadgeModule,
    MatProgressBarModule
  ],
  template: `
    <div class="dashboard">
      <h2 class="page-title">
        <mat-icon>dashboard</mat-icon>
        Dashboard operativo
        <span class="realtime-badge">
          <span class="pulse"></span> Tiempo real
        </span>
      </h2>

      <!-- ── METRIC CARDS ── -->
      <div class="metrics-grid">

        <mat-card class="metric-card emergency-card">
          <mat-card-content>
            <div class="metric-row">
              <mat-icon class="metric-icon">crisis_alert</mat-icon>
              <div>
                <div class="metric-value">{{ activeEmergencies() }}</div>
                <div class="metric-label">Emergencias activas</div>
              </div>
            </div>
            <mat-progress-bar mode="determinate"
              [value]="(activeEmergencies() / (emergencies().length || 1)) * 100"
              color="warn" />
          </mat-card-content>
        </mat-card>

        <mat-card class="metric-card drone-card">
          <mat-card-content>
            <div class="metric-row">
              <mat-icon class="metric-icon">flight</mat-icon>
              <div>
                <div class="metric-value">{{ availableDrones() }}</div>
                <div class="metric-label">Drones disponibles</div>
              </div>
            </div>
            <div class="drone-detail">
              <span>En misión: {{ dronesOnMission() }}</span>
              <span>Offline: {{ dronesOffline() }}</span>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="metric-card mission-card">
          <mat-card-content>
            <div class="metric-row">
              <mat-icon class="metric-icon">task_alt</mat-icon>
              <div>
                <div class="metric-value">{{ activeMissions() }}</div>
                <div class="metric-label">Misiones en curso</div>
              </div>
            </div>
            <div class="mission-detail">
              <span class="unattended" *ngIf="unattendedMissions() > 0">
                ⚠ Sin cobertura: {{ unattendedMissions() }}
              </span>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="metric-card alert-card">
          <mat-card-content>
            <div class="metric-row">
              <mat-icon class="metric-icon">notifications_active</mat-icon>
              <div>
                <div class="metric-value">{{ liveEvents().length }}</div>
                <div class="metric-label">Eventos recientes</div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- ── CONTENT GRID ── -->
      <div class="content-grid">

        <!-- Live Events Feed -->
        <mat-card class="events-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>stream</mat-icon>
              Feed de eventos en vivo
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            @if (liveEvents().length === 0) {
              <div class="empty-state">
                <mat-icon>wifi_tethering</mat-icon>
                <p>Esperando eventos...</p>
              </div>
            }
            @for (event of liveEvents(); track event.time) {
              <div class="event-item">
                <mat-icon [style.color]="event.color">{{ event.icon }}</mat-icon>
                <div class="event-body">
                  <span class="event-msg">{{ event.message }}</span>
                  <span class="event-time">{{ event.time }}</span>
                </div>
              </div>
            }
          </mat-card-content>
        </mat-card>

        <!-- Drones status -->
        <mat-card class="drones-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>flight</mat-icon>
              Estado de flota
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            @for (drone of drones(); track drone.id) {
              <div class="drone-row">
                <mat-icon [class]="'drone-status-' + drone.status.toLowerCase()">
                  {{ droneIcon(drone.status) }}
                </mat-icon>
                <div class="drone-info">
                  <span class="drone-name">{{ drone.name }}</span>
                  <span class="drone-type">{{ drone.type }}</span>
                </div>
                <div class="battery-bar">
                  <mat-progress-bar mode="determinate"
                    [value]="drone.batteryPercentage"
                    [color]="batteryColor(drone.batteryPercentage)" />
                  <span class="battery-pct">{{ drone.batteryPercentage }}%</span>
                </div>
              </div>
            }
            @empty {
              <p class="empty-state">Cargando drones...</p>
            }
          </mat-card-content>
        </mat-card>

      </div>
    </div>
  `,
  styles: [`
    .dashboard { max-width: 1400px; }
    .page-title {
      display: flex; align-items: center; gap: 8px;
      font-size: 22px; font-weight: 600; color: #1a237e; margin-bottom: 20px;
    }
    .realtime-badge {
      display: flex; align-items: center; gap: 6px;
      background: #e8f5e9; color: #2e7d32;
      padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 500;
      margin-left: 8px;
    }
    .pulse {
      width: 8px; height: 8px; border-radius: 50%;
      background: #4caf50; animation: pulse 1.5s infinite;
    }
    @keyframes pulse {
      0% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.5; transform: scale(1.3); }
      100% { opacity: 1; transform: scale(1); }
    }

    .metrics-grid {
      display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px;
    }
    .metric-card { border-radius: 12px !important; }
    .metric-row { display: flex; align-items: center; gap: 16px; margin-bottom: 12px; }
    .metric-icon { font-size: 40px; width: 40px; height: 40px; }
    .metric-value { font-size: 36px; font-weight: 700; line-height: 1; }
    .metric-label { font-size: 13px; color: #546e7a; margin-top: 4px; }
    .emergency-card .metric-icon { color: #c62828; }
    .drone-card .metric-icon { color: #1565c0; }
    .mission-card .metric-icon { color: #2e7d32; }
    .alert-card .metric-icon { color: #e65100; }
    .drone-detail, .mission-detail { font-size: 12px; color: #78909c; display: flex; gap: 16px; }
    .unattended { color: #e65100; font-weight: 600; }

    .content-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .events-card, .drones-card { border-radius: 12px !important; }
    mat-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; }

    .event-item {
      display: flex; align-items: flex-start; gap: 10px;
      padding: 8px 0; border-bottom: 1px solid #f5f5f5;
    }
    .event-body { display: flex; flex-direction: column; flex: 1; }
    .event-msg { font-size: 13px; color: #37474f; }
    .event-time { font-size: 11px; color: #90a4ae; margin-top: 2px; }

    .drone-row {
      display: flex; align-items: center; gap: 12px;
      padding: 8px 0; border-bottom: 1px solid #f5f5f5;
    }
    .drone-info { flex: 1; }
    .drone-name { display: block; font-size: 13px; font-weight: 600; }
    .drone-type { font-size: 11px; color: #78909c; }
    .battery-bar { display: flex; align-items: center; gap: 6px; width: 140px; }
    .battery-bar mat-progress-bar { flex: 1; border-radius: 4px; }
    .battery-pct { font-size: 12px; color: #546e7a; min-width: 36px; }

    .drone-status-available { color: #2e7d32; }
    .drone-status-on_mission { color: #1565c0; }
    .drone-status-returning { color: #e65100; }
    .drone-status-offline { color: #c62828; }
    .drone-status-maintenance { color: #78909c; }
    .drone-status-inactive { color: #bdbdbd; }

    .empty-state {
      text-align: center; padding: 32px; color: #90a4ae;
      display: flex; flex-direction: column; align-items: center; gap: 8px;
    }
    .empty-state mat-icon { font-size: 40px; width: 40px; height: 40px; }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  private subs: Subscription[] = [];

  emergencies = signal<Emergency[]>([]);
  drones      = signal<Drone[]>([]);
  missions    = signal<Mission[]>([]);
  liveEvents  = signal<LiveEvent[]>([]);

  activeEmergencies = () => this.emergencies().filter(e =>
    !['RESOLVED','CANCELLED'].includes(e.status)).length;
  availableDrones  = () => this.drones().filter(d => d.status === 'AVAILABLE').length;
  dronesOnMission  = () => this.drones().filter(d => d.status === 'ON_MISSION').length;
  dronesOffline    = () => this.drones().filter(d => d.status === 'OFFLINE').length;
  activeMissions   = () => this.missions().filter(m =>
    !['COMPLETED','FAILED'].includes(m.status)).length;
  unattendedMissions = () => this.missions().filter(m => m.status === 'UNATTENDED').length;

  constructor(
    private emergencyService: EmergencyService,
    private droneService: DroneService,
    private missionService: MissionService,
    private ws: WebSocketService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.subscribeToEvents();
  }

  private loadData(): void {
    this.emergencyService.getAll().subscribe(e => this.emergencies.set(e));
    this.droneService.getAll().subscribe(d => this.drones.set(d));
    this.missionService.getAll().subscribe(m => this.missions.set(m));
  }

  private subscribeToEvents(): void {
    this.subs.push(
      this.ws.emergencyEvents$.subscribe(event => {
        this.addEvent('crisis_alert', '#c62828', event);
        this.emergencyService.getAll().subscribe(e => this.emergencies.set(e));
      }),
      this.ws.missionEvents$.subscribe(event => {
        this.addEvent('task_alt', '#1565c0', event);
        this.missionService.getAll().subscribe(m => this.missions.set(m));
      }),
      this.ws.droneEvents$.subscribe(event => {
        this.addEvent('flight', '#2e7d32', event);
        this.droneService.getAll().subscribe(d => this.drones.set(d));
      }),
      this.ws.alertEvents$.subscribe(event => {
        this.addEvent('warning', '#e65100', event);
      })
    );
  }

  private addEvent(icon: string, color: string, event: KafkaEvent): void {
    const msg = this.formatEventMessage(event);
    const live: LiveEvent = {
      icon, color, message: msg,
      time: new Date().toLocaleTimeString('es-PE')
    };
    this.liveEvents.update(prev => [live, ...prev].slice(0, 20));
  }

  private formatEventMessage(event: KafkaEvent): string {
    const p = event.payload;
    return switch_map(event.eventType, {
      'emergency.created':    `Nueva emergencia ${p.priority} en ${p.location?.region}`,
      'emergency.escalated':  `Emergencia escalada a ${p.newPriority}`,
      'mission.assigned':     `Misión asignada al drone ${p.droneId}`,
      'mission.reassigned':   `Misión reasignada — motivo: ${p.reason}`,
      'mission.completed':    `Misión completada — ${p.outcome}`,
      'drone.battery.low':    `Batería baja en drone ${p.droneId}: ${p.batteryPercentage}%`,
      'drone.offline':        `Drone ${p.droneId} sin señal`,
      'drone.status.changed': `Drone ${p.droneId}: ${p.previousStatus} → ${p.newStatus}`,
      'alert.created':        `Alerta ${p.severity}: ${p.message}`,
    }, `Evento: ${event.eventType}`);
  }

  droneIcon(status: string): string {
    const icons: Record<string, string> = {
      AVAILABLE: 'flight_land', ON_MISSION: 'flight',
      RETURNING: 'flight_takeoff', OFFLINE: 'flight_off',
      MAINTENANCE: 'build', INACTIVE: 'power_off'
    };
    return icons[status] ?? 'flight';
  }

  batteryColor(pct: number): 'primary' | 'accent' | 'warn' {
    if (pct < 20) return 'warn';
    if (pct < 50) return 'accent';
    return 'primary';
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }
}

function switch_map(key: string, map: Record<string, string>, fallback: string): string {
  return map[key] ?? fallback;
}
