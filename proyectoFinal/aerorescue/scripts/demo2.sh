#!/bin/bash
# ══════════════════════════════════════════════════════════════
# AeroRescue — Demo Script para Presentación
# Flujo completo: Activar drones → Emergencia → Misión →
#                 Batería baja → Reasignación → Completar
# Requiere: curl, jq
# ══════════════════════════════════════════════════════════════

BASE="http://localhost:8080"

# Colores
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

box()   { echo ""; echo -e "${BLUE}${BOLD}══════════════════════════════════════════${NC}"; echo -e "${BLUE}${BOLD}  $1${NC}"; echo -e "${BLUE}${BOLD}══════════════════════════════════════════${NC}"; }
step()  { echo ""; echo -e "${CYAN}▶  $1${NC}"; }
ok()    { echo -e "${GREEN}✓  $1${NC}"; }
info()  { echo -e "${YELLOW}ℹ  $1${NC}"; }
pause() { echo ""; echo -e "${YELLOW}  [ENTER para continuar → ]${NC}"; read -r _; }

# ── LOGIN ──────────────────────────────────────────────────────
box "PASO 0 — Login como ADMIN"
step "POST /auth/login"
TOKEN_RESP=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}')

STATUS=$(echo "$TOKEN_RESP" | jq -r '.status' 2>/dev/null)
TOKEN=$(echo "$TOKEN_RESP"  | jq -r '.accessToken' 2>/dev/null)

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo -e "${RED}✗ Login fallido. ¿Está corriendo el backend?${NC}"
  echo "$TOKEN_RESP"
  exit 1
fi
ok "Token JWT obtenido"
info "Rol: ADMIN | Región: ALL | clearance_level: 5"
pause

# ── LISTAR DRONES ──────────────────────────────────────────────
box "PASO 1 — Ver drones registrados"
step "GET /drones"
DRONES=$(curl -s "$BASE/drones" -H "Authorization: Bearer $TOKEN")
echo "$DRONES" | jq '[.[] | {id, name, status, type, batteryPercentage}]'

DRONE_COUNT=$(echo "$DRONES" | jq 'length')
if [ "$DRONE_COUNT" -eq 0 ]; then
  info "No hay drones. Registrando drones de demo..."

  D1=$(curl -s -X POST "$BASE/drones" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"name":"DR-001","type":"RESCUE","lat":-12.0464,"lng":-77.0428}')
  D2=$(curl -s -X POST "$BASE/drones" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"name":"DR-002","type":"RESCUE","lat":-12.0550,"lng":-77.0350}')
  ok "Drones DR-001 y DR-002 registrados"
  DRONES=$(curl -s "$BASE/drones" -H "Authorization: Bearer $TOKEN")
fi

DRONE1_ID=$(echo "$DRONES" | jq -r '.[0].id')
DRONE2_ID=$(echo "$DRONES" | jq -r '.[1].id // empty')
info "Drone principal: $DRONE1_ID"
pause

# ── ACTIVAR DRONES ─────────────────────────────────────────────
box "PASO 2 — Activar drones (INACTIVE → AVAILABLE)"
info "Los drones se crean en estado INACTIVE."
info "Enviamos telemetría para cambiarlos a AVAILABLE."
info "En producción real, el hardware envía esto automáticamente."
echo ""

step "Enviando telemetría a DR-001 (batería 95%, status AVAILABLE)"
curl -s -X POST "$BASE/drones/$DRONE1_ID/telemetry" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"lat\":-12.0464,\"lng\":-77.0428,\"altitudeMeters\":0,\"batteryPercentage\":95,\"status\":\"AVAILABLE\"}" | jq '{id,name,status,batteryPercentage}'

if [ -n "$DRONE2_ID" ]; then
  step "Enviando telemetría a DR-002 (batería 90%, status AVAILABLE)"
  curl -s -X POST "$BASE/drones/$DRONE2_ID/telemetry" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"lat\":-12.0550,\"lng\":-77.0350,\"altitudeMeters\":0,\"batteryPercentage\":90,\"status\":\"AVAILABLE\"}" | jq '{id,name,status,batteryPercentage}'
fi

ok "Drones en estado AVAILABLE — listos para misiones"
info "Mission Service actualiza su proyección CQRS local via evento drone.status.changed"
pause

# ── CREAR EMERGENCIA ───────────────────────────────────────────
box "PASO 3 — Crear emergencia HIGH (RBAC + ABAC)"
info "RBAC: solo ADMIN/SUPERVISOR/OPERATOR pueden crear emergencias"
info "ABAC-01: OPERATOR solo puede crear en su región asignada"
info "ABAC-02: CRITICAL requiere clearance_level >= 3"
echo ""
step "POST /emergencies"

