#!/bin/bash
# ══════════════════════════════════════════════════════
# AeroRescue — Health Check de todos los servicios
# ══════════════════════════════════════════════════════

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

ok()   { echo -e "  ${GREEN}✓${NC} $1"; }
fail() { echo -e "  ${RED}✗${NC} $1"; }
warn() { echo -e "  ${YELLOW}⚠${NC} $1"; }

check_http() {
  local name=$1
  local url=$2
  local code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$url" 2>/dev/null)
  if [ "$code" = "200" ] || [ "$code" = "401" ]; then
    ok "$name ($url) — HTTP $code"
  else
    fail "$name ($url) — HTTP $code (esperado 200 o 401)"
  fi
}

check_kafka_topic() {
  local topic=$1
  local exists=$(docker exec aerorescue-kafka \
    kafka-topics --list --bootstrap-server localhost:9092 2>/dev/null | grep -c "^${topic}$" || true)
  if [ "$exists" = "1" ]; then
    ok "Topic: $topic"
  else
    fail "Topic faltante: $topic"
  fi
}

echo ""
echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     AeroRescue — Health Check          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

# ── Infraestructura ──────────────────────────────────
echo -e "${BLUE}Infrastructure:${NC}"
check_http "Kafka UI"     "http://localhost:8090"
check_http "Auth DB"      "http://localhost:5432" || true  # postgres no responde HTTP

echo ""
echo -e "${BLUE}Microservices:${NC}"
check_http "Auth Service"         "http://localhost:8081/api-docs"
check_http "API Gateway"          "http://localhost:8080/auth/login"
check_http "Emergency Service"    "http://localhost:8082/api-docs"
check_http "Drone Service"        "http://localhost:8083/api-docs"
check_http "Mission Service"      "http://localhost:8084/api-docs"
check_http "Alert Service"        "http://localhost:8085/api-docs"
check_http "Notification Service" "http://localhost:8086"
check_http "Audit Service"        "http://localhost:8087/api-docs"

echo ""
echo -e "${BLUE}Frontend:${NC}"
check_http "Angular App" "http://localhost"

echo ""
echo -e "${BLUE}Kafka Topics:${NC}"
topics=(
  "emergency.created" "emergency.updated" "emergency.escalated"
  "drone.telemetry" "drone.battery.low" "drone.offline" "drone.status.changed"
  "mission.assigned" "mission.reassigned" "mission.completed"
  "alert.created"
)
for topic in "${topics[@]}"; do
  check_kafka_topic "$topic"
done

echo ""
echo -e "${BLUE}Docker Containers:${NC}"
docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}" 2>/dev/null || \
  docker compose ps

echo ""
