# AXISERP — POSTMAN VALIDATION REPORT

## Archivos generados

| Archivo | Tamaño | Descripción |
|---------|--------|-------------|
| `postman/AxisERP.postman_collection.json` | 165 KB | Colección completa (73 endpoints, 11 carpetas) |
| `postman/AxisERP_Local.postman_environment.json` | 955 B | Variables para entorno local |
| `postman/AxisERP_Render.postman_environment.json` | 972 B | Variables para entorno Render |

---

## Problemas corregidos

### Problema 1: Login usaba `username` en vez de `email`

**Antes:** `{ "username": "admin@axiserp.com", "password": "admin123" }`
**Después:** `{ "email": "admin@axiserp.com", "password": "admin123" }`

**Requests corregidos:** Todos los que usaban login body.

### Problema 2: Scripts leían token en nivel incorrecto

**Antes:** `json.accessToken` → siempre undefined
**Después:** `json.data.accessToken` → correcto

**Scripts actualizados:** Login, Refresh Token, y todos los tests que validan respuesta.

### Problema 3: base_url hardcodeada

**Antes:** `base_url=http://localhost:8081`
**Después:** `{{baseUrl}}` con 146 referencias en toda la colección

**Ambientes creados:**

| Variable | Local | Render |
|----------|-------|--------|
| `baseUrl` | `http://localhost:8080` | `https://api-gateway-quvd.onrender.com` |
| `admin_user_email` | admin@axiserp.com | admin@axiserp.com |
| `admin_user_password` | admin123 | admin123 |
| `inventory_user_email` | inventario@axiserp.com | inventario@axiserp.com |
| `inventory_user_password` | inventario123 | inventario123 |
| `seller_user_email` | vendedor@axiserp.com | vendedor@axiserp.com |
| `seller_user_password` | vendedor123 | vendedor123 |

---

## Estructura de la colección

| Carpeta | Endpoints | Autenticación |
|---------|-----------|---------------|
| Auth | 6 | 3 públicos (login, refresh, password-reset) + 3 autenticados |
| Usuarios | 8 | ADMIN |
| Categorias | 6 | ADMIN/INVENTARIO (crear/actualizar) + ADMIN (desactivar) |
| Productos | 6 | ADMIN/INVENTARIO + ADMIN (desactivar) + VENDEDOR (consultar) |
| Inventario | 9 | ADMIN/INVENTARIO + ADMIN (reversiones) |
| Proveedores | 6 | ADMIN (CREAR) + ADMIN/INVENTARIO/VENDEDOR (consultar) |
| Compras | 5 | ADMIN/INVENTARIO |
| Clientes | 7 | ADMIN/VENDEDOR |
| Ventas | 6 | ADMIN/VENDEDOR + ADMIN (anular) |
| Facturas | 5 | ADMIN/VENDEDOR |
| Reportes | 9 | ADMIN + ADMIN/INVENTARIO (inventario) |
| **TOTAL** | **73** | |

---

## Variables de colección (auto-generadas)

| Variable | Se genera en | Se usa en |
|----------|-------------|-----------|
| `access_token` | Login → script test | Todos los endpoints autenticados |
| `refresh_token` | Login → script test | Refresh, Logout |
| `user_name` | Login → script test | — |
| `role` | Login → script test | — |

---

## Flujo de autenticación

```
Login → 
  extrae json.data.accessToken → access_token
  extrae json.data.refreshToken → refresh_token
  extrae json.data.role → role
  extrae json.data.name → user_name
  →
Refresh → renueva tokens →
Logout → limpia access_token y refresh_token
```

Todos los requests autenticados incluyen:
- Header: `Authorization: Bearer {{access_token}}`
- Pre-request Script: verifica que `access_token` exista
- Test Script: valida `success=true` y HTTP status

---

## Resumen

| Métrica | Valor |
|---------|-------|
| Total endpoints | 73 |
| Carpetas | 11 |
| Endpoints públicos | 3 |
| Endpoints autenticados | 70 |
| Variables de colección | 4 |
| Variables de ambiente | 7 |
| Usos de {{baseUrl}} | 146 |
| Archivos generados | 3 |

**Colección lista para Frontend y QA.**
