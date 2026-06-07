# PLAN DE TESTING E2E - AXISERP PLATFORM

**Fecha:** 2026-06-07  
**Versión:** 1.0  
**Objetivo:** Validar flujos completos de usuario en ambiente integrado (Docker Compose)

---

## TABLA DE CONTENIDOS

1. [Descripción General](#descripción-general)
2. [Ambiente de Testing](#ambiente-de-testing)
3. [Flujos E2E Definidos](#flujos-e2e-definidos)
4. [Precondiciones Globales](#precondiciones-globales)
5. [Comandos de Setup](#comandos-de-setup)
6. [Flujos Detallados](#flujos-detallados)
7. [Scripts de Validación](#scripts-de-validación)
8. [Reporte de Resultados](#reporte-de-resultados)

---

## DESCRIPCIÓN GENERAL

Este plan define 8 flujos E2E críticos que cubren el ciclo de vida completo de AxisERP:

| Flujo | Servicio Principal | Rol Requerido | Criticidad |
|-------|-------------------|---------------|-----------|
| 1. Registro de Usuario | Auth Service | - | CRÍTICO |
| 2. Autenticación y Login | Auth Service | - | CRÍTICO |
| 3. Crear Producto (Catálogo) | Catalog Service | ADMIN/INVENTARIO | CRÍTICO |
| 4. Registrar Inventario | Inventory Service | ADMIN/INVENTARIO | CRÍTICO |
| 5. Crear Pedido de Compra | Purchase Service | ADMIN/INVENTARIO | ALTO |
| 6. Crear Venta | Sales Service | ADMIN/VENDEDOR | CRÍTICO |
| 7. Consultar Reporte | Report Service | ADMIN/VENDEDOR | ALTO |
| 8. Logout y Cierre de Sesión | Auth Service | - | CRÍTICO |

---

## AMBIENTE DE TESTING

### Tecnología Stack

```
Backend Services:
- Java 21 + Spring Boot 3.5.x
- PostgreSQL (6 bases de datos, una por servicio)
- RabbitMQ (asincronía de eventos)
- Docker Compose (orquestación)

API Gateway:
- Puerto 8080 (punto de entrada)

Servicios Internos:
- auth-service:8081
- catalog-service:8082
- inventory-service:8083
- sales-service:8084
- report-service:8085
- purchase-service:8086

Autenticación:
- JWT ES256 (Supabase JWKS)
- Roles: ADMIN, VENDEDOR, INVENTARIO
- Estados de Usuario: PENDIENTE, ACTIVO, INACTIVO, ELIMINADO
```

### Configuración de Ambiente

```bash
# .env debe contener:
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_JWT_ISSUER=https://your-project.supabase.co
SUPABASE_SERVICE_ROLE_KEY=<your-service-role-key>
SUPABASE_ANON_KEY=<your-anon-key>
JWT_SECRET=<your-jwt-secret>
INTERNAL_API_KEY=<your-internal-api-key>

# URLs de Bases de Datos (PostgreSQL)
AUTH_DB_URL=postgresql://user:pass@auth-db:5432/auth_db
CATALOG_DB_URL=postgresql://user:pass@catalog-db:5432/catalog_db
INVENTORY_DB_URL=postgresql://user:pass@inventory-db:5432/inventory_db
SALES_DB_URL=postgresql://user:pass@sales-db:5432/sales_db
PURCHASE_DB_URL=postgresql://user:pass@purchase-db:5432/purchase_db
REPORT_DB_URL=postgresql://user:pass@report-db:5432/report_db
```

---

## FLUJOS E2E DEFINIDOS

### FLUJO 1: REGISTRO DE USUARIO

**Objetivo:** Validar creación de nuevo usuario y estado inicial correcto.

#### Pre-condiciones
- [ ] Supabase está configurado y accesible
- [ ] Auth Service está corriendo (puerto 8081)
- [ ] Base de datos auth_db está limpia o preparada
- [ ] No existe usuario previo con el correo a registrar

#### Pasos
1. **POST** `/api/v1/usuarios` (requiere autenticación ADMIN)
   ```json
   {
     "nombre": "Juan Pérez",
     "correo": "juan.perez@example.com",
     "rol": "VENDEDOR"
   }
   ```

#### Expected Results
- [ ] HTTP Status: 201 (CREATED)
- [ ] Response contiene:
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid-generado",
      "nombre": "Juan Pérez",
      "correo": "juan.perez@example.com",
      "rol": "VENDEDOR",
      "estado": "PENDIENTE",
      "createdAt": "2026-06-07T02:00:00Z"
    }
  }
  ```
- [ ] Usuario tiene estado inicial: PENDIENTE
- [ ] Rol asignado correctamente: VENDEDOR

#### Post-condiciones
- [ ] Usuario registrado en auth_db
- [ ] Email de confirmación enviado (si aplica)
- [ ] Audit log creado para la creación

#### Validaciones de Base de Datos

```sql
-- En auth_db
SELECT * FROM users WHERE correo = 'juan.perez@example.com';
-- Validar: id (UUID), nombre, correo, rol = 'VENDEDOR', estado = 'PENDIENTE'
-- Validar: created_by, created_at, updated_at

-- Verificar audit log
SELECT * FROM audit_logs 
WHERE user_id = '<user-id>' 
AND action = 'USER_CREATED' 
ORDER BY created_at DESC LIMIT 1;
```

#### Comandos de Ejecución

```bash
# Test 1.1: Crear usuario VENDEDOR
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nombre": "Juan Pérez",
    "correo": "juan.perez@example.com",
    "rol": "VENDEDOR"
  }' | jq .

# Test 1.2: Intentar crear usuario con rol inválido
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nombre": "Test User",
    "correo": "test@example.com",
    "rol": "SUPERUSUARIO"
  }' | jq .
# Esperado: 400 Bad Request

# Test 1.3: Intentar crear usuario sin autenticación
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test User",
    "correo": "test@example.com",
    "rol": "VENDEDOR"
  }' | jq .
# Esperado: 401 Unauthorized

# Test 1.4: Crear usuario duplicado (mismo email)
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nombre": "Otro Nombre",
    "correo": "juan.perez@example.com",
    "rol": "INVENTARIO"
  }' | jq .
# Esperado: 409 Conflict (email duplicado)
```

---

### FLUJO 2: AUTENTICACIÓN Y LOGIN

**Objetivo:** Validar login correcto, generación de tokens y estados prohibidos.

#### Pre-condiciones
- [ ] Usuario existe en auth_db con estado ACTIVO
- [ ] Password está correctamente encriptado en Supabase
- [ ] Auth Service está corriendo
- [ ] Supabase JWKS están accesibles

#### Pasos
1. **POST** `/api/v1/auth/login` (sin autenticación requerida)
   ```json
   {
     "email": "usuario@example.com",
     "password": "SecurePass123!"
   }
   ```

#### Expected Results
- [ ] HTTP Status: 200 (OK)
- [ ] Response contiene:
  ```json
  {
    "success": true,
    "data": {
      "accessToken": "eyJhbGc...",
      "refreshToken": "refresh_token_value",
      "expiresIn": 3600,
      "tokenType": "Bearer"
    }
  }
  ```
- [ ] Access Token es válido JWT ES256
- [ ] Refresh Token está registrado en DB
- [ ] Expiration es ~1 hora (3600 segundos)

#### Post-condiciones
- [ ] Refresh Token guardado en auth_db
- [ ] Sesión registrada en audit log
- [ ] Token puede usarse en próximas requests

#### Validaciones de Base de Datos

```sql
-- En auth_db
SELECT * FROM refresh_tokens 
WHERE user_id = '<user-id>' 
ORDER BY created_at DESC LIMIT 1;
-- Validar: token_hash, expires_at, revoked = false

-- Verificar audit log
SELECT * FROM audit_logs 
WHERE user_id = '<user-id>' 
AND action = 'LOGIN' 
ORDER BY created_at DESC LIMIT 1;
```

#### Validación de Token

```bash
# Decodificar JWT
echo $ACCESS_TOKEN | jq -R 'split(".") | .[1] | @base64d | fromjson'

# Verificar claims
# - sub: user_id
# - email: email
# - role: rol del usuario
# - exp: timestamp de expiración
# - iat: issued at timestamp
```

#### Comandos de Ejecución

```bash
# Test 2.1: Login exitoso
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminPass123!"
  }')
echo $RESPONSE | jq .
ADMIN_TOKEN=$(echo $RESPONSE | jq -r '.data.accessToken')
echo "Token: $ADMIN_TOKEN"

# Test 2.2: Login con password incorrecto
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "WrongPassword123!"
  }' | jq .
