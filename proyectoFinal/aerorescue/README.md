# AeroRescue 🚁
Plataforma distribuida para coordinación de drones de emergencia en tiempo real.

## Stack
- **Backend**: Java 17 · Spring Boot 3.2 · Apache Kafka · PostgreSQL
- **Frontend**: Angular 18 · Angular Material · WebSocket
- **Infra**: Docker · Docker Compose

## Arquitectura
- Microservicios + Event-Driven Architecture
- Clean Architecture + Arquitectura Hexagonal por servicio
- Seguridad RBAC + ABAC con JWT RS256 + 2FA (Google Authenticator)

---

## Acceso a la aplicación web

Una vez levantado el sistema, abre el navegador en:

```
http://localhost
```

### Credenciales de acceso

| Usuario               | Contraseña  | Rol         | Región      | Clearance |
|-----------------------|-------------|-------------|-------------|-----------|
| `admin`               | `Admin123!` | ADMIN       | ALL         | 5         |
| `supervisor.lima`     | `Admin123!` | SUPERVISOR  | LIMA_NORTE  | 3         |
| `operator.lima.norte` | `Admin123!` | OPERATOR    | LIMA_NORTE  | 2         |
| `tech.drones`         | `Admin123!` | DRONE_TECH  | ALL         | 1         |

> **Nota:** Los usuarios de demo tienen MFA deshabilitado para facilitar el acceso. Para habilitar MFA en un usuario, usa el endpoint `POST /auth/enroll-mfa` con el token del usuario y escanea el QR con Google Authenticator.

### Qué puede hacer cada usuario

| Rol         | Puede hacer |
|-------------|-------------|
| ADMIN       | Todo — ver, crear, escalar, reasignar, cerrar misiones, registrar drones |
| SUPERVISOR  | Ver todo su organización, escalar emergencias, aprobar reasignaciones manuales |
| OPERATOR    | Crear emergencias **solo en su región** (LIMA_NORTE), ver misiones y drones |
| DRONE_TECH  | Registrar y mantener drones, enviar telemetría |

---

## Setup inicial (una sola vez)

> **Requisitos:** Docker Desktop corriendo, Git Bash o WSL en Windows, Maven 3.9+, OpenSSL

### Opción A — Script automático (recomendado)

```bash
chmod +x scripts/start.sh
./scripts/start.sh
```

El script hace todo en orden: genera claves JWT, levanta infraestructura, crea topics Kafka, compila el backend y levanta todos los servicios.

### Opción B — Manual paso a paso

#### 1. Generar claves JWT RS256
```bash
chmod +x scripts/generate-keys.sh
./scripts/generate-keys.sh
```
Esto genera el par de claves RSA y las distribuye a cada servicio en `src/main/resources/certs/`.

#### 2. Levantar infraestructura
```bash
docker compose up -d zookeeper kafka kafka-ui \
  auth-db emergency-db drone-db mission-db alert-db audit-db
```

#### 3. Esperar ~20s y crear topics Kafka
```bash
chmod +x scripts/create-topics.sh
./scripts/create-topics.sh
```

#### 4. Compilar el backend
```bash
mvn clean package -DskipTests
```

#### 5. Levantar microservicios
```bash
docker compose up -d \
  auth-service api-gateway \
  emergency-service drone-service mission-service \
  alert-service notification-service audit-service
```

#### 6. Levantar frontend
```bash
docker compose up -d frontend
```

#### 7. Verificar que todo está corriendo
```bash
chmod +x scripts/health-check.sh
./scripts/health-check.sh
```

---

## URLs del sistema

| Componente        | URL                              | Descripción |
|-------------------|----------------------------------|-------------|
| **Frontend**      | http://localhost                 | Dashboard principal — acceso con credenciales arriba |
| **API Gateway**   | http://localhost:8080            | Punto de entrada único de la API |
| **Kafka UI**      | http://localhost:8090            | Monitoreo de topics y mensajes Kafka |
| **Swagger Auth**  | http://localhost:8081/swagger-ui.html | API de autenticación |
| **Swagger Emergency** | http://localhost:8082/swagger-ui.html | API de emergencias |
| **Swagger Drone** | http://localhost:8083/swagger-ui.html | API de drones |
| **Swagger Mission**| http://localhost:8084/swagger-ui.html | API de misiones |
| **Swagger Audit** | http://localhost:8087/swagger-ui.html | Trazabilidad de eventos |

