#!/bin/bash
# check-services.sh
# Verifica que todos los servicios de AxisERP están corriendo

set -e

echo "========================================"
echo "VERIFICANDO SERVICIOS - AXISERP"
echo "========================================"

# Definir servicios y puertos
declare -A SERVICES=(
  ["API Gateway"]="8080"
  ["Auth Service"]="8081"
  ["Catalog Service"]="8082"
  ["Inventory Service"]="8083"
  ["Sales Service"]="8084"
  ["Report Service"]="8085"
  ["Purchase Service"]="8086"
)

# Verificar cada servicio
FAILED=0
for SERVICE in "${!SERVICES[@]}"; do
  PORT=${SERVICES[$SERVICE]}
  echo -n "Checking $SERVICE (port $PORT)... "

  if curl -s -f http://localhost:$PORT/health > /dev/null 2>&1; then
    echo "✓ RUNNING"
  else
    echo "✗ DOWN"
    FAILED=$((FAILED + 1))
  fi
done

echo ""
if [ $FAILED -eq 0 ]; then
  echo "✓ All services are running!"
  exit 0
else
  echo "✗ $FAILED service(s) are down"
  echo ""
  echo "Run: docker compose up -d"
  exit 1
fi