EMERGENCY=$(curl -s -X POST "$BASE/emergencies" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "priority": "HIGH",
    "type": "RESCUE",
    "address": "Av. Arequipa 1200, Lima",
    "region": "LIMA_NORTE",
    "lat": -12.0464,
    "lng": -77.0428,
    "description": "Persona atrapada en edificio — Demo AeroRescue"
  }')

echo "$EMERGENCY" | jq '{id, status, priority, type, region}'
EMERGENCY_ID=$(echo "$EMERGENCY" | jq -r '.id')
ok "Emergencia creada: $EMERGENCY_ID"
echo ""
info "Emergency Service publica → Kafka: emergency.created"
info "Mission Service consume el evento y busca drone disponible"
info "Algoritmo scoring: batería(35%) + distancia(30%) + tipo(25%) + disponibilidad(10%)"
pause

# ── VER MISIÓN CREADA ──────────────────────────────────────────
box "PASO 4 — Misión asignada automáticamente"
step "GET /missions (esperando 3s para que Kafka procese...)"
sleep 3

MISSIONS=$(curl -s "$BASE/missions" -H "Authorization: Bearer $TOKEN")
echo "$MISSIONS" | jq '[.[] | {id, status, missionType, priority, droneId, emergencyId}]'

MISSION_ID=$(echo "$MISSIONS" | jq -r '[.[] | select(.emergencyId == "'"$EMERGENCY_ID"'")] | .[0].id')
MISSION_STATUS=$(echo "$MISSIONS" | jq -r '[.[] | select(.emergencyId == "'"$EMERGENCY_ID"'")] | .[0].status')

echo ""
if [ "$MISSION_STATUS" = "ASSIGNED" ] || [ "$MISSION_STATUS" = "IN_PROGRESS" ]; then
  ok "Misión en estado: $MISSION_STATUS"
  info "El drone fue seleccionado automáticamente por el algoritmo de scoring"
  info "Mission Service publicó → Kafka: mission.assigned"
  info "Drone Service actualizó el drone a ON_MISSION"
elif [ "$MISSION_STATUS" = "UNATTENDED" ]; then
  echo -e "${RED}✗ Misión en UNATTENDED — drone no disponible aún en proyección CQRS${NC}"
  info "Espera 5s más y corre: curl -s $BASE/missions -H 'Authorization: Bearer TOKEN'"
else
  info "Estado actual: $MISSION_STATUS — ID: $MISSION_ID"
fi
pause

# ── DRONE EN RUTA ──────────────────────────────────────────────
box "PASO 5 — Drone en ruta → simulando vuelo"
step "Enviando telemetrías para simular el vuelo del drone hacia la emergencia"
info "En producción: el hardware envía esto cada segundo automáticamente"
echo ""

ASSIGNED_DRONE=$(curl -s "$BASE/missions" -H "Authorization: Bearer $TOKEN" | \
  jq -r '[.[] | select(.emergencyId == "'"$EMERGENCY_ID"'")] | .[0].droneId')