---

## Scripts disponibles

| Script | Descripción |
|--------|-------------|
| `./scripts/start.sh` | Levanta todo el sistema en orden correcto |
| `./scripts/stop.sh` | Detiene todos los contenedores |
| `./scripts/health-check.sh` | Verifica estado de servicios, topics y contenedores |
| `./scripts/demo.sh` | Ejecuta 10 escenarios de demo para presentación |
| `./scripts/generate-keys.sh` | Genera claves JWT RS256 (solo ejecutar una vez) |
| `./scripts/create-topics.sh` | Crea los 11 topics de Kafka |

---

## Demo rápida para presentación

```bash
chmod +x scripts/demo.sh
./scripts/demo.sh
```

El script ejecuta automáticamente:
1. Login como ADMIN y obtención de JWT
2. RBAC — request rechazado sin token válido
3. Crear emergencia HIGH en LIMA_NORTE
4. Listar drones disponibles
5. Simular batería baja (< 20%) → graceful handoff automático
6. Simular batería crítica (< 5%) → drone forzado a RETURNING
7. Ver misiones con estados actualizados en tiempo real
8. ABAC — operador rechazado al intentar crear en otra región
9. Escalar emergencia a CRITICAL
10. Consultar trazabilidad en Audit Service por correlationId

---

## Puertos de cada servicio

| Servicio             | Puerto |
|----------------------|--------|
| Frontend (Nginx)     | 80     |
| API Gateway          | 8080   |
| Auth Service         | 8081   |
| Emergency Service    | 8082   |
| Drone Service        | 8083   |
| Mission Service      | 8084   |
| Alert Service        | 8085   |
| Notification Service | 8086   |
| Audit Service        | 8087   |
| Kafka UI             | 8090   |

---

## Eventos Kafka

| Topic                | Productor         | Consumidores                                      |
|----------------------|-------------------|---------------------------------------------------|
| emergency.created    | Emergency Service | Mission, Audit                                    |
| emergency.updated    | Emergency Service | Mission, Notification, Audit                      |
| emergency.escalated  | Emergency Service | Alert, Mission, Audit                             |
| drone.telemetry      | Drone Service     | Drone (interno), Mission (CQRS), Notification, Audit |
| drone.battery.low    | Drone Service     | Alert, Mission, Notification, Audit               |
| drone.offline        | Drone Service     | Alert, Mission, Notification, Audit               |
| drone.status.changed | Drone Service     | Mission (CQRS), Notification, Audit               |
| mission.assigned     | Mission Service   | Drone, Notification, Audit                        |
| mission.reassigned   | Mission Service   | Drone, Notification, Audit                        |
| mission.completed    | Mission Service   | Drone, Notification, Audit                        |
| alert.created        | Alert Service     | Notification, Audit                               |

---

## Seguridad implementada

### RBAC (control por roles)
El API Gateway valida el rol del JWT antes de llegar a cada microservicio:
- `VIEWER` no puede hacer POST, PUT, PATCH ni DELETE en ningún endpoint
- Solo `DRONE_TECH` y `ADMIN` pueden registrar drones (`POST /drones`)
- Solo `ADMIN` y `SUPERVISOR` pueden consultar auditoría (`GET /audit`)

### ABAC (control por atributos)
Cada microservicio valida atributos del JWT contra el contexto de la operación:

| Regla   | Descripción |
|---------|-------------|
| ABAC-01 | OPERATOR solo puede crear emergencias en su región asignada |
| ABAC-02 | Crear emergencias CRITICAL requiere `clearance_level >= 3` |
| ABAC-03 | SUPERVISOR solo gestiona emergencias de su organización |
| ABAC-04 | Solo se asignan drones compatibles con el tipo de misión |
| ABAC-05 | Reasignación manual requiere `clearance_level >= 3` |
| ABAC-06 | OPERATOR no ve misiones de otras regiones |
| ABAC-07 | Solo DRONE_TECH o ADMIN pueden registrar/dar de baja drones |
| ABAC-08 | Ver ubicación en tiempo real requiere `clearance_level >= 2` |

---

## Parar el sistema

```bash
# Detener contenedores (conserva datos)
./scripts/stop.sh

# Detener y eliminar volúmenes (borra bases de datos)
docker compose down -v
```
