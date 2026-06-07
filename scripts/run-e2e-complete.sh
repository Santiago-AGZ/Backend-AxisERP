#!/bin/bash
# run-e2e-complete.sh
# Ejecuta todos los flujos E2E de AxisERP

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Variables globales
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@example.com}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-AdminPass123!}"
API_GATEWAY="http://localhost:8080"
ADMIN_TOKEN=""
TEST_PASSED=0
TEST_FAILED=0

# Función para imprimir encabezados
print_header() {
  echo ""
  echo -e "${YELLOW}========================================"
  echo "TESTING E2E FLOW - AXISERP"
  echo "========================================${NC}"
}

# Función para imprimir tests
print_test() {
  echo -e "\n${YELLOW}[$1]${NC} $2"
}

# Función para imprimir éxito
print_success() {
  echo -e "${GREEN}✓ $1${NC}"
  TEST_PASSED=$((TEST_PASSED + 1))
}

# Función para imprimir error
print_error() {
  echo -e "${RED}✗ $1${NC}"
  TEST_FAILED=$((TEST_FAILED + 1))
}

# Función para imprimir resumen
print_summary() {
  echo ""
  echo -e "${YELLOW}========================================"
  echo "TEST SUMMARY"
  echo "========================================${NC}"
  echo -e "Passed: ${GREEN}$TEST_PASSED${NC}"
  echo -e "Failed: ${RED}$TEST_FAILED${NC}"
  echo -e "Total:  $((TEST_PASSED + TEST_FAILED))"

  if [ $TEST_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}ALL TESTS PASSED!${NC}"
    exit 0
  else
    echo -e "\n${RED}SOME TESTS FAILED!${NC}"
    exit 1
  fi
}

# ==========================================
# FLUJO 1: LOGIN
# ==========================================
flow_login() {
  print_test "1" "AUTHENTICATION AND LOGIN"

  local response=$(curl -s -X POST "$API_GATEWAY/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{
      \"email\": \"$ADMIN_EMAIL\",
      \"password\": \"$ADMIN_PASSWORD\"
    }")

  ADMIN_TOKEN=$(echo $response | jq -r '.data.accessToken // empty')

  if [ -z "$ADMIN_TOKEN" ]; then
    print_error "Login failed"
    echo "Response: $response"
    return 1
  fi

  print_success "Login successful (token: ${ADMIN_TOKEN:0:20}...)"
}