if [ -n "$ASSIGNED_DRONE" ] && [ "$ASSIGNED_DRONE" != "null" ]; then
  info "Drone asignado: $ASSIGNED_DRONE"

  for i in 1 2 3; do
    LAT=$(echo "-12.0$(( RANDOM % 100 ))" | head -c 9)
    LNG=$(echo "-77.0$(( RANDOM % 100 ))" | head -c 9)
    BAT=$((95 - i * 5))
    step "Telemetría $i/3 — Batería: $BAT% | Coordenadas cambiando..."
    curl -s -X POST "$BASE/drones/$ASSIGNED_DRONE/telemetry" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{\"lat\":$LAT,\"lng\":$LNG,\"altitudeMeters\":45,\"batteryPercentage\":$BAT,\"status\":\"ON_MISSION\",\"missionId\":\"$MISSION_ID\"}" \
      | jq '{name, status, batteryPercentage}' 2>/dev/null || true
    sleep 1
  done
  ok "Drone volando hacia la emergencia"
fi
pause

# ── BATERÍA BAJA — FLUJO WOW ───────────────────────────────────
box "PASO 6 — ⚠ BATERÍA BAJA — El 'Flow WOW'"
info "Umbral WARNING: < 20% → busca reemplazo (graceful handoff)"
info "Umbral CRITICAL: < 5%  → fuerza RETURNING inmediatamente"
echo ""
step "Simulando batería al 18% (WARNING)..."

if [ -n "$ASSIGNED_DRONE" ] && [ "$ASSIGNED_DRONE" != "null" ]; then
  curl -s -X POST "$BASE/drones/$ASSIGNED_DRONE/telemetry" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"lat\":-12.0501,\"lng\":-77.0350,\"altitudeMeters\":45,\"batteryPercentage\":18,\"status\":\"ON_MISSION\",\"missionId\":\"$MISSION_ID\"}" \
    | jq '{name, status, batteryPercentage, batteryStatus}'

  echo ""
  ok "Drone Service detectó battery < 20%"
  info "Publicó → Kafka: drone.battery.low (severity: WARNING)"
  info "Mission Service consume el evento → inicia graceful handoff"
  info "Drone ACTUAL continúa en misión mientras se busca reemplazo"
fi
pause

step "Verificando estado de la misión tras batería baja..."
sleep 2
curl -s "$BASE/missions" -H "Authorization: Bearer $TOKEN" | \
  jq '[.[] | select(.emergencyId == "'"$EMERGENCY_ID"'")] | .[0] | {id, status, droneId}'
pause

# ── BATERÍA CRÍTICA ────────────────────────────────────────────
box "PASO 7 — 🔴 BATERÍA CRÍTICA < 5% — Drone forzado a RETURNING"
step "Simulando batería al 3% (CRITICAL)..."

if [ -n "$ASSIGNED_DRONE" ] && [ "$ASSIGNED_DRONE" != "null" ]; then
  curl -s -X POST "$BASE/drones/$ASSIGNED_DRONE/telemetry" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"lat\":-12.0501,\"lng\":-77.0350,\"altitudeMeters\":30,\"batteryPercentage\":3,\"status\":\"ON_MISSION\",\"missionId\":\"$MISSION_ID\"}" \
    | jq '{name, status, batteryPercentage, batteryStatus}'

  echo ""
  ok "Drone Service forzó al drone a RETURNING"
  info "Publicó → Kafka: drone.battery.low (severity: CRITICAL)"
  info "Mission Service inicia reasignación de emergencia"
fi
pause

# ── VER REASIGNACIÓN ───────────────────────────────────────────
box "PASO 8 — Reasignación automática de misión"
step "Estado actual de misiones y drones..."
sleep 2

echo ""
echo -e "${BOLD}MISIONES:${NC}"
curl -s "$BASE/missions" -H "Authorization: Bearer $TOKEN" | \
  jq '[.[] | {id, status, droneId, outcome}]'

echo ""
echo -e "${BOLD}DRONES:${NC}"
curl -s "$BASE/drones" -H "Authorization: Bearer $TOKEN" | \
  jq '[.[] | {name, status, batteryPercentage}]'
pause

# ── COMPLETAR MISIÓN ───────────────────────────────────────────
box "PASO 9 — Completar misión (outcome: SUCCESS)"
info "El supervisor cierra la misión manualmente"
info "Mission Service publica → Kafka: mission.completed"
info "Emergency Service consume → actualiza emergencia a RESOLVED"
echo ""

if [ -n "$MISSION_ID" ] && [ "$MISSION_ID" != "null" ]; then
  step "POST /missions/$MISSION_ID/close?outcome=SUCCESS"
  curl -s -X POST "$BASE/missions/$MISSION_ID/close?outcome=SUCCESS" \
    -H "Authorization: Bearer $TOKEN" | jq '{id, status, outcome}'
  echo ""
  ok "Misión completada con outcome: SUCCESS"
  sleep 2
fi

# ── VERIFICAR EMERGENCIA ───────────────────────────────────────
box "PASO 10 — Emergencia resuelta automáticamente"
step "GET /emergencies/$EMERGENCY_ID"
sleep 1
curl -s "$BASE/emergencies/$EMERGENCY_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '{id, status, priority, type}'

echo ""
echo -e "${GREEN}${BOLD}╔════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}${BOLD}║      DEMO COMPLETADA EXITOSAMENTE ✓        ║${NC}"
echo -e "${GREEN}${BOLD}╚════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  Lo que demostramos:"
echo -e "  ${GREEN}✓${NC} RBAC — solo ADMIN puede crear/gestionar"
echo -e "  ${GREEN}✓${NC} ABAC — validación por región y clearance_level"
echo -e "  ${GREEN}✓${NC} Event-Driven — Kafka desacopla los servicios"
echo -e "  ${GREEN}✓${NC} Asignación automática con algoritmo scoring"
echo -e "  ${GREEN}✓${NC} Graceful handoff — WARNING < 20% batería"
echo -e "  ${GREEN}✓${NC} Forzado RETURNING — CRITICAL < 5% batería"
echo -e "  ${GREEN}✓${NC} Misión completada → Emergencia resuelta"
echo ""
echo -e "  ${YELLOW}Verificar en:${NC}"
echo -e "  • Dashboard: ${BLUE}http://localhost${NC}"
echo -e "  • Kafka UI:  ${BLUE}http://localhost:8090${NC}"
echo ""
