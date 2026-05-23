#!/bin/bash
# ══════════════════════════════════════════════════════
# AeroRescue — Script de parada
# ══════════════════════════════════════════════════════

BLUE='\033[0;34m'
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${BLUE}[AeroRescue]${NC} Deteniendo todos los contenedores..."
docker compose down
echo -e "${GREEN}[OK]${NC} AeroRescue detenido"
echo ""
echo "Para eliminar volúmenes (bases de datos): docker compose down -v"
