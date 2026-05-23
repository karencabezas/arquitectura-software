#!/bin/bash
# ═══════════════════════════════════════════════════
# Crea todos los topics de Kafka para AeroRescue
# Ejecutar después de que Kafka esté corriendo
# ═══════════════════════════════════════════════════

KAFKA_CONTAINER="aerorescue-kafka"
BOOTSTRAP="kafka:29092"

echo "Creando topics de Kafka..."

topics=(
  "emergency.created"
  "emergency.updated"
  "emergency.escalated"
  "drone.telemetry"
  "drone.battery.low"
  "drone.offline"
  "drone.status.changed"
  "mission.assigned"
  "mission.reassigned"
  "mission.completed"
  "alert.created"
)

for topic in "${topics[@]}"; do
  docker exec "$KAFKA_CONTAINER" \
    kafka-topics --create \
    --bootstrap-server "$BOOTSTRAP" \
    --topic "$topic" \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists
  echo "  ✓ $topic"
done

echo ""
echo "Topics creados. Verifica en http://localhost:8090 (Kafka UI)"
