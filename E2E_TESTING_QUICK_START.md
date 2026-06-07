# GUÍA RÁPIDA - TESTING E2E AXISERP

**Versión:** 1.0  
**Fecha:** 2026-06-07  
**Audiencia:** QA Engineers, Developers

---

## INICIO RÁPIDO (5 minutos)

### 1. Verificar Ambiente

```bash
# Verificar que Docker Compose está corriendo
docker-compose ps

# Si no está corriendo:
docker-compose up -d

# Esperar ~30 segundos para que servicios inicien
sleep 30

# Verificar servicios
bash scripts/check-services.sh
```

### 2. Ejecutar Tests E2E Completos

```bash
# Hacer script ejecutable
chmod +x scripts/run-e2e-complete.sh

# Ejecutar tests (requiere usuario ADMIN preexistente)
ADMIN_EMAIL="admin@example.com" \
ADMIN_PASSWORD="AdminPass123!" \
bash scripts/run-e2e-complete.sh
```

### 3. Validar Datos en Bases de Datos

```bash
# Verificar integridad de datos
chmod +x scripts/validate-database.sh
bash scripts/validate-database.sh
```

---

## OPCIÓN A: Testing Manual con cURL

### Login y Obtener Token

```bash
# 1. Login
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminPass123!"
  }')

ADMIN_TOKEN=$(echo $RESPONSE | jq -r '.data.accessToken')
echo "Token: $ADMIN_TOKEN"

# Guardar en variable de ambiente para facilitar reutilización
export ADMIN_TOKEN
```

### Flujo 1: Crear Producto

```bash
# Obtener categoría
CATEGORY=$(curl -s -X GET http://localhost:8080/api/v1/categorias?page=1&size=1 \
  -H "Authorization: Bearer $ADMIN_TOKEN")
CATEGORY_ID=$(echo $CATEGORY | jq -r '.data[0].id')

# Crear producto
PRODUCT=$(curl -s -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"nombre\": \"Test Laptop\",
    \"codigo\": \"PROD-E2E-$(date +%s)\",
    \"descripcion\": \"Test product\",
    \"precioVenta\": 2500000,
    \"costo\": 1500000,
    \"categoriaId\": \"$CATEGORY_ID\"
  }")

PRODUCT_ID=$(echo $PRODUCT | jq -r '.data.id')
echo "Producto creado: $PRODUCT_ID"

# Guardar para siguiente flujo
export PRODUCT_ID
```

### Flujo 2: Inicializar Inventario

```bash
curl -X POST http://localhost:8080/api/v1/inventory/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"productoId\": \"$PRODUCT_ID\",
    \"stockInicial\": 100,
    \"stockMinimo\": 10,
    \"stockMaximo\": 500
  }" | jq '.data | {stockActual, stockMinimo, stockMaximo}'
```

### Flujo 3: Crear Venta

```bash
# Obtener cliente
CUSTOMER=$(curl -s -X GET http://localhost:8080/api/v1/clientes?page=1&size=1 \
  -H "Authorization: Bearer $ADMIN_TOKEN")
CUSTOMER_ID=$(echo $CUSTOMER | jq -r '.data[0].id')

# Crear venta
SALE=$(curl -s -X POST http://localhost:8080/api/v1/sales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"clienteId\": \"$CUSTOMER_ID\",
    \"items\": [{
      \"productoId\": \"$PRODUCT_ID\",
      \"cantidad\": 20,
      \"precioUnitario\": 2500000
    }]
  }")

SALE_ID=$(echo $SALE | jq -r '.data.id')
echo "Venta creada: $SALE_ID"

# Guardar para siguiente flujo
export SALE_ID
```

### Flujo 4: Confirmar Venta

```bash
curl -X PATCH http://localhost:8080/api/v1/sales/$SALE_ID/confirmar \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data | {id, estado, total}'
```

### Flujo 5: Pagar Venta

```bash
curl -X PATCH http://localhost:8080/api/v1/sales/$SALE_ID/pagar \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data | {id, estado, total}'
```

### Flujo 6: Obtener Reporte

```bash
curl -X GET "http://localhost:8080/api/v1/reports/ventas?page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data | length'
```

### Flujo 7: Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Verificar que token está inválido
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer $ADMIN_TOKEN"
# Esperado: 401 Unauthorized
```

---

## OPCIÓN B: Testing con Postman

### Pasos

1. **Importar Colección**
   - Abrir Postman
   - Click en "Import"
   - Seleccionar archivo: `postman/AxisERP-E2E-Tests.postman_collection.json`

2. **Configurar Ambiente**
   - Crear nuevo Environment o usar "AxisERP-Dev"
   - Variables requeridas:
     - `BASE_URL`: http://localhost:8080
     - `ADMIN_EMAIL`: admin@example.com
     - `ADMIN_PASSWORD`: AdminPass123!

3. **Ejecutar Tests**
   - Opción A: Ejecutar requests manualmente en orden
   - Opción B: Usar "Run Collection" para automatizar

4. **Ver Resultados**
   - Pestaña "Test Results" muestra pass/fail de cada test
   - Pestaña "Console" muestra logs detallados

---

## OPCIÓN C: Testing Automatizado (CI/CD)

### GitHub Actions / Pipeline

```bash
# Script para ejecutar en CI/CD
#!/bin/bash
set -e