# Esperado: 401 Unauthorized

# Test 2.3: Login con email inexistente
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "noexiste@example.com",
    "password": "AnyPass123!"
  }' | jq .
# Esperado: 401 Unauthorized

# Test 2.4: Obtener info del usuario autenticado
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
# Esperado: 200 OK con datos del usuario

# Test 2.5: Intentar acceder sin token
curl -X GET http://localhost:8080/api/v1/auth/me | jq .
# Esperado: 401 Unauthorized
```

---

### FLUJO 3: CREAR PRODUCTO (CATÁLOGO)

**Objetivo:** Validar creación de producto con código único, categoría válida y precio correcto.

#### Pre-condiciones
- [ ] Usuario autenticado con rol ADMIN o INVENTARIO
- [ ] Catalog Service está corriendo (puerto 8082)
- [ ] Base de datos catalog_db está accesible
- [ ] Categoría válida existe en catalog_db
- [ ] Código de producto no existe previamente

#### Pasos
1. **POST** `/api/v1/productos` (requiere ADMIN o INVENTARIO)
   ```json
   {
     "nombre": "Laptop Dell XPS 13",
     "codigo": "PROD-000001",
     "descripcion": "Laptop de alto rendimiento",
     "precioVenta": 2500000,
     "costo": 1500000,
     "categoriaId": "uuid-categoria",
     "codigoBarras": "8719256123456"
   }
   ```

#### Expected Results
- [ ] HTTP Status: 201 (CREATED)
- [ ] Response contiene:
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid-producto",
      "nombre": "Laptop Dell XPS 13",
      "codigo": "PROD-000001",
      "descripcion": "Laptop de alto rendimiento",
      "precioVenta": 2500000,
      "costo": 1500000,
      "margen": 1000000,
      "estado": "ACTIVO",
      "categoriaId": "uuid-categoria",
      "createdAt": "2026-06-07T02:00:00Z",
      "createdBy": "uuid-usuario"
    }
  }
  ```
- [ ] Estado inicial: ACTIVO
- [ ] Margen calculado: precioVenta - costo

#### Post-condiciones
- [ ] Producto registrado en catalog_db
- [ ] Código es único y no puede reutilizarse
- [ ] Audit log creado
- [ ] No se crea inventario automáticamente

#### Validaciones de Base de Datos

```sql
-- En catalog_db
SELECT * FROM products WHERE codigo = 'PROD-000001';
-- Validar: id, nombre, codigo, precio_venta, costo, estado = 'ACTIVO'
-- Validar: categoria_id, created_by, created_at

-- Verificar código es único
SELECT COUNT(*) FROM products WHERE codigo = 'PROD-000001';
-- Resultado esperado: 1

-- Verificar categoría es válida
SELECT * FROM categories 
WHERE id = '<categoria-id>' AND estado = 'ACTIVA';

-- Verificar audit
SELECT * FROM audit_logs 
WHERE entity_type = 'PRODUCT' 
AND entity_id = '<product-id>'
AND action = 'CREATED'
ORDER BY created_at DESC LIMIT 1;
```

#### Comandos de Ejecución

