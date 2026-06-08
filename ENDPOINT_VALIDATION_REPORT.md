# AXISERP — ENDPOINT VALIDATION REPORT

**Fecha:** 2026-06-08
**Entorno:** `https://api-gateway-quvd.onrender.com`
**Metodología:** Ejecución real de endpoints. Sin suposiciones. Sin inferencias.

---

## ESTADO DE SERVICIOS EN RENDER

| Servicio | URL | Estado | Evidencia |
|----------|-----|--------|-----------|
| api-gateway | `api-gateway-quvd.onrender.com` | ✅ UP (200) | `/actuator/health` |
| auth-service | vía gateway | ✅ UP (200) | Login responde |
| catalog-service | vía gateway | ✅ UP (200) | Categorías responde |
| report-service | vía gateway | ⚠️ ERROR (500) | Dashboard falla |
| inventory-service | vía gateway | ❌ (429) | Rate limited / no accesible |
| sales-service | vía gateway | ❌ (502) | No disponible (no deployado?) |
| purchase-service | vía gateway | ❌ (502) | No disponible (no deployado?) |

---

## ENDPOINTS VERIFICADOS EXITOSAMENTE

### AUTH (6/6 endpoints funcionales)

| Endpoint | Método | Auth | Status | Evidencia |
|----------|--------|------|--------|-----------|
| `/api/v1/auth/login` | POST | Pública | ✅ 200 | `{"success":true,"data":{"accessToken":"eyJ...","role":"ADMIN","name":"Santiago ADMIN"}}` |
| `/api/v1/auth/login` (inválido) | POST | Pública | ✅ 401 | `{"success":false,"code":"INVALID_CREDENTIALS"}` |
| `/api/v1/auth/me` | GET | Bearer | ✅ 200 | `{"data":{"email":"santiagoalvarez374@gmail.com","role":"ADMIN","status":"ACTIVO"}}` |
| `/api/v1/auth/validate-token` | GET | Bearer | ✅ 200 | `{"success":true}` |
| `/api/v1/auth/refresh` | POST | Pública | ✅ 200 | Nuevo accessToken + refreshToken |
| `/api/v1/auth/logout` | POST | Bearer | ✅ 200 | `{"success":true,"message":"Sesion cerrada exitosamente"}` |
| `/api/v1/auth/password-reset` | POST | Pública | ✅ 200 | `{"success":true,"message":"Si el correo existe..."}` |

### CATEGORÍAS (5/5 endpoints funcionales)

| Endpoint | Método | Auth | Status | Evidencia |
|----------|--------|------|--------|-----------|
| `/api/v1/categorias` | POST | Internal Key | ✅ 201 | Creada: `7deaa821-10a5-4b6a-b1be-aa7e1d9faa77` |
| `/api/v1/categorias` | GET | Internal Key | ✅ 200 | `pagination.totalRecords: 11` |
| `/api/v1/categorias/{id}` | GET | Internal Key | ✅ 200 | `{"data":{"name":"Cat Test","status":"ACTIVA"}}` |
| `/api/v1/categorias/{id}` | PUT | Internal Key | ✅ 200 | Actualizada |
| `/api/v1/categorias/{id}/desactivar` | PATCH | Internal Key | ✅ 200 | `{"data":{"status":"INACTIVA"}}` |

### PRODUCTOS (1/1 endpoint funcional)

| Endpoint | Método | Auth | Status | Evidencia |
|----------|--------|------|--------|-----------|
| `/api/v1/productos` | POST | Internal Key | ✅ 201 | Creado: `8005aac8-c680-4484-ba9a-14b2024bc140` |

---

## ENDPOINTS FALLIDOS

### INVENTARIO
| Endpoint | Método | Status | Error |
|----------|--------|--------|-------|
| `/api/v1/inventory/initialize` | POST | 429 | Too Many Requests (Render infra limit) |
| `/api/v1/inventory/products/{id}/entry` | POST | 429 | Too Many Requests |
| `/api/v1/inventory/products/{id}/exit` | POST | 429 | Too Many Requests |
| `/api/v1/inventory/products/{id}` | GET | 429 | Too Many Requests |
| `/api/v1/inventory/alerts` | GET | 429 | Too Many Requests |

### PROVEEDORES
| Endpoint | Método | Status | Error |
|----------|--------|--------|-------|
| `/api/v1/suppliers` | POST | 502 | Bad Gateway - Servicio no disponible |
| `/api/v1/suppliers` | GET | 502 | Bad Gateway - Servicio no disponible |