# 1. Esperar a que servicios estén listos
until curl -s http://localhost:8080/health | jq -e '.status == "UP"' > /dev/null 2>&1; do
  echo "Waiting for services..."
  sleep 5
done

# 2. Ejecutar E2E tests
ADMIN_EMAIL="admin@example.com" \
ADMIN_PASSWORD="AdminPass123!" \
bash scripts/run-e2e-complete.sh

# 3. Validar datos
bash scripts/validate-database.sh

echo "✓ All E2E tests passed!"
```

---

## CONSULTAS COMUNES

### ¿Cómo obtener lista de usuarios?

```bash
curl -X GET "http://localhost:8080/api/v1/usuarios?page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data'
```

### ¿Cómo obtener detalles de una venta?

```bash
curl -X GET http://localhost:8080/api/v1/sales/$SALE_ID \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data'
```

### ¿Cómo ver movimientos de inventario?

```bash
curl -X GET "http://localhost:8080/api/v1/inventory/movements?productoId=$PRODUCT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data'
```

### ¿Cómo exportar reporte en PDF?

```bash
curl -X POST http://localhost:8080/api/v1/reports/export \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "reportType": "VENTAS",
    "formato": "PDF",
    "filtros": {
      "fechaInicio": "2026-06-01",
      "fechaFin": "2026-06-30"
    }
  }' \
  -o reporte_ventas.pdf
```

### ¿Cómo ver logs de un servicio?

```bash
# Ver logs en tiempo real
docker-compose logs -f catalog-service

# Ver últimas 100 líneas
docker-compose logs --tail=100 catalog-service
```

### ¿Cómo conectarme a la base de datos?

```bash
# Acceder a PostgreSQL
docker-compose exec postgres psql -U postgres -d catalog_db

# Query de ejemplo
SELECT * FROM products LIMIT 5;
\q  # Salir
```

---

## RESOLUCIÓN DE PROBLEMAS

### Error: "Connection refused"

**Causa:** Servicios no están corriendo

**Solución:**
```bash
docker-compose up -d
sleep 30
docker-compose ps
```

### Error: "401 Unauthorized"

**Causa:** Token expirado o inválido

**Solución:**
```bash
# Obtener nuevo token
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminPass123!"
  }')

ADMIN_TOKEN=$(echo $RESPONSE | jq -r '.data.accessToken')
export ADMIN_TOKEN
```

### Error: "Product not found"

**Causa:** Producto no existe o fue eliminado

**Solución:**
```bash
# Verificar productos disponibles
curl -X GET "http://localhost:8080/api/v1/productos?page=1&size=10&includeInactive=false" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data[] | {id, nombre}'
```

### Error: "Insufficient stock"

**Causa:** No hay suficiente inventario

**Solución:**
```bash
# Ver stock actual
curl -X GET http://localhost:8080/api/v1/inventory/$PRODUCT_ID \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data.stockActual'

# Aumentar stock
curl -X POST http://localhost:8080/api/v1/inventory/entrada \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"productoId\": \"$PRODUCT_ID\",
    \"cantidad\": 100,
    \"razon\": \"Reabastecimiento\"
  }"
```

### Error: "Database connection failed"

**Causa:** Bases de datos no están disponibles

**Solución:**
```bash
# Verificar estado de PostgreSQL
docker-compose logs postgres | tail -20

# Reiniciar servicios
docker-compose down
docker-compose up -d
sleep 30
```

---

## DOCUMENTOS RELACIONADOS

| Documento | Propósito |
|-----------|----------|
| `E2E_TESTING_PLAN.md` | Plan completo con todos los detalles (este documento) |
| `postman/AxisERP-E2E-Tests.postman_collection.json` | Colección de tests para Postman |
| `scripts/run-e2e-complete.sh` | Script automatizado de todos los flujos |
| `scripts/check-services.sh` | Verificar que servicios están corriendo |
| `scripts/validate-database.sh` | Validar integridad de datos |

---

## CHECKLIST PRE-TESTING

Antes de ejecutar tests, verificar:

- [ ] Docker Desktop está corriendo
- [ ] `docker-compose ps` muestra todos los servicios UP
- [ ] `.env` contiene valores válidos de Supabase
- [ ] Usuario ADMIN existe en auth_db
- [ ] Categoría existe en catalog_db
- [ ] Cliente existe en sales_db
- [ ] API Gateway responde en http://localhost:8080/health
- [ ] PostgreSQL accesible en localhost:5432

---

## MÉTRICAS DE ÉXITO

Después de ejecutar todos los flujos:

✓ 8/8 flujos completados exitosamente  
✓ 50+ test cases pasaron  
✓ Integridad de datos validada  
✓ Tokens JWT generados y revocados correctamente  
✓ Inventario actualizado correctamente  
✓ Reportes generados sin errores  
✓ Audit logs registran todas las operaciones  
✓ Ningún error HTTP 5xx  

---

## CONTACTO Y SOPORTE

Para reportar issues con los tests E2E:

1. Incluir output del test que falló
2. Incluir logs de servicios relevantes (`docker-compose logs <service>`)
3. Incluir estado de base de datos (`bash scripts/validate-database.sh`)
4. Crear issue en repositorio con label `e2e-testing`

---

**Última actualización:** 2026-06-07  
**Responsable:** QA Team AxisERP