```bash
# Obtener ID de categoría válida
CATEGORY_ID=$(curl -s -X GET http://localhost:8080/api/v1/categorias \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.data[0].id')
echo "Category ID: $CATEGORY_ID"

# Test 3.1: Crear producto con datos válidos
curl -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"nombre\": \"Laptop Dell XPS 13\",
    \"codigo\": \"PROD-000001\",
    \"descripcion\": \"Laptop de alto rendimiento\",
    \"precioVenta\": 2500000,
    \"costo\": 1500000,
    \"categoriaId\": \"$CATEGORY_ID\",
    \"codigoBarras\": \"8719256123456\"
  }" | jq .

# Test 3.2: Intentar crear producto con código duplicado
curl -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"nombre\": \"Otro Laptop\",
    \"codigo\": \"PROD-000001\",
    \"descripcion\": \"Intento duplicado\",
    \"precioVenta\": 3000000,
    \"costo\": 1800000,
    \"categoriaId\": \"$CATEGORY_ID\",
    \"codigoBarras\": \"1234567890123\"
  }" | jq .
# Esperado: 409 Conflict (código duplicado)

# Test 3.3: Crear producto con precio inválido (negativo)
curl -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"nombre\": \"Producto Inválido\",
    \"codigo\": \"PROD-000002\",
    \"descripcion\": \"Precio negativo\",
    \"precioVenta\": -1000,
    \"costo\": 500,
    \"categoriaId\": \"$CATEGORY_ID\"
  }" | jq .
# Esperado: 400 Bad Request

# Test 3.4: Crear producto con rol VENDEDOR (no permitido)
VENDEDOR_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "vendedor@example.com",
    "password": "VendedorPass123!"
  }' | jq -r '.data.accessToken')

curl -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VENDEDOR_TOKEN" \
  -d "{
    \"nombre\": \"No Debería Crearse\",
    \"codigo\": \"PROD-000003\",
    \"descripcion\": \"Rol no permitido\",
    \"precioVenta\": 1000000,
    \"costo\": 500000,
    \"categoriaId\": \"$CATEGORY_ID\"
  }" | jq .
# Esperado: 403 Forbidden

# Test 3.5: Listar productos
curl -X GET "http://localhost:8080/api/v1/productos?page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

---

### FLUJO 4: REGISTRAR INVENTARIO

**Objetivo:** Validar inicialización de inventario y movimientos de stock.

#### Pre-condiciones
- [ ] Producto existe en catalog_db (Flujo 3)
- [ ] Inventory Service está corriendo (puerto 8083)
- [ ] Base de datos inventory_db está accesible
- [ ] No existe inventario previo para este producto
- [ ] Usuario autenticado con rol ADMIN o INVENTARIO

#### Pasos
1. **POST** `/api/v1/inventory/initialize` (requiere ADMIN o INVENTARIO)
   ```json
   {
     "productoId": "uuid-producto",
     "stockInicial": 100,
     "stockMinimo": 10,
     "stockMaximo": 500,
     "ubicacion": "Almacén A - Estante 3"
   }
   ```

2. **POST** `/api/v1/inventory/entrada` (entrada de stock)
   ```json
   {
     "productoId": "uuid-producto",
     "cantidad": 50,
     "razon": "Reabastecimiento"
   }
   ```

#### Expected Results
- [ ] Inicialización HTTP Status: 201
- [ ] Response contiene:
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid-inventario",
      "productoId": "uuid-producto",
      "stockActual": 100,
      "stockMinimo": 10,
      "stockMaximo": 500,
      "estado": "ACTIVO",
      "createdAt": "2026-06-07T02:00:00Z"
    }
  }
  ```
- [ ] Stock inicial registrado correctamente
- [ ] Stock actual = stock inicial
- [ ] Movimiento de tipo INVENTARIO_INICIAL creado

#### Post-condiciones
- [ ] Inventario registrado en inventory_db
- [ ] Movimiento de stock registrado
- [ ] Audit log creado para inicialización
- [ ] Stock disponible para ventas

#### Validaciones de Base de Datos

```sql
-- En inventory_db
SELECT * FROM inventory WHERE producto_id = '<product-id>';
-- Validar: stock_actual = 100, stock_minimo = 10, stock_maximo = 500
-- Validar: estado = 'ACTIVO', ubicacion

-- Verificar movimiento de inventario
SELECT * FROM movements 
WHERE producto_id = '<product-id>' 
ORDER BY created_at DESC LIMIT 1;
-- Validar: tipo = 'INVENTARIO_INICIAL', cantidad = 100
-- Validar: stock_anterior = 0, stock_nuevo = 100, usuario_id = '<user-id>'

-- Verificar entrada de stock
SELECT * FROM movements 
WHERE producto_id = '<product-id>' 
AND tipo = 'ENTRADA'
ORDER BY created_at DESC LIMIT 1;
-- Validar: cantidad = 50, stock_nuevo = 150
```

#### Comandos de Ejecución

```bash
# Obtener ID de producto creado en Flujo 3
PRODUCT_ID=$(curl -s -X GET "http://localhost:8080/api/v1/productos?page=1&size=1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.data[0].id')
echo "Product ID: $PRODUCT_ID"

# Test 4.1: Inicializar inventario
curl -X POST http://localhost:8080/api/v1/inventory/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"productoId\": \"$PRODUCT_ID\",
    \"stockInicial\": 100,
    \"stockMinimo\": 10,
    \"stockMaximo\": 500,
    \"ubicacion\": \"Almacén A - Estante 3\"
  }" | jq .

# Test 4.2: Intentar reinicializar (debe fallar)
curl -X POST http://localhost:8080/api/v1/inventory/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"productoId\": \"$PRODUCT_ID\",
    \"stockInicial\": 200,
    \"stockMinimo\": 20,
    \"stockMaximo\": 600,
    \"ubicacion\": \"Otro Almacén\"
  }" | jq .
# Esperado: 409 Conflict (ya inicializado)

# Test 4.3: Registrar entrada de stock
curl -X POST http://localhost:8080/api/v1/inventory/entrada \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"productoId\": \"$PRODUCT_ID\",
    \"cantidad\": 50,
    \"razon\": \"Reabastecimiento\"
  }" | jq .
# Esperado: 201, stock_actual = 150

# Test 4.4: Registrar salida de stock
curl -X POST http://localhost:8080/api/v1/inventory/salida \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"productoId\": \"$PRODUCT_ID\",
    \"cantidad\": 30,
    \"razon\": \"Venta manual\"
  }" | jq .
# Esperado: 201, stock_actual = 120

# Test 4.5: Intentar salida que excede stock
curl -X POST http://localhost:8080/api/v1/inventory/salida \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"productoId\": \"$PRODUCT_ID\",
    \"cantidad\": 200,
    \"razon\": \"Intento exceso\"
  }" | jq .
# Esperado: 400 Bad Request (stock insuficiente)

# Test 4.6: Obtener información de inventario
curl -X GET "http://localhost:8080/api/v1/inventory/$PRODUCT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 4.7: Listar movimientos
curl -X GET "http://localhost:8080/api/v1/inventory/movements?productoId=$PRODUCT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

---

### FLUJO 5: CREAR PEDIDO DE COMPRA

**Objetivo:** Validar creación de compra, cálculo automático de totales y actualización de inventario.

#### Pre-condiciones
- [ ] Producto existe en catalog_db (Flujo 3)
- [ ] Inventario inicializado (Flujo 4)
- [ ] Purchase Service está corriendo (puerto 8086)
- [ ] Base de datos purchase_db está accesible
- [ ] Proveedor válido existe
- [ ] Usuario autenticado con rol ADMIN o INVENTARIO

#### Pasos
1. **POST** `/api/v1/compras` (requiere ADMIN o INVENTARIO)
   ```json
   {
     "proveedorId": "uuid-proveedor",
     "items": [
       {
         "productoId": "uuid-producto",
         "cantidad": 50,
         "precioUnitario": 1500000
       }
     ],
     "observaciones": "Pedido urgente"
   }
   ```

2. **PATCH** `/api/v1/compras/{id}/confirmar` (cambiar a PENDIENTE)

3. **PATCH** `/api/v1/compras/{id}/recibir` (cambiar a RECIBIDA, actualiza inventario)
   ```json
   {
     "cantidadRecibida": 50
   }
   ```

#### Expected Results
- [ ] Creación HTTP Status: 201
- [ ] Estado inicial: BORRADOR
- [ ] Response contiene:
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid-compra",
      "codigo": "COM-2026-000001",
      "proveedorId": "uuid-proveedor",
      "estado": "BORRADOR",
      "subtotal": 75000000,
      "impuestos": 14250000,
      "total": 89250000,
      "items": [
        {
          "productoId": "uuid-producto",
          "cantidad": 50,
          "precioUnitario": 1500000,
          "subtotal": 75000000
        }
      ],
      "createdAt": "2026-06-07T02:00:00Z"
    }
  }
  ```
