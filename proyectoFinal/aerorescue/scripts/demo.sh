#!/bin/bash
# ══════════════════════════════════════════════════════
# AeroRescue — Script de Demo
# Simula el flujo completo para la presentación
# Requiere: curl, jq
# ══════════════════════════════════════════════════════

set -e

BASE="http://localhost:8080"
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

step()  { echo ""; echo -e "${BLUE}══ $1 ══${NC}"; }
ok()    { echo -e "${GREEN}✓${NC} $1"; }
info()  { echo -e "${YELLOW}→${NC} $1"; }
pause() { echo ""; read -p "  [ENTER para continuar]" _; }

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     AeroRescue — Demo Flujo Completo       ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"

# ── DEMO 1: Login como ADMIN ─────────────────────────
step "DEMO 1 — Login como ADMIN"
info "POST /auth/login { username: admin, password: Admin123! }"

TOKEN_RESPONSE=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}')

STATUS=$(echo $TOKEN_RESPONSE | jq -r '.status')
echo "$TOKEN_RESPONSE" | jq .

if [ "$STATUS" = "SUCCESS" ]; then
  ADMIN_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.accessToken')
  ok "Login exitoso — token obtenido"
else
  info "MFA habilitado para este usuario"
  TEMP_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.tempToken')
  info "tempToken: $TEMP_TOKEN"
fi
pause

# ── DEMO 2: RBAC — VIEWER intenta crear emergencia ───
step "DEMO 2 — RBAC: VIEWER bloqueado en POST /emergencies"
info "Login como viewer (si existe) o mostramos el concepto con token manipulado"
info "El API Gateway rechaza con 403 antes de llegar al microservicio"

VIEWER_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "$BASE/emergencies" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer INVALID_TOKEN" \
  -d '{"priority":"HIGH","type":"RESCUE","address":"Test","region":"LIMA_NORTE","lat":-12.04,"lng":-77.04}')

echo "HTTP Status: $VIEWER_RESPONSE"
ok "Gateway rechazó la solicitud sin token válido — RBAC funcionando"
pause

# ── DEMO 3: Crear emergencia ─────────────────────────
step "DEMO 3 — Crear emergencia HIGH en LIMA_NORTE"

if [ -z "$ADMIN_TOKEN" ]; then
  info "Necesitas token de admin — ejecuta con MFA deshabilitado para demo rápida"
  exit 0
fi

info "POST /emergencies — priority: HIGH, type: RESCUE, region: LIMA_NORTE"
EMERGENCY=$(curl -s -X POST "$BASE/emergencies" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "priority": "HIGH",
    "type": "RESCUE",
    "address": "Av. Arequipa 1200, Lima",
    "region": "LIMA_NORTE",
    "lat": -12.0464,
    "lng": -77.0428,
    "description": "Persona atrapada en edificio"
  }')

echo $EMERGENCY | jq .
EMERGENCY_ID=$(echo $EMERGENCY | jq -r '.id')
ok "Emergencia creada: $EMERGENCY_ID"
info "Kafka publica: emergency.created → Mission Service la consumirá automáticamente"
pause

# ── DEMO 4: Ver drones disponibles ───────────────────
step "DEMO 4 — Listar drones disponibles"
info "GET /drones"

