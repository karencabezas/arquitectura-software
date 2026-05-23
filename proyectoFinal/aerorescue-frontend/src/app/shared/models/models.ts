// ── Auth ──────────────────────────────────────────────
export interface LoginResponse {
  status: 'SUCCESS' | 'MFA_REQUIRED';
  tempToken?: string;
  accessToken?: string;
  refreshToken?: string;
  expiresIn?: number;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface JwtPayload {
  sub: string;
  username: string;
  role: UserRole;
  region: string;
  clearance_level: number;
  allowed_mission_types: string[];
  organization_id: string;
  exp: number;
}

export type UserRole = 'ADMIN' | 'SUPERVISOR' | 'OPERATOR' | 'DRONE_TECH' | 'VIEWER';

// ── Emergency ─────────────────────────────────────────
export type EmergencyStatus = 'PENDING' | 'ASSIGNED' | 'IN_PROGRESS' | 'CRITICAL' | 'RESOLVED' | 'CANCELLED';
export type EmergencyPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type EmergencyType = 'RESCUE' | 'MEDICAL' | 'SEARCH' | 'FIRE';

export interface Emergency {
  id: string;
  status: EmergencyStatus;
  priority: EmergencyPriority;
  type: EmergencyType;
  address: string;
  region: string;
  lat: number;
  lng: number;
  description: string;
  reportedBy: string;
  organizationId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEmergencyRequest {
  priority: EmergencyPriority;
  type: EmergencyType;
  address: string;
  region: string;
  lat: number;
  lng: number;
  description?: string;
}

// ── Drone ─────────────────────────────────────────────
export type DroneStatus = 'INACTIVE' | 'AVAILABLE' | 'ON_MISSION' | 'RETURNING' | 'OFFLINE' | 'MAINTENANCE';
export type DroneType = 'RESCUE' | 'MEDICAL' | 'SEARCH' | 'FIRE';
export type BatteryStatus = 'NORMAL' | 'LOW' | 'CRITICAL';

export interface Drone {
  id: string;
  name: string;
  status: DroneStatus;
  type: DroneType;
  lat: number;
  lng: number;
  batteryPercentage: number;
  batteryStatus: BatteryStatus;
  missionId?: string;
  lastTelemetryAt: string;
}

export interface TelemetryRequest {
  lat: number;
  lng: number;
  altitudeMeters: number;
  batteryPercentage: number;
  status: string;
  missionId?: string;
}

// ── Mission ───────────────────────────────────────────
export type MissionStatus =
  | 'PENDING' | 'ASSIGNED' | 'IN_PROGRESS'
  | 'AWAITING_REPLACEMENT' | 'REASSIGNED'
  | 'UNATTENDED' | 'COMPLETED' | 'FAILED';

export type MissionOutcome = 'SUCCESS' | 'RESOLVED_WITHOUT_DRONE' | 'TIMEOUT' | 'DRONE_FAILURE';

export interface Mission {
  id: string;
  emergencyId: string;
  droneId?: string;
  status: MissionStatus;
  outcome?: MissionOutcome;
  missionType: string;
  priority: string;
}

// ── Alert ─────────────────────────────────────────────
export type AlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type AlertType = 'BATTERY_LOW' | 'DRONE_OFFLINE' | 'EMERGENCY_ESCALATED' | 'MISSION_UNATTENDED';

export interface Alert {
  alertId: string;
  type: AlertType;
  severity: AlertSeverity;
  relatedEntityType: string;
  relatedEntityId: string;
  missionId?: string;
  message: string;
  createdAt: string;
}

// ── Kafka WebSocket event envelope ────────────────────
export interface KafkaEvent {
  eventId: string;
  eventType: string;
  eventVersion: string;
  occurredAt: string;
  source: string;
  correlationId: string;
  payload: any;
}
