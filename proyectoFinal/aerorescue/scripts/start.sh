#!/bin/bash
# ══════════════════════════════════════════════════════
# AeroRescue — Script de arranque completo
# Ejecutar desde la raíz del monorepo (aerorescue/)
# ══════════════════════════════════════════════════════

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log()  { echo -e "${BLUE}[AeroRescue]${NC} $1"; }
ok()   { echo -e "${GREEN}[OK]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
err()  { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

echo ""
echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║        AeroRescue — Full Startup       ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

# ── 1. Verificar herramientas ────────────────────────
log "Verificando herramientas..."
command -v docker   >/dev/null 2>&1 || err "Docker no encontrado"
command -v mvn      >/dev/null 2>&1 || err "Maven no encontrado"
command -v openssl  >/dev/null 2>&1 || err "OpenSSL no encontrado"
ok "Docker, Maven y OpenSSL disponibles"

# ── 2. Generar claves JWT si no existen ──────────────
if [ ! -f "./keys/private_pkcs8.pem" ]; then
  log "Generando claves JWT RS256..."
  chmod +x ./scripts/generate-keys.sh
  ./scripts/generate-keys.sh
  ok "Claves JWT generadas"
else
  ok "Claves JWT ya existen — omitiendo"
fi

# ── 3. Levantar infraestructura ──────────────────────
log "Levantando infraestructura (Kafka + bases de datos)..."
docker compose up -d \
  zookeeper kafka kafka-ui \
  auth-db emergency-db drone-db \
  mission-db 

log "Esperando que Kafka esté listo (30s)..."
sleep 30

# ── 4. Crear topics Kafka ────────────────────────────
log "Creando topics de Kafka..."
chmod +x ./scripts/create-topics.sh
./scripts/create-topics.sh
ok "Topics creados"

# ── 5. Compilar backend ──────────────────────────────
log "Compilando todos los microservicios..."
mvn clean package -DskipTests -q
ok "Backend compilado"

# ── 6. Levantar microservicios ───────────────────────
log "Levantando microservicios..."
docker compose up -d \
  auth-service \
  api-gateway \
  emergency-service \
  drone-service \
  mission-service \
  notification-service 

log "Esperando que los servicios estén listos (40s)..."
sleep 40

# ── 7. Levantar frontend ─────────────────────────────
log "Levantando frontend..."
docker compose up -d frontend

# ── 8. Status final ──────────────────────────────────
echo ""
echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║         AeroRescue Levantado ✓         ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
echo ""
echo -e "  🌐  Frontend:        ${BLUE}http://localhost${NC}"
echo -e "  🔌  API Gateway:     ${BLUE}http://localhost:8080${NC}"
echo -e "  📊  Kafka UI:        ${BLUE}http://localhost:8090${NC}"
echo ""
echo -e "  Usuarios demo:"
echo -e "    admin              / Admin123!"
echo -e "    operator.lima.norte / Admin123!"
echo -e "    supervisor.lima    / Admin123!"
echo ""
docker compose ps