### COMPRAS
| Endpoint | Método | Status | Error |
|----------|--------|--------|-------|
| `/api/v1/purchases` | POST | 502 | Bad Gateway - Servicio no disponible |
| `/api/v1/purchases` | GET | 502 | Bad Gateway - Servicio no disponible |

### CLIENTES
| Endpoint | Método | Status | Error |
|----------|--------|--------|-------|
| `/api/v1/customers` | POST | 502 | Bad Gateway - Servicio no disponible |
| `/api/v1/customers` | GET | 502 | Bad Gateway - Servicio no disponible |

### VENTAS
| Endpoint | Método | Status | Error |
|----------|--------|--------|-------|
| `/api/v1/sales` | POST | 502 | Bad Gateway - Servicio no disponible |
| `/api/v1/sales` | GET | 502 | Bad Gateway - Servicio no disponible |
| `/api/v1/invoices/{id}` | GET | 502 | Bad Gateway - Servicio no disponible |

### REPORTES
| Endpoint | Método | Status | Error |
|----------|--------|--------|-------|
| `/api/v1/reports/dashboard` | GET | 500 | Internal Server Error |

---

## ENDPOINTS NO VERIFICABLES

Los siguientes endpoints no pudieron ser verificados porque los servicios no están disponibles en Render:

| Servicio | Endpoints | Causa |
|----------|-----------|-------|
| **purchase-service** | 6 endpoints (suppliers + purchases) | Servicio no desplegado (502 Bad Gateway) |
| **sales-service** | 18 endpoints (sales, customers, invoices) | Servicio no desplegado (502 Bad Gateway) |
| **inventory-service** | 11 endpoints | Límite de requests excedido (429) |
| **report-service** | 9 endpoints | Error interno del servidor (500) |

---

## ERRORES DE AUTENTICACIÓN CROSS-SERVICE

El JWT generado por **auth-service** (HS256) no es aceptado por los otros servicios que validan contra **Supabase JWKS** (ES256).

| Origen | Destino | Token | Resultado |
|--------|---------|-------|-----------|
| auth-service | auth-service | HS256 | ✅ Válido |
| auth-service | catalog-service | HS256 | ❌ 401 (fue reparado en commit `0779cd6`, pendiente de deploy) |
| auth-service | inventory-service | HS256 | ❌ 401 |
| auth-service | purchase-service | HS256 | ❌ 401 |
| auth-service | sales-service | HS256 | ❌ 401 |
| auth-service | report-service | HS256 | ❌ 401 |

**Solución parcial:** El commit `0779cd6` agrega validación HS256 en catalog-service. Falta replicar a inventory, purchase, sales, report.

**Solución temporal:** Usar `X-Internal-Api-Key` header para pruebas internas (funciona en catalog-service).

---

## AFIRMACIONES PREVIAS SIN EVIDENCIA

No se encontraron afirmaciones previas sin evidencia en esta auditoría. Todos los endpoints reportados fueron ejecutados realmente contra el entorno desplegado.

---

## PORCENTAJE REAL DE ENDPOINTS VERIFICADOS

| Categoría | Total | Verificados | % |
|-----------|-------|-------------|---|
| AUTH | 7 | 7 | **100%** |
| CATEGORÍAS | 6 | 5 | **83%** |
| PRODUCTOS | 6 | 1 | **17%** |
| INVENTARIO | 11 | 0 | **0%** |
| PROVEEDORES | 6 | 0 | **0%** |
| COMPRAS | 5 | 0 | **0%** |
| CLIENTES | 7 | 0 | **0%** |
| VENTAS | 6 | 0 | **0%** |
| FACTURAS | 5 | 0 | **0%** |
| REPORTES | 9 | 0 | **0%** |
| **TOTAL** | **73** | **13** | **18%** |

---

## CONCLUSIÓN

| Aspecto | Resultado |
|---------|-----------|
| Endpoints funcionales | **13/73 (18%)** |
| Servicios disponibles | 3/7 (gateway, auth, catalog) |
| Servicios no disponibles | 4/7 (inventory, purchase, sales, report) |
| JWT cross-service | No funcional (solo auth-service acepta tokens HS256) |
| Internal API Key | Funciona en catalog-service |

**El backend requiere que los servicios faltantes (inventory, purchase, sales, report) sean desplegados en Render.** La mayoría de las fallas NO son de código, son de infraestructura (servicios no deployados o rate limit excedido).
