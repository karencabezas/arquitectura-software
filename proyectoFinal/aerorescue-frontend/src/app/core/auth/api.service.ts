import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Emergency, CreateEmergencyRequest,
  Drone, TelemetryRequest,
  Mission, MissionOutcome
} from '../../shared/models/models';
import { environment } from '../../../environments/environment';

// ── Emergency Service ──────────────────────────────────
@Injectable({ providedIn: 'root' })
export class EmergencyService {
  private base = `${environment.apiUrl}/emergencies`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<Emergency[]> {
    return this.http.get<Emergency[]>(this.base);
  }
  getById(id: string): Observable<Emergency> {
    return this.http.get<Emergency>(`${this.base}/${id}`);
  }
  create(req: CreateEmergencyRequest): Observable<Emergency> {
    return this.http.post<Emergency>(this.base, req);
  }
  updateStatus(id: string, status: string): Observable<Emergency> {
    return this.http.patch<Emergency>(`${this.base}/${id}/status`, { status });
  }
  escalate(id: string): Observable<Emergency> {
    return this.http.post<Emergency>(`${this.base}/${id}/escalate`, {});
  }
}

// ── Drone Service ──────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class DroneService {
  private base = `${environment.apiUrl}/drones`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<Drone[]> {
    return this.http.get<Drone[]>(this.base);
  }
  getById(id: string): Observable<Drone> {
    return this.http.get<Drone>(`${this.base}/${id}`);
  }
  register(name: string, type: string, lat: number, lng: number): Observable<Drone> {
    return this.http.post<Drone>(this.base, { name, type, lat, lng });
  }
  sendTelemetry(droneId: string, telemetry: TelemetryRequest): Observable<Drone> {
    return this.http.post<Drone>(`${this.base}/${droneId}/telemetry`, telemetry);
  }
}

// ── Mission Service ────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class MissionService {
  private base = `${environment.apiUrl}/missions`;
  constructor(private http: HttpClient) {}

  getAll(): Observable<Mission[]> {
    return this.http.get<Mission[]>(this.base);
  }
  getById(id: string): Observable<Mission> {
    return this.http.get<Mission>(`${this.base}/${id}`);
  }
  reassign(id: string, reason: string = 'MANUAL'): Observable<Mission> {
    return this.http.post<Mission>(`${this.base}/${id}/reassign?reason=${reason}`, {});
  }
  close(id: string, outcome: MissionOutcome): Observable<Mission> {
    return this.http.post<Mission>(`${this.base}/${id}/close?outcome=${outcome}`, {});
  }
}