- [ ] Total = Subtotal + Impuestos (19% IVA)
- [ ] Estado puede cambiar: BORRADOR → PENDIENTE → RECIBIDA → PAGADA
- [ ] Al recibir, inventario se actualiza

#### Post-condiciones
- [ ] Compra registrada en purchase_db
- [ ] Inventario del producto aumenta al recibir
- [ ] Audit log registra todas las transiciones de estado
- [ ] Relación con producto mantiene referencia UUID

#### Validaciones de Base de Datos

```sql
-- En purchase_db
SELECT * FROM purchases WHERE codigo = 'COM-2026-000001';
-- Validar: id, codigo, proveedor_id, estado = 'BORRADOR'
-- Validar: subtotal, impuestos, total

-- Verificar items de compra
SELECT * FROM purchase_items 
WHERE purchase_id = '<compra-id>';
-- Validar: producto_id, cantidad, precio_unitario, subtotal

-- Verificar cambio de estado
SELECT * FROM audit_logs 
WHERE entity_type = 'PURCHASE' 
AND entity_id = '<compra-id>'
ORDER BY created_at;

-- En inventory_db - Verificar movimiento al recibir
SELECT * FROM movements 
WHERE tipo = 'ENTRADA' 
AND related_purchase_id = '<compra-id>'
ORDER BY created_at DESC LIMIT 1;
```

#### Comandos de Ejecución

```bash
# Obtener proveedor (previamente creado)
SUPPLIER_ID=$(curl -s -X GET "http://localhost:8080/api/v1/proveedores?page=1&size=1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.data[0].id')
echo "Supplier ID: $SUPPLIER_ID"

# Test 5.1: Crear compra
PURCHASE=$(curl -s -X POST http://localhost:8080/api/v1/compras \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"proveedorId\": \"$SUPPLIER_ID\",
    \"items\": [
      {
        \"productoId\": \"$PRODUCT_ID\",
        \"cantidad\": 50,
        \"precioUnitario\": 1500000
      }
    ],
    \"observaciones\": \"Pedido urgente\"
  }")
echo $PURCHASE | jq .
PURCHASE_ID=$(echo $PURCHASE | jq -r '.data.id')
echo "Purchase ID: $PURCHASE_ID"

# Test 5.2: Confirmar compra
curl -X PATCH "http://localhost:8080/api/v1/compras/$PURCHASE_ID/confirmar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
# Esperado: estado cambia de BORRADOR a PENDIENTE

# Test 5.3: Recibir compra (actualiza inventario)
curl -X PATCH "http://localhost:8080/api/v1/compras/$PURCHASE_ID/recibir" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"cantidadRecibida\": 50
  }" | jq .
# Esperado: estado = RECIBIDA, inventario aumenta

# Test 5.4: Verificar inventario actualizado
curl -X GET "http://localhost:8080/api/v1/inventory/$PRODUCT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data.stockActual'
# Esperado: stock_actual incrementado

# Test 5.5: Intentar crear compra sin proveedor válido
curl -X POST http://localhost:8080/api/v1/compras \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"proveedorId\": \"00000000-0000-0000-0000-000000000000\",
    \"items\": [
      {
        \"productoId\": \"$PRODUCT_ID\",
        \"cantidad\": 10,
        \"precioUnitario\": 1500000
      }
    ]
  }" | jq .
# Esperado: 404 Not Found (proveedor no existe)

# Test 5.6: Intentar recibir cantidad superior a lo pedido
curl -X PATCH "http://localhost:8080/api/v1/compras/$PURCHASE_ID/recibir" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"cantidadRecibida\": 100
  }" | jq .
# Esperado: 400 Bad Request (cantidad excede)

# Test 5.7: Obtener detalles de compra
curl -X GET "http://localhost:8080/api/v1/compras/$PURCHASE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 5.8: Listar compras
curl -X GET "http://localhost:8080/api/v1/compras?estado=RECIBIDA&page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

---

### FLUJO 6: CREAR VENTA

**Objetivo:** Validar creación de venta, validación de stock, cálculo de impuestos y generación de factura.

#### Pre-condiciones
- [ ] Producto existe en catalog_db (Flujo 3)
- [ ] Inventario tiene stock disponible (Flujos 4-5)
- [ ] Sales Service está corriendo (puerto 8084)
- [ ] Base de datos sales_db está accesible
- [ ] Cliente válido existe
- [ ] Usuario autenticado con rol ADMIN o VENDEDOR

#### Pasos
1. **POST** `/api/v1/sales` (requiere ADMIN o VENDEDOR)
   ```json
   {
     "clienteId": "uuid-cliente",
     "items": [
       {
         "productoId": "uuid-producto",
         "cantidad": 20,
         "precioUnitario": 2500000
       }
     ],
     "descuentoVenta": 0,
     "observaciones": "Venta regular"
   }
   ```

2. **PATCH** `/api/v1/sales/{id}/confirmar` (cambiar a CONFIRMADA, genera factura)

3. **PATCH** `/api/v1/sales/{id}/pagar` (cambiar a PAGADA)

#### Expected Results
- [ ] Creación HTTP Status: 201
- [ ] Estado inicial: BORRADOR
- [ ] Response contiene:
  ```json
  {
    "success": true,
    "data": {
      "id": "uuid-venta",
      "codigo": "VTA-2026-000001",
      "clienteId": "uuid-cliente",
      "estado": "BORRADOR",
      "subtotal": 50000000,
      "descuento": 0,
      "baseGravable": 50000000,
      "iva": 9500000,
      "total": 59500000,
      "items": [
        {
          "productoId": "uuid-producto",
          "cantidad": 20,
          "precioUnitario": 2500000,
          "subtotal": 50000000
        }
      ],
      "createdAt": "2026-06-07T02:00:00Z"
    }
  }
  ```
- [ ] IVA calculado: 19% sobre base gravable (subtotal - descuentos)
- [ ] Total = Subtotal - Descuentos + IVA
- [ ] Stock suficiente validado antes de confirmar
- [ ] Stock se decrementa al confirmar
- [ ] Factura generada automáticamente

#### Post-condiciones
- [ ] Venta registrada en sales_db
- [ ] Inventario del producto disminuye
- [ ] Factura generada en sales_db
- [ ] Audit log registra transiciones de estado
- [ ] Evento VentaConfirmada publicado en RabbitMQ

#### Validaciones de Base de Datos

```sql
-- En sales_db
SELECT * FROM sales WHERE codigo = 'VTA-2026-000001';
-- Validar: id, codigo, cliente_id, estado = 'BORRADOR'
-- Validar: subtotal, descuento, iva, total
-- Validar: created_by (user_id del vendedor)