# ==========================================
# FLUJO 2: OBTENER USUARIO AUTENTICADO
# ==========================================
flow_get_user_info() {
  print_test "2" "GET USER INFO"

  local response=$(curl -s -X GET "$API_GATEWAY/api/v1/auth/me" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

  local success=$(echo $response | jq -r '.success // false')

  if [ "$success" == "true" ]; then
    local user_name=$(echo $response | jq -r '.data.nombre // .data.name // empty')
    print_success "User info retrieved: $user_name"
  else
    print_error "Failed to get user info"
    echo "Response: $response"
    return 1
  fi
}

# ==========================================
# FLUJO 3: OBTENER O CREAR CATEGORÍA
# ==========================================
flow_get_category() {
  print_test "3" "GET OR CREATE CATEGORY"

  # Intentar obtener categoría existente
  local response=$(curl -s -X GET "$API_GATEWAY/api/v1/categorias?page=1&size=1" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

  CATEGORY_ID=$(echo $response | jq -r '.data[0].id // empty')

  if [ -z "$CATEGORY_ID" ]; then
    print_error "No categories found and cannot create"
    return 1
  fi

  print_success "Category found: $CATEGORY_ID"
}

# ==========================================
# FLUJO 4: CREAR PRODUCTO
# ==========================================
flow_create_product() {
  print_test "4" "CREATE PRODUCT"

  local timestamp=$(date +%s)
  local product_code="PROD-E2E-$timestamp"

  local response=$(curl -s -X POST "$API_GATEWAY/api/v1/productos" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{
      \"nombre\": \"E2E Test Product\",
      \"codigo\": \"$product_code\",
      \"descripcion\": \"Producto de prueba E2E\",
      \"precioVenta\": 100000,
      \"costo\": 50000,
      \"categoriaId\": \"$CATEGORY_ID\"
    }")

  PRODUCT_ID=$(echo $response | jq -r '.data.id // empty')

  if [ -z "$PRODUCT_ID" ]; then
    print_error "Failed to create product"
    echo "Response: $response"
    return 1
  fi

  print_success "Product created: $PRODUCT_ID"
}

# ==========================================
# FLUJO 5: INICIALIZAR INVENTARIO
# ==========================================
flow_initialize_inventory() {
  print_test "5" "INITIALIZE INVENTORY"

  local response=$(curl -s -X POST "$API_GATEWAY/api/v1/inventory/initialize" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{
      \"productoId\": \"$PRODUCT_ID\",
      \"stockInicial\": 100,
      \"stockMinimo\": 10,
      \"stockMaximo\": 500
    }")

  local stock=$(echo $response | jq -r '.data.stockActual // empty')

  if [ -z "$stock" ] || [ "$stock" != "100" ]; then
    print_error "Failed to initialize inventory"
    echo "Response: $response"
    return 1
  fi

  print_success "Inventory initialized with stock: $stock"
}

# ==========================================
# FLUJO 6: OBTENER CLIENTE
# ==========================================
flow_get_customer() {
  print_test "6" "GET CUSTOMER"

  local response=$(curl -s -X GET "$API_GATEWAY/api/v1/clientes?page=1&size=1" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

  CUSTOMER_ID=$(echo $response | jq -r '.data[0].id // empty')

  if [ -z "$CUSTOMER_ID" ]; then
    print_error "No customers found"
    return 1
  fi

  print_success "Customer found: $CUSTOMER_ID"
}

# ==========================================
# FLUJO 7: CREAR VENTA
# ==========================================
flow_create_sale() {
  print_test "7" "CREATE SALE"

  local response=$(curl -s -X POST "$API_GATEWAY/api/v1/sales" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{
      \"clienteId\": \"$CUSTOMER_ID\",
      \"items\": [{
        \"productoId\": \"$PRODUCT_ID\",
        \"cantidad\": 10,
        \"precioUnitario\": 100000
      }]
    }")

  SALE_ID=$(echo $response | jq -r '.data.id // empty')

  if [ -z "$SALE_ID" ]; then
    print_error "Failed to create sale"
    echo "Response: $response"
    return 1
  fi

  print_success "Sale created: $SALE_ID"
}

# ==========================================
# FLUJO 8: CONFIRMAR VENTA
# ==========================================
flow_confirm_sale() {
  print_test "8" "CONFIRM SALE"

  local response=$(curl -s -X PATCH "$API_GATEWAY/api/v1/sales/$SALE_ID/confirmar" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

  local estado=$(echo $response | jq -r '.data.estado // empty')

  if [ "$estado" != "CONFIRMADA" ]; then
    print_error "Failed to confirm sale (estado: $estado)"
    echo "Response: $response"
    return 1
  fi

  print_success "Sale confirmed with estado: $estado"
}

# ==========================================
# FLUJO 9: OBTENER REPORTE
# ==========================================
flow_get_report() {
  print_test "9" "GET SALES REPORT"

  local response=$(curl -s -X GET "$API_GATEWAY/api/v1/reports/ventas?page=1&size=10" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

  local success=$(echo $response | jq -r '.success // false')

  if [ "$success" != "true" ]; then
    print_error "Failed to get report"
    echo "Response: $response"
    return 1
  fi

  print_success "Sales report retrieved"
}

# ==========================================
# FLUJO 10: LOGOUT
# ==========================================
flow_logout() {
  print_test "10" "LOGOUT"

  local response=$(curl -s -X POST "$API_GATEWAY/api/v1/auth/logout" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

  local success=$(echo $response | jq -r '.success // false')

  if [ "$success" != "true" ]; then
    print_error "Failed to logout"
    echo "Response: $response"
    return 1
  fi

  print_success "Logout successful"
}

# ==========================================
# FLUJO 11: VERIFICAR TOKEN INVALIDADO
# ==========================================
flow_verify_token_invalidated() {
  print_test "11" "VERIFY TOKEN INVALIDATED"

  local response=$(curl -s -X GET "$API_GATEWAY/api/v1/auth/me" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -w "%{http_code}")

  # Extraer status code (últimos 3 caracteres)
  local status_code=${response: -3}

  if [ "$status_code" == "401" ]; then
    print_success "Token invalidated correctly (401)"
  else
    print_error "Token should be invalid (got $status_code)"
    return 1
  fi
}

# ==========================================
# MAIN
# ==========================================
main() {
  print_header

  # Verificar servicios
  echo -e "\nChecking services..."
  if ! bash "$(dirname $0)/check-services.sh"; then
    echo "Services not running!"
    exit 1
  fi

  # Ejecutar flujos
  flow_login || return 1
  flow_get_user_info || return 1
  flow_get_category || return 1
  flow_create_product || return 1
  flow_initialize_inventory || return 1
  flow_get_customer || return 1
  flow_create_sale || return 1
  flow_confirm_sale || return 1
  flow_get_report || return 1
  flow_logout || return 1
  flow_verify_token_invalidated || return 1

  print_summary
}

# Ejecutar main
main "$@"