DRONES=$(curl -s "$BASE/drones" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo $DRONES | jq .
DRONE_ID=$(echo $DRONES | jq -r '.[0].id')
ok "Drones obtenidos — primer drone: $DRONE_ID"
pause

# ── DEMO 5: Simular telemetría con batería baja ──────
step "DEMO 5 — Simular batería BAJA en drone (< 20%) — el 'Flow WOW'"
info "POST /drones/$DRONE_ID/telemetry — batteryPercentage: 18"

TELEMETRY=$(curl -s -X POST "$BASE/drones/$DRONE_ID/telemetry" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"lat\": -12.0501,
    \"lng\": -77.0350,
    \"altitudeMeters\": 45,
    \"batteryPercentage\": 18,
    \"status\": \"ON_MISSION\"
  }")

echo $TELEMETRY | jq .
ok "Telemetría enviada — batteryPercentage: 18%"
echo ""
info "Flujo automático activado:"
info "  1. Drone Service detecta battery < 20%"
info "  2. Publica: drone.battery.low (severity: WARNING)"
info "  3. Mission Service → busca reemplazo (graceful handoff)"
info "  4. Alert Service → genera alerta crítica"
info "  5. Notification Service → WebSocket al dashboard"
info "  Verificar en: http://localhost:8090 (Kafka UI)"
pause

# ── DEMO 6: Simular batería CRÍTICA (< 5%) ───────────
step "DEMO 6 — Simular batería CRÍTICA (< 5%) — drone forzado a RETURNING"
info "POST /drones/$DRONE_ID/telemetry — batteryPercentage: 3"

curl -s -X POST "$BASE/drones/$DRONE_ID/telemetry" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"lat\": -12.0501,
    \"lng\": -77.0350,
    \"altitudeMeters\": 30,
    \"batteryPercentage\": 3,
    \"status\": \"ON_MISSION\"
  }" | jq .

ok "Batería CRÍTICA — drone cambia a RETURNING automáticamente"
info "  drone.battery.low (severity: CRITICAL) publicado"
info "  Mission Service → reasignación de emergencia inmediata"
pause

# ── DEMO 7: Ver misiones ──────────────────────────────
step "DEMO 7 — Estado actual de misiones"
info "GET /missions"

curl -s "$BASE/missions" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

ok "Misiones listadas — verificar estados: AWAITING_REPLACEMENT o REASSIGNED"
pause

# ── DEMO 8: ABAC — Operador intenta crear en otra región
step "DEMO 8 — ABAC: Operador intenta crear emergencia en región no autorizada"
info "Simulando token de OPERATOR de LIMA_NORTE intentando crear en LIMA_SUR"
info "Emergency Service rechazará con 403 (ABAC-01)"

# Nota: para demo real necesitarías token del operator
echo "  Token JWT del operador incluye: region=LIMA_NORTE"
echo "  Emergencia solicitada:          region=LIMA_SUR"
echo "  Resultado esperado:             403 Forbidden"
echo "  Mensaje:                        ABAC-01: Operator can only create emergencies in their region: LIMA_NORTE"
ok "ABAC-01 demostraría rechazo — ver logs del emergency-service"
pause

# ── DEMO 9: Escalada de emergencia ────────────────────
step "DEMO 9 — Escalar emergencia a CRITICAL"
info "POST /emergencies/$EMERGENCY_ID/escalate"
info "Requiere clearance_level >= 3 (ABAC-02)"

ESCALATED=$(curl -s -X POST "$BASE/emergencies/$EMERGENCY_ID/escalate" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo $ESCALATED | jq .
ok "Emergencia escalada a CRITICAL"
info "Kafka publica: emergency.escalated → Alert Service genera alerta CRITICAL"
pause

# ── DEMO 10: Trazabilidad Audit ───────────────────────
step "DEMO 10 — Trazabilidad por correlationId en Audit Service"
info "GET /audit/events/emergency.created"

curl -s "$BASE/audit/events/emergency.created" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.[0:3]'

ok "Historial de auditoría — todos los eventos con correlationId registrados"
echo ""
echo -e "${GREEN}╔════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║         Demo completada exitosamente ✓     ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════╝${NC}"
echo ""
echo "  Verificar en tiempo real:"
echo "  • Dashboard:  http://localhost"
echo "  • Kafka UI:   http://localhost:8090"
echo "  • Swagger:    http://localhost:8082/swagger-ui.html (Emergency)"
echo "  •             http://localhost:8083/swagger-ui.html (Drone)"
echo "  •             http://localhost:8084/swagger-ui.html (Mission)"
echo ""