-- Verificar items de venta
SELECT * FROM sale_items 
WHERE sale_id = '<venta-id>';
-- Validar: producto_id, cantidad, precio_unitario

-- Verificar factura generada (después de confirmar)
SELECT * FROM invoices 
WHERE sale_id = '<venta-id>';
-- Validar: numero_factura (secuencial), fecha, estado = 'EMITIDA'

-- En inventory_db - Verificar salida de stock
SELECT * FROM movements 
WHERE tipo = 'SALIDA' 
AND related_sale_id = '<venta-id>'
ORDER BY created_at DESC LIMIT 1;
-- Validar: cantidad = 20, stock se decrementó
```

#### Comandos de Ejecución

```bash
# Obtener cliente (previamente creado)
CUSTOMER_ID=$(curl -s -X GET "http://localhost:8080/api/v1/clientes?page=1&size=1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.data[0].id')
echo "Customer ID: $CUSTOMER_ID"

# Test 6.1: Crear venta
SALE=$(curl -s -X POST http://localhost:8080/api/v1/sales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"clienteId\": \"$CUSTOMER_ID\",
    \"items\": [
      {
        \"productoId\": \"$PRODUCT_ID\",
        \"cantidad\": 20,
        \"precioUnitario\": 2500000
      }
    ],
    \"descuentoVenta\": 0,
    \"observaciones\": \"Venta regular\"
  }")
echo $SALE | jq .
SALE_ID=$(echo $SALE | jq -r '.data.id')
echo "Sale ID: $SALE_ID"

# Test 6.2: Confirmar venta (genera factura)
curl -X PATCH "http://localhost:8080/api/v1/sales/$SALE_ID/confirmar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
# Esperado: estado = CONFIRMADA, factura generada

# Test 6.3: Pagar venta
curl -X PATCH "http://localhost:8080/api/v1/sales/$SALE_ID/pagar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
# Esperado: estado = PAGADA

# Test 6.4: Intentar crear venta con stock insuficiente
curl -X POST http://localhost:8080/api/v1/sales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"clienteId\": \"$CUSTOMER_ID\",
    \"items\": [
      {
        \"productoId\": \"$PRODUCT_ID\",
        \"cantidad\": 10000,
        \"precioUnitario\": 2500000
      }
    ]
  }" | jq .
# Esperado: 400 Bad Request (stock insuficiente)

# Test 6.5: Intentar crear venta sin cliente válido
curl -X POST http://localhost:8080/api/v1/sales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"clienteId\": \"00000000-0000-0000-0000-000000000000\",
    \"items\": [
      {
        \"productoId\": \"$PRODUCT_ID\",
        \"cantidad\": 5,
        \"precioUnitario\": 2500000
      }
    ]
  }" | jq .
# Esperado: 404 Not Found

# Test 6.6: Aplicar descuento (requiere ADMIN)
SALE_DISCOUNT=$(curl -s -X POST http://localhost:8080/api/v1/sales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"clienteId\": \"$CUSTOMER_ID\",
    \"items\": [
      {
        \"productoId\": \"$PRODUCT_ID\",
        \"cantidad\": 10,
        \"precioUnitario\": 2500000
      }
    ],
    \"descuentoVenta\": 5000000
  }")
echo $SALE_DISCOUNT | jq .
# Validar: descuento aplicado, total reducido

# Test 6.7: Verificar inventario reducido
curl -X GET "http://localhost:8080/api/v1/inventory/$PRODUCT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data.stockActual'

