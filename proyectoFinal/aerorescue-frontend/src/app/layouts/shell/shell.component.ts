import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { AuthService } from '../../core/auth/auth.service';
import { WebSocketService } from '../../core/websocket/websocket.service';
import { Alert } from '../../shared/models/models';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatSidenavModule, MatToolbarModule, MatListModule,
    MatIconModule, MatButtonModule, MatBadgeModule,
    MatTooltipModule, MatChipsModule
  ],
  template: `
    <mat-sidenav-container class="shell-container">

      <!-- ── SIDENAV ── -->
      <mat-sidenav mode="side" opened class="sidenav">
        <div class="brand">
          <mat-icon class="brand-icon">emergency</mat-icon>
          <span class="brand-name">AeroRescue</span>
        </div>

        <div class="ws-status" [class.connected]="ws.isConnected()">
          <mat-icon>{{ ws.isConnected() ? 'wifi' : 'wifi_off' }}</mat-icon>
          <span>{{ ws.isConnected() ? 'Tiempo real activo' : 'Desconectado' }}</span>
        </div>

        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard" routerLinkActive="active-link">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
          <a mat-list-item routerLink="/emergencies" routerLinkActive="active-link">
            <mat-icon matListItemIcon>crisis_alert</mat-icon>
            <span matListItemTitle>Emergencias</span>
          </a>
          <a mat-list-item routerLink="/drones" routerLinkActive="active-link">
            <mat-icon matListItemIcon>flight</mat-icon>
            <span matListItemTitle>Drones</span>
          </a>
          <a mat-list-item routerLink="/missions" routerLinkActive="active-link">
            <mat-icon matListItemIcon>task_alt</mat-icon>
            <span matListItemTitle>Misiones</span>
          </a>
        </mat-nav-list>

        <div class="sidenav-footer">
          <div class="user-info">
            <mat-icon>account_circle</mat-icon>
            <div>
              <div class="username">{{ auth.user()?.username }}</div>
              <mat-chip class="role-chip" [class]="'role-' + auth.role()?.toLowerCase()">
                {{ auth.role() }}
              </mat-chip>
            </div>
          </div>
          <button mat-icon-button (click)="auth.logout()" matTooltip="Cerrar sesión">
            <mat-icon>logout</mat-icon>
          </button>
        </div>
      </mat-sidenav>

      <!-- ── MAIN CONTENT ── -->
      <mat-sidenav-content class="main-content">

        <!-- Toolbar -->
        <mat-toolbar color="primary" class="top-toolbar">
          <span class="toolbar-spacer"></span>

          <!-- Alert badge -->
          <button mat-icon-button
                  [matBadge]="alertCount()"
                  [matBadgeHidden]="alertCount() === 0"
                  matBadgeColor="warn"
                  matTooltip="Alertas activas">
            <mat-icon>notifications</mat-icon>
          </button>

          <span class="region-label">
            <mat-icon>location_on</mat-icon>
            {{ auth.region() }}
          </span>
        </mat-toolbar>

        <!-- Alerts banner -->
        @if (latestAlert()) {
          <div class="alert-banner" [class]="'severity-' + latestAlert()!.severity.toLowerCase()">
            <mat-icon>warning</mat-icon>
            <span>{{ latestAlert()!.message }}</span>
            <button mat-icon-button (click)="dismissAlert()">
              <mat-icon>close</mat-icon>
            </button>
          </div>
        }

        <!-- Page content -->
        <div class="page-content">
          <router-outlet />
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .shell-container { height: 100vh; }

    .sidenav {
      width: 240px;
      background: #1a237e;
      color: white;
      display: flex; flex-direction: column;
    }

    .brand {
      display: flex; align-items: center; gap: 10px;
      padding: 20px 16px 12px;
      border-bottom: 1px solid rgba(255,255,255,0.1);
    }
    .brand-icon { font-size: 32px; width: 32px; height: 32px; color: #90caf9; }
    .brand-name { font-size: 20px; font-weight: 700; color: white; }

    .ws-status {
      display: flex; align-items: center; gap: 6px;
      padding: 8px 16px; font-size: 12px; color: #ef9a9a;
    }
    .ws-status mat-icon { font-size: 16px; width: 16px; height: 16px; }
    .ws-status.connected { color: #a5d6a7; }

    mat-nav-list { flex: 1; padding-top: 8px; }
    .active-link { background: rgba(255,255,255,0.15) !important; border-radius: 8px; }
    mat-nav-list a { color: rgba(255,255,255,0.85); border-radius: 8px; margin: 2px 8px; }
    mat-nav-list a mat-icon { color: #90caf9; }

    .sidenav-footer {
      display: flex; align-items: center; justify-content: space-between;
      padding: 12px 16px;
      border-top: 1px solid rgba(255,255,255,0.1);
    }
    .user-info { display: flex; align-items: center; gap: 8px; color: white; }
    .username { font-size: 13px; font-weight: 600; }
    .role-chip { font-size: 10px; height: 20px; }
    .role-admin { background: #ff7043 !important; color: white !important; }
    .role-supervisor { background: #ab47bc !important; color: white !important; }
    .role-operator { background: #26a69a !important; color: white !important; }
    .role-drone_tech { background: #42a5f5 !important; color: white !important; }
    .role-viewer { background: #78909c !important; color: white !important; }

    .top-toolbar { box-shadow: 0 2px 4px rgba(0,0,0,0.2); }
    .toolbar-spacer { flex: 1; }
    .region-label {
      display: flex; align-items: center; gap: 4px;
      font-size: 13px; margin-left: 8px; opacity: 0.85;
    }

    .alert-banner {
      display: flex; align-items: center; gap: 10px;
      padding: 10px 20px; font-size: 14px; font-weight: 500;
    }
    .alert-banner button { margin-left: auto; }
    .severity-critical { background: #c62828; color: white; }
    .severity-high { background: #e65100; color: white; }
    .severity-medium { background: #f57f17; color: white; }
    .severity-low { background: #558b2f; color: white; }

    .main-content { display: flex; flex-direction: column; background: #f5f5f5; }
    .page-content { flex: 1; padding: 24px; overflow-y: auto; }
  `]
})
export class ShellComponent implements OnInit, OnDestroy {
  private subs: Subscription[] = [];
  private alerts = signal<Alert[]>([]);

  alertCount  = computed(() => this.alerts().length);
  latestAlert = computed(() => this.alerts()[0] ?? null);

  constructor(
    public auth: AuthService,
    public ws: WebSocketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Conectar WebSocket al cargar el shell
    const token = this.auth.getAccessToken();
    if (token && !this.ws.isConnected()) {
      this.ws.connect(token);
    }

    // Escuchar alertas en tiempo real
    this.subs.push(
      this.ws.alertEvents$.subscribe(event => {
        const alert = event.payload as Alert;
        this.alerts.update(prev => [alert, ...prev].slice(0, 10));
      })
    );
  }

  dismissAlert(): void {
    this.alerts.update(prev => prev.slice(1));
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    this.ws.disconnect();
  }
}
