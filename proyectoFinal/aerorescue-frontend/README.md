# AeroRescue Frontend

Angular 18 + Angular Material — Dashboard en tiempo real para coordinación de drones de emergencia.

## Stack
- Angular 18 (Standalone Components, Signals)
- Angular Material
- STOMP WebSocket (tiempo real)
- RxJS

## Estructura
```
src/app/
├── core/
│   ├── auth/           ← AuthService, LoginComponent, MfaComponent, ApiService
│   ├── guards/         ← authGuard, roleGuard (RBAC)
│   ├── interceptors/   ← jwtInterceptor (Bearer token + refresh automático)
│   └── websocket/      ← WebSocketService (STOMP sobre SockJS)
├── shared/
│   └── models/         ← Todos los tipos TypeScript
├── features/
│   ├── dashboard/      ← Métricas tiempo real + feed de eventos Kafka
│   ├── emergencies/    ← Lista + formulario con validación ABAC
│   ├── drones/         ← Flota + simulador de telemetría (demo)
│   └── missions/       ← Lista + cierre + reasignación manual
└── layouts/
    └── shell/          ← Sidenav + toolbar + badge de alertas
```

## Setup

```bash
# Instalar dependencias
npm install

# Levantar en desarrollo (proxy al backend)
npm start

# Acceder en: http://localhost:4200
```

## Usuarios demo

| Usuario              | Contraseña  | Rol         | Región      |
|----------------------|-------------|-------------|-------------|
| admin                | Admin123!   | ADMIN       | ALL         |
| supervisor.lima      | Admin123!   | SUPERVISOR  | LIMA_NORTE  |
| operator.lima.norte  | Admin123!   | OPERATOR    | LIMA_NORTE  |
| tech.drones          | Admin123!   | DRONE_TECH  | ALL         |

## Seguridad implementada

### RBAC en rutas (Angular Guards)
- `/emergencies/new` → solo ADMIN, SUPERVISOR, OPERATOR
- `/drones/new`      → solo ADMIN, DRONE_TECH

### ABAC en UI
- Formulario de emergencia: OPERATOR solo puede seleccionar su región (ABAC-01)
- Botón escalar: solo clearance_level >= 3 (ABAC-02)
- Botón reasignar misión: solo clearance_level >= 3 (ABAC-05)
- Botón cerrar misión: solo clearance_level >= 2

## Tiempo real (WebSocket STOMP)

El `WebSocketService` se conecta al Notification Service en `/ws`
y distribuye eventos a 4 topics:

| Topic Angular         | Eventos Kafka escuchados                      |
|-----------------------|-----------------------------------------------|
| `/topic/emergencies`  | emergency.updated, emergency.escalated        |
| `/topic/missions`     | mission.assigned, mission.reassigned, mission.completed |
| `/topic/drones`       | drone.telemetry, drone.status.changed, drone.battery.low, drone.offline |
| `/topic/alerts`       | alert.created                                 |

## Demo — Simular flujo completo

1. Login como `admin`
2. Ir a **Drones** → registrar un drone tipo RESCUE
3. Ir a **Emergencias** → crear emergencia HIGH en LIMA_NORTE
4. Observar en **Dashboard** cómo aparece el evento en tiempo real
5. Ir a **Drones** → simular telemetría con batería < 20%
6. Observar la alerta de batería baja en el banner del shell
7. Observar en **Misiones** cómo pasa a AWAITING_REPLACEMENT
