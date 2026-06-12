#!/bin/bash
# validate-database.sh
# Valida la integridad de datos en las bases de datos

set -e

echo "========================================"
echo "VALIDANDO INTEGRIDAD DE DATOS"
echo "========================================"

# Verificar que Docker Compose está corriendo
if ! docker compose ps | grep -q "Up"; then
  echo "Error: Docker Compose services are not running"
  exit 1
fi

# Función para ejecutar query
run_query() {
  local db=$1
  local query=$2
  docker compose exec -T postgres psql -U postgres -d "$db" -c "$query"
}

echo ""
echo "--- USERS (auth_db) ---"
run_query "auth_db" "SELECT COUNT(*) as total, COUNT(CASE WHEN estado='ACTIVO' THEN 1 END) as active FROM users;"

echo ""
echo "--- CATEGORIES (catalog_db) ---"
run_query "catalog_db" "SELECT COUNT(*) as total, COUNT(CASE WHEN estado='ACTIVA' THEN 1 END) as active FROM categories;"

echo ""
echo "--- PRODUCTS (catalog_db) ---"
run_query "catalog_db" "SELECT COUNT(*) as total, COUNT(CASE WHEN estado='ACTIVO' THEN 1 END) as active FROM products;"

echo ""
echo "--- INVENTORY (inventory_db) ---"
run_query "inventory_db" "SELECT COUNT(*) as total FROM inventory;"

echo ""
echo "--- INVENTORY MOVEMENTS (inventory_db) ---"
run_query "inventory_db" "SELECT tipo, COUNT(*) as cantidad FROM movements GROUP BY tipo ORDER BY tipo;"

echo ""
echo "--- SALES (sales_db) ---"
run_query "sales_db" "SELECT estado, COUNT(*) as cantidad FROM sales GROUP BY estado ORDER BY estado;"

echo ""
echo "--- PURCHASES (purchase_db) ---"
run_query "purchase_db" "SELECT estado, COUNT(*) as cantidad FROM purchases GROUP BY estado ORDER BY estado;"

echo ""
echo "--- AUDIT LOGS (auth_db) ---"
run_query "auth_db" "SELECT action, COUNT(*) as cantidad FROM audit_logs GROUP BY action ORDER BY action LIMIT 10;"

echo ""
echo "--- RECENT ORDERS ---"
echo "Latest 5 sales:"
run_query "sales_db" "SELECT id, codigo, estado, total FROM sales ORDER BY created_at DESC LIMIT 5;"

echo ""
echo "Latest 5 purchases:"
run_query "purchase_db" "SELECT id, codigo, estado, total FROM purchases ORDER BY created_at DESC LIMIT 5;"

echo ""
echo "✓ Database validation completed!"