# Test 6.8: Obtener detalles de venta
curl -X GET "http://localhost:8080/api/v1/sales/$SALE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 6.9: Obtener factura generada
INVOICE_ID=$(curl -s -X GET "http://localhost:8080/api/v1/sales/$SALE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.data.invoiceId // .data.facturaId')
curl -X GET "http://localhost:8080/api/v1/facturas/$INVOICE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 6.10: Listar ventas por estado
curl -X GET "http://localhost:8080/api/v1/sales?estado=PAGADA&page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

---

### FLUJO 7: CONSULTAR REPORTE

**Objetivo:** Validar generación de reportes, filtrado y exportación.

#### Pre-condiciones
- [ ] Múltiples ventas confirmadas (Flujo 6)
- [ ] Report Service está corriendo (puerto 8085)
- [ ] Base de datos report_db está accesible
- [ ] Datos se han replicado desde otros servicios
- [ ] Usuario autenticado con rol ADMIN o VENDEDOR

#### Pasos
1. **GET** `/api/v1/reports/ventas` (con filtros opcionales)
   ```
   ?fechaInicio=2026-06-01&fechaFin=2026-06-30&vendedorId=...
   ```

2. **GET** `/api/v1/reports/inventario`

3. **GET** `/api/v1/reports/productos-vendidos`

4. **POST** `/api/v1/reports/export` (exportar a PDF/Excel)

#### Expected Results
- [ ] HTTP Status: 200
- [ ] Response contiene:
  ```json
  {
    "success": true,
    "data": {
      "reportType": "VENTAS",
      "periodo": "2026-06-01 a 2026-06-30",
      "totalVentas": 3,
      "totalMonto": 178500000,
      "items": [
        {
          "fecha": "2026-06-07",
          "codigo": "VTA-2026-000001",
          "cliente": "Nombre Cliente",
          "monto": 59500000,
          "estado": "PAGADA"
        }
      ]
    }
  }
  ```
- [ ] Filtros aplicados correctamente
- [ ] Totales calculados correctamente
- [ ] Datos consolidados desde múltiples servicios

#### Post-condiciones
- [ ] Reporte consultable y exportable
- [ ] No modifica datos originales
- [ ] Audit log registra consulta (si aplica)

#### Validaciones de Base de Datos

```sql
-- En report_db
SELECT * FROM reports WHERE tipo = 'VENTAS' 
ORDER BY created_at DESC LIMIT 1;

-- Verificar datos consolidados
SELECT COUNT(*) as total_ventas, SUM(total) as monto_total
FROM view_sales_report
WHERE fecha BETWEEN '2026-06-01' AND '2026-06-30';
```

#### Comandos de Ejecución

```bash
# Test 7.1: Obtener reporte de ventas (sin filtros)
curl -X GET "http://localhost:8080/api/v1/reports/ventas?page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 7.2: Reporte de ventas con filtros de fecha
curl -X GET "http://localhost:8080/api/v1/reports/ventas?fechaInicio=2026-06-01&fechaFin=2026-06-30&page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 7.3: Reporte de inventario
curl -X GET "http://localhost:8080/api/v1/reports/inventario?page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 7.4: Productos más vendidos
curl -X GET "http://localhost:8080/api/v1/reports/productos-vendidos?limit=5" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 7.5: Clientes frecuentes
curl -X GET "http://localhost:8080/api/v1/reports/clientes-frecuentes?limit=5" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Test 7.6: Exportar reporte a PDF
curl -X POST "http://localhost:8080/api/v1/reports/export" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"reportType\": \"VENTAS\",
    \"formato\": \"PDF\",
    \"filtros\": {
      \"fechaInicio\": \"2026-06-01\",
      \"fechaFin\": \"2026-06-30\"
    }
  }" \
  -o reporte_ventas.pdf

# Test 7.7: Exportar reporte a Excel
curl -X POST "http://localhost:8080/api/v1/reports/export" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"reportType\": \"INVENTARIO\",
    \"formato\": \"EXCEL\",
    \"filtros\": {}
  }" \
  -o reporte_inventario.xlsx

# Test 7.8: Intentar ver reporte sin autorización (VENDEDOR puede ver solo sus ventas)
curl -X GET "http://localhost:8080/api/v1/reports/ventas?page=1&size=10" \
  -H "Authorization: Bearer $VENDEDOR_TOKEN" | jq .
# Esperado: 200 (puede ver, pero filtrado)

# Test 7.9: Dashboard resumido
curl -X GET "http://localhost:8080/api/v1/reports/dashboard" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
# Esperado: KPIs de ventas, inventario, etc.
```

---

### FLUJO 8: LOGOUT Y CIERRE DE SESIÓN

**Objetivo:** Validar invalidación de tokens y cierre seguro de sesión.

#### Pre-condiciones
- [ ] Usuario autenticado con access token válido
- [ ] Refresh token existe en auth_db
- [ ] Auth Service está corriendo

#### Pasos
1. **POST** `/api/v1/auth/logout` (requiere autenticación)
   ```json
   {
     "refreshToken": "refresh_token_value"
   }
   ```

2. Intentar usar access token después de logout (debe fallar)

#### Expected Results
- [ ] HTTP Status: 200
- [ ] Response contiene:
  ```json
  {
    "success": true,
    "message": "Sesión cerrada exitosamente"
  }
  ```
- [ ] Refresh token marcado como revocado
- [ ] Access token no puede reutilizarse
- [ ] Nueva autenticación requerida

#### Post-condiciones
- [ ] Sesión registrada en audit log
- [ ] Refresh token no válido para renovación
- [ ] Todas las sesiones del usuario permanecen activas (solo esta se invalida)

#### Validaciones de Base de Datos

```sql
-- En auth_db
SELECT * FROM refresh_tokens 
WHERE token_hash = HASH('<refresh-token>')
ORDER BY created_at DESC LIMIT 1;
-- Validar: revoked = true, revoked_at = NOW()

-- Verificar audit log
SELECT * FROM audit_logs 
WHERE user_id = '<user-id>' 
AND action = 'LOGOUT'
ORDER BY created_at DESC LIMIT 1;
```

#### Comandos de Ejecución

```bash
# Test 8.1: Logout exitoso
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"refreshToken\": \"<refresh-token-value>\"
  }" | jq .

# Test 8.2: Intentar usar token después de logout (debe fallar)
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
# Esperado: 401 Unauthorized (token invalidado)

# Test 8.3: Intentar renovar con refresh token revocado
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"<revoked-refresh-token>\"
  }" | jq .
# Esperado: 401 Unauthorized (token revocado)

# Test 8.4: Logout sin token (no autenticado)
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"any-token\"
  }" | jq .
# Esperado: 401 Unauthorized

# Test 8.5: Renovar token (refresh)
REFRESH=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminPass123!"
  }' | jq -r '.data.refreshToken')

curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH\"
  }" | jq .
# Esperado: 200 con nuevo access token

# Test 8.6: Logout all sessions (logout desde todos los dispositivos)
curl -X POST http://localhost:8080/api/v1/auth/logout-all \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
# Esperado: 200, todos los refresh tokens revocados
```

---

## PRECONDICIONES GLOBALES

Antes de ejecutar los flujos E2E, verificar:

```bash
# 1. Docker Compose está corriendo
docker-compose ps

# 2. Bases de datos están accesibles
docker-compose exec postgres psql -U postgres -l

# 3. API Gateway responde
curl -s http://localhost:8080/health | jq .

# 4. Supabase está configurado en .env
cat .env | grep SUPABASE

# 5. Usuario ADMIN existe y puede autenticarse
# (se proporciona en setup inicial)
```

---

## COMANDOS DE SETUP

### 1. Inicializar Ambiente

```bash
# Clonar repositorio
git clone <repo-url>
cd axisERP-platform

# Copiar variables de ambiente
cp .env.example .env
# EDITAR .env con valores reales de Supabase y bases de datos

# Construir imágenes Docker
docker-compose build

# Levantar servicios
docker-compose up -d

# Esperar a que servicios inicien (~30 segundos)
sleep 30

# Verificar servicios corriendo
docker-compose logs -f api-gateway | head -20
```

### 2. Crear Usuario ADMIN Inicial

```bash
# Vía script de seed o manualmente en Supabase:
# Crear usuario en Supabase con:
# - Email: admin@example.com
# - Password: AdminPass123! (temporal)
# - Role: ADMIN

# Luego crear en auth_db vía API:
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <supabase-service-role-token>" \
  -d '{
    "nombre": "Administrador",
    "correo": "admin@example.com",
    "rol": "ADMIN"
  }'
```

### 3. Crear Datos Base

```bash
# Ejecutar scripts de seed
cd scripts
./seed-database.sh

# O manualmente:
# 1. Crear categoría de productos
# 2. Crear proveedor
# 3. Crear cliente
```

---

## SCRIPTS DE VALIDACIÓN

### Script 1: Verificar Servicios Activos

```bash
#!/bin/bash
# scripts/check-services.sh

SERVICES=("8080" "8081" "8082" "8083" "8084" "8085" "8086")
for PORT in "${SERVICES[@]}"; do
  if curl -s http://localhost:$PORT/health > /dev/null; then
    echo "✓ Service on port $PORT is running"
  else
    echo "✗ Service on port $PORT is DOWN"
    exit 1
  fi
done
```

### Script 2: Ejecutar Flujo E2E Completo

```bash
#!/bin/bash
# scripts/run-e2e-test.sh

set -e

echo "========================================"
echo "TESTING E2E FLOW - AXISERP"
echo "========================================"

# 1. Login
echo -e "\n[1] Testing Login Flow..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "AdminPass123!"
  }')
ADMIN_TOKEN=$(echo $RESPONSE | jq -r '.data.accessToken')
echo "✓ Login successful, token: ${ADMIN_TOKEN:0:20}..."

# 2. Crear Producto
echo -e "\n[2] Creating Product..."
PRODUCT=$(curl -s -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nombre": "Test Product",
    "codigo": "PROD-TEST-001",
    "descripcion": "E2E Test Product",
    "precioVenta": 100000,
    "costo": 50000,
    "categoriaId": "'$(curl -s -X GET http://localhost:8080/api/v1/categorias \
      -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.data[0].id')'"
  }')
PRODUCT_ID=$(echo $PRODUCT | jq -r '.data.id')
echo "✓ Product created: $PRODUCT_ID"

# 3. Inicializar Inventario
echo -e "\n[3] Initializing Inventory..."
curl -s -X POST http://localhost:8080/api/v1/inventory/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "productoId": "'$PRODUCT_ID'",
    "stockInicial": 100,
    "stockMinimo": 10,
    "stockMaximo": 500
  }' | jq '.data.stockActual'
echo "✓ Inventory initialized"

# 4. Crear Venta
echo -e "\n[4] Creating Sale..."
SALE=$(curl -s -X POST http://localhost:8080/api/v1/sales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "clienteId": "'$(curl -s -X GET http://localhost:8080/api/v1/clientes?page=1&size=1 \
      -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.data[0].id')'",
    "items": [{
      "productoId": "'$PRODUCT_ID'",
      "cantidad": 10,
      "precioUnitario": 100000
    }]
  }')
SALE_ID=$(echo $SALE | jq -r '.data.id')
echo "✓ Sale created: $SALE_ID"

# 5. Confirmar Venta
echo -e "\n[5] Confirming Sale..."
curl -s -X PATCH "http://localhost:8080/api/v1/sales/$SALE_ID/confirmar" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.data.estado'
echo "✓ Sale confirmed"

# 6. Ver Reporte
echo -e "\n[6] Generating Report..."
REPORT=$(curl -s -X GET "http://localhost:8080/api/v1/reports/ventas?page=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "✓ Report generated"
echo $REPORT | jq '.data | length'

# 7. Logout
echo -e "\n[7] Logout..."
curl -s -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.success'
echo "✓ Logout successful"

echo -e "\n========================================"
echo "ALL E2E TESTS PASSED!"
echo "========================================"
```

### Script 3: Validación de Base de Datos

```bash
#!/bin/bash
# scripts/validate-database.sh

echo "Validating Database State..."

# Verificar usuarios creados
echo -e "\n--- USERS ---"
docker-compose exec -T postgres psql -U postgres -d auth_db -c \
  "SELECT id, nombre, rol, estado FROM users LIMIT 5;"

# Verificar productos
echo -e "\n--- PRODUCTS ---"
docker-compose exec -T postgres psql -U postgres -d catalog_db -c \
  "SELECT id, nombre, codigo, precio_venta FROM products LIMIT 5;"

# Verificar inventario
echo -e "\n--- INVENTORY ---"
docker-compose exec -T postgres psql -U postgres -d inventory_db -c \
  "SELECT p.producto_id, i.stock_actual, i.stock_minimo FROM inventory i LIMIT 5;"

# Verificar ventas
echo -e "\n--- SALES ---"
docker-compose exec -T postgres psql -U postgres -d sales_db -c \
  "SELECT id, codigo, estado, total FROM sales LIMIT 5;"

# Verificar movimientos de stock
echo -e "\n--- MOVEMENTS ---"
docker-compose exec -T postgres psql -U postgres -d inventory_db -c \
  "SELECT producto_id, tipo, cantidad, stock_nuevo FROM movements ORDER BY created_at DESC LIMIT 10;"
```

---

## REPORTE DE RESULTADOS

Después de ejecutar los flujos, completar:

```markdown
## REPORTE DE TESTING E2E - FECHA: [FECHA]

### RESUMEN EJECUTIVO
- Flujos Ejecutados: __/8
- Flujos Exitosos: __/8
- Tasa de Éxito: __%
- Tiempo Total: __ minutos

### RESULTADOS POR FLUJO

#### Flujo 1: Registro de Usuario
- [ ] Test 1.1: Crear usuario válido - ✓ PASS / ✗ FAIL
- [ ] Test 1.2: Validar rol inválido - ✓ PASS / ✗ FAIL
- [ ] Test 1.3: Validar autenticación requerida - ✓ PASS / ✗ FAIL
- [ ] Test 1.4: Validar email duplicado - ✓ PASS / ✗ FAIL
**Observaciones:**

#### Flujo 2: Autenticación y Login
- [ ] Test 2.1: Login exitoso - ✓ PASS / ✗ FAIL
- [ ] Test 2.2: Password incorrecto - ✓ PASS / ✗ FAIL
- [ ] Test 2.3: Email inexistente - ✓ PASS / ✗ FAIL
- [ ] Test 2.4: Obtener info autenticado - ✓ PASS / ✗ FAIL
- [ ] Test 2.5: Acceso sin token - ✓ PASS / ✗ FAIL
**Observaciones:**

#### Flujo 3: Crear Producto
- [ ] Test 3.1: Crear producto válido - ✓ PASS / ✗ FAIL
- [ ] Test 3.2: Código duplicado - ✓ PASS / ✗ FAIL
- [ ] Test 3.3: Precio inválido - ✓ PASS / ✗ FAIL
- [ ] Test 3.4: Rol no autorizado - ✓ PASS / ✗ FAIL
- [ ] Test 3.5: Listar productos - ✓ PASS / ✗ FAIL
**Observaciones:**

#### Flujo 4: Registrar Inventario
- [ ] Test 4.1: Inicializar inventario - ✓ PASS / ✗ FAIL
- [ ] Test 4.2: Reinicializar (debe fallar) - ✓ PASS / ✗ FAIL
- [ ] Test 4.3: Entrada de stock - ✓ PASS / ✗ FAIL
- [ ] Test 4.4: Salida de stock - ✓ PASS / ✗ FAIL
- [ ] Test 4.5: Exceso de salida - ✓ PASS / ✗ FAIL
**Observaciones:**

#### Flujo 5: Crear Pedido de Compra
- [ ] Test 5.1: Crear compra - ✓ PASS / ✗ FAIL
- [ ] Test 5.2: Confirmar compra - ✓ PASS / ✗ FAIL
- [ ] Test 5.3: Recibir compra - ✓ PASS / ✗ FAIL
- [ ] Test 5.4: Verificar inventario actualizado - ✓ PASS / ✗ FAIL
- [ ] Test 5.5: Proveedor inválido - ✓ PASS / ✗ FAIL
**Observaciones:**

#### Flujo 6: Crear Venta
- [ ] Test 6.1: Crear venta - ✓ PASS / ✗ FAIL
- [ ] Test 6.2: Confirmar venta - ✓ PASS / ✗ FAIL
- [ ] Test 6.3: Pagar venta - ✓ PASS / ✗ FAIL
- [ ] Test 6.4: Stock insuficiente - ✓ PASS / ✗ FAIL
- [ ] Test 6.5: Cliente inválido - ✓ PASS / ✗ FAIL
**Observaciones:**

#### Flujo 7: Consultar Reporte
- [ ] Test 7.1: Reporte ventas básico - ✓ PASS / ✗ FAIL
- [ ] Test 7.2: Reporte con filtros - ✓ PASS / ✗ FAIL
- [ ] Test 7.3: Reporte inventario - ✓ PASS / ✗ FAIL
- [ ] Test 7.4: Productos vendidos - ✓ PASS / ✗ FAIL
- [ ] Test 7.5: Exportar PDF - ✓ PASS / ✗ FAIL
**Observaciones:**

#### Flujo 8: Logout y Cierre
- [ ] Test 8.1: Logout exitoso - ✓ PASS / ✗ FAIL
- [ ] Test 8.2: Token invalidado - ✓ PASS / ✗ FAIL
- [ ] Test 8.3: Refresh token revocado - ✓ PASS / ✗ FAIL
- [ ] Test 8.4: Logout sin token - ✓ PASS / ✗ FAIL
**Observaciones:**

### VALIDACIONES DE DATOS

#### Validaciones de Integridad
- [ ] Usuarios registrados correctamente en auth_db
- [ ] Roles y estados asignados correctamente
- [ ] Productos con códigos únicos en catalog_db
- [ ] Inventario actualizado en inventory_db
- [ ] Ventas registradas en sales_db
- [ ] Facturas generadas automáticamente
- [ ] Audit logs completos en todos los servicios

#### Validaciones de Seguridad
- [ ] Tokens JWT válidos y expiración correcta
- [ ] Refresh tokens revocados al logout
- [ ] Autorización por rol funciona correctamente
- [ ] Datos sensibles no expuestos en responses

### ISSUES ENCONTRADOS

| ID | Severidad | Descripción | Estado |
|----|-----------|-------------|--------|
| E2E-001 | ALTA | | ABIERTO |
| E2E-002 | MEDIA | | ABIERTO |

### CONCLUSIONES

[Resumen de hallazgos y recomendaciones]

### PRÓXIMOS PASOS

1. [Acción 1]
2. [Acción 2]
3. [Acción 3]

---
**Fecha de Ejecución:** [FECHA]  
**Ejecutado por:** [USUARIO]  
**Versión del Plan:** 1.0
```

---

## ANEXOS

### A. Variables de Ambiente Requeridas

```bash
# Supabase
SUPABASE_URL=
SUPABASE_JWT_ISSUER=
SUPABASE_SERVICE_ROLE_KEY=
SUPABASE_ANON_KEY=

# Seguridad
JWT_SECRET=
INTERNAL_API_KEY=

# PostgreSQL
AUTH_DB_URL=
CATALOG_DB_URL=
INVENTORY_DB_URL=
SALES_DB_URL=
PURCHASE_DB_URL=
REPORT_DB_URL=

AUTH_DB_USERNAME=
AUTH_DB_PASSWORD=
CATALOG_DB_USERNAME=
CATALOG_DB_PASSWORD=
INVENTORY_DB_USERNAME=
INVENTORY_DB_PASSWORD=
SALES_DB_USERNAME=
SALES_DB_PASSWORD=
PURCHASE_DB_USERNAME=
PURCHASE_DB_PASSWORD=
REPORT_DB_USERNAME=
REPORT_DB_PASSWORD=
```

### B. Estructura de Respuesta API Estándar

```json
{
  "success": true,
  "data": {},
  "message": "Operación exitosa",
  "timestamp": "2026-06-07T02:00:00Z",
  "pagination": {
    "page": 1,
    "size": 20,
    "totalElements": 100
  }
}
```

### C. Roles y Permisos

| Rol | Crear Usuario | Crear Producto | Crear Venta | Crear Compra | Ver Reportes |
|-----|---------------|----------------|-------------|--------------|--------------|
| ADMIN | ✓ | ✓ | ✓ | ✓ | ✓ |
| INVENTARIO | ✗ | ✓ | ✗ | ✓ | ✓ (limitado) |
| VENDEDOR | ✗ | ✗ | ✓ | ✗ | ✓ (limitado) |

### D. Estados de Entidades

**Usuario:** PENDIENTE → ACTIVO → INACTIVO / ELIMINADO  
**Producto:** ACTIVO ↔ INACTIVO / ELIMINADO  
**Inventario:** ACTIVO ↔ INACTIVO  
**Compra:** BORRADOR → PENDIENTE → RECIBIDA → PAGADA / CANCELADA  
**Venta:** BORRADOR → PENDIENTE → CONFIRMADA → PAGADA / ANULADA  
**Factura:** EMITIDA → PAGADA / ANULADA

---

**Documento de Referencia:** E2E Testing Plan v1.0  
**Última Actualización:** 2026-06-07  
**Responsable:** Team AxisERP
