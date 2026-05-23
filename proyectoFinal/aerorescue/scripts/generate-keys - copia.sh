#!/bin/bash
# ═══════════════════════════════════════════════════
# Genera par de claves RS256 para JWT
# Ejecutar UNA sola vez antes de levantar el proyecto
# ═══════════════════════════════════════════════════

set -e

KEYS_DIR="./keys"
mkdir -p "$KEYS_DIR"

echo "Generando par de claves RS256..."

# Generar clave privada
openssl genrsa -out "$KEYS_DIR/private.pem" 2048

# Extraer clave pública
openssl rsa -in "$KEYS_DIR/private.pem" -pubout -out "$KEYS_DIR/public.pem"

# Convertir a formato PKCS8 (requerido por Spring Security)
openssl pkcs8 -topk8 -inform PEM -outform PEM \
  -in "$KEYS_DIR/private.pem" \
  -out "$KEYS_DIR/private_pkcs8.pem" \
  -nocrypt

echo "Claves generadas en $KEYS_DIR/"
echo ""
echo "Copiando a resources de auth-service..."
cp "$KEYS_DIR/private_pkcs8.pem" ./auth-service/src/main/resources/certs/private.pem
cp "$KEYS_DIR/public.pem"         ./auth-service/src/main/resources/certs/public.pem
cp "$KEYS_DIR/public.pem"         ./api-gateway/src/main/resources/certs/public.pem

# Copiar clave pública a todos los servicios que validan JWT
for service in emergency-service drone-service mission-service alert-service audit-service; do
  mkdir -p "./$service/src/main/resources/certs"
  cp "$KEYS_DIR/public.pem" "./$service/src/main/resources/certs/public.pem"
  echo "  → $service/src/main/resources/certs/public.pem"
done

echo ""
echo "Listo. Las claves están en ./keys/ y distribuidas en cada servicio."
echo "IMPORTANTE: No subas ./keys/ ni los archivos .pem a Git."
