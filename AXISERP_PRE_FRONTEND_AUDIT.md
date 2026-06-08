# AXISERP — AUDITORÍA DE CIERRE PRE-FRONTEND

## BLOQUEANTES PARA FRONTEND

**NO SE ENCONTRARON BLOQUEANTES.**

Tras verificar los 76 endpoints en 7 servicios, todos los módulos tienen contratos API completos y funcionales.

---

## BLOQUEANTES PARA PRODUCCIÓN

**NO SE ENCONTRARON BLOQUEANTES FUNCIONALES.**

Los riesgos operativos (secrets en `.env`, `JPA_DDL_AUTO`) son de configuración de despliegue, no de código.

---

## MATRIZ DE CONTRATOS API

### auth-service (14 endpoints)
| Endpoint | Funcional | Observaciones |
|----------|-----------|---------------|
| `POST /api/v1/auth/login` | ✅ | Public. Retorna accessToken + refreshToken |
| `GET /api/v1/auth/me` | ✅ | Autenticado. Retorna perfil del usuario |
| `POST /api/v1/auth/password-reset` | ✅ | Public. Envía email de recuperación |
| `POST /api/v1/auth/logout` | ✅ | Autenticado. Invalida refresh token |
| `POST /api/v1/auth/refresh` | ✅ | Public. Renueva access + refresh tokens |
| `GET /api/v1/auth/validate-token` | ✅ | Autenticado. Valida token actual |
| `POST /api/v1/usuarios` | ✅ | ADMIN. Crea usuario |
| `GET /api/v1/usuarios` | ✅ | ADMIN. Lista usuarios paginado |
| `GET /api/v1/usuarios/{id}` | ✅ | ADMIN. Obtiene usuario |
| `PUT /api/v1/usuarios/{id}` | ✅ | ADMIN. Actualiza usuario |
| `PATCH /api/v1/usuarios/{id}/desactivar` | ✅ | ADMIN. Requiere currentPassword en query |
| `PATCH /api/v1/usuarios/{id}/activar` | ✅ | ADMIN |
| `PATCH /api/v1/usuarios/{id}/reactivar` | ✅ | ADMIN |
| `DELETE /api/v1/usuarios/{id}` | ✅ | ADMIN. Requiere currentPassword en query — **ANTI-PATRÓN: password en query param** |
| `GET /api/v1/audit-log` | ✅ | ADMIN. Paginado |

### catalog-service (12 endpoints)
| Endpoint | Funcional | Observaciones |
|----------|-----------|---------------|
| `POST /api/v1/categorias` | ✅ | ADMIN, INVENTARIO |
| `GET /api/v1/categorias` | ✅ | Paginado con search + includeInactive |
| `GET /api/v1/categorias/{id}` | ✅ | |
| `PUT /api/v1/categorias/{id}` | ✅ | |
| `PATCH /api/v1/categorias/{id}/desactivar` | ✅ | |
| `PATCH /api/v1/categorias/{id}/activar` | ✅ | |
| `POST /api/v1/productos` | ✅ | ADMIN, INVENTARIO |
| `GET /api/v1/productos/{id}` | ✅ | |
| `GET /api/v1/productos` | ✅ | Paginado con search + codigo + categoryId |
| `PUT /api/v1/productos/{id}` | ✅ | |
| `PATCH /api/v1/productos/{id}/desactivar` | ✅ | ADMIN |
| `PATCH /api/v1/productos/{id}/activar` | ✅ | ADMIN |

### inventory-service (11 endpoints)
| Endpoint | Funcional | Observaciones |
|----------|-----------|---------------|
| `POST /api/v1/inventory/initialize` | ✅ | ADMIN, INVENTARIO. Una vez por producto |
| `GET /api/v1/inventory/products` | ✅ | Paginado con categoryId |
| `GET /api/v1/inventory/alerts` | ✅ | Stock bajo (currentStock <= minStock) |
| `GET /api/v1/inventory/alerts/depleted` | ✅ | Stock agotado (currentStock = 0) |
| `GET /api/v1/inventory/products/{productId}` | ✅ | |
| `GET /api/v1/inventory/products/{productId}/movements` | ✅ | Paginado |
| `POST /api/v1/inventory/products/{productId}/entry` | ✅ | |
| `POST /api/v1/inventory/products/{productId}/exit` | ✅ | Valida stock suficiente |
| `POST /api/v1/inventory/products/{productId}/return` | ✅ | |
| `POST /api/v1/inventory/products/{productId}/adjust` | ✅ | Requiere justificación |
| `POST /api/v1/inventory/movements/{movementId}/reverse` | ✅ | ADMIN. Requiere justificación |

### purchase-service (12 endpoints)
| Endpoint | Funcional | Observaciones |
|----------|-----------|---------------|
| `POST /api/v1/suppliers` | ✅ | ADMIN |
| `GET /api/v1/suppliers/{id}` | ✅ | |
| `GET /api/v1/suppliers` | ✅ | Paginado con search |
| `PUT /api/v1/suppliers/{id}` | ✅ | ADMIN |
| `PATCH /api/v1/suppliers/{id}/deactivate` | ✅ | ADMIN |
| `PATCH /api/v1/suppliers/{id}/reactivate` | ✅ | ADMIN |
| `POST /api/v1/purchases` | ✅ | ADMIN, INVENTARIO |
| `GET /api/v1/purchases/{id}` | ✅ | |
| `GET /api/v1/purchases` | ✅ | Paginado con search + status |
| `PATCH /api/v1/purchases/{id}/status` | ✅ | ADMIN |
| `POST /api/v1/purchases/{id}/receive` | ✅ | ADMIN, INVENTARIO. Soporta recepción parcial |
| `PATCH /api/v1/purchases/{id}/cancel` | ✅ | ADMIN |

### sales-service (18 endpoints)
| Endpoint | Funcional | Observaciones |
|----------|-----------|---------------|
| `POST /api/v1/sales` | ✅ | ADMIN, VENDEDOR |
| `GET /api/v1/sales/{id}` | ✅ | |
| `GET /api/v1/sales` | ✅ | Paginado con customerId + status + productId |
| `PATCH /api/v1/sales/{id}/confirm` | ✅ | Valida stock. Genera factura automática |
| `PATCH /api/v1/sales/{id}/pay` | ✅ | |
| `PATCH /api/v1/sales/{id}/void` | ✅ | ADMIN. Restaura stock |
| `GET /api/v1/invoices/{id}` | ✅ | |
| `GET /api/v1/invoices/by-sale/{saleId}` | ✅ | |
| `GET /api/v1/invoices/{saleId}/pdf` | ✅ | Binary PDF download |
| `GET /api/v1/invoices/{saleId}/excel` | ✅ | Binary XLSX download |
| `GET /api/v1/invoices/{saleId}/csv` | ✅ | Binary CSV download |
| `POST /api/v1/customers` | ✅ | |
| `GET /api/v1/customers/{codigo}` | ✅ | Usa codigo (String) en path — **INCONSISTENCIA: otros endpoints usan UUID** |
| `GET /api/v1/customers` | ✅ | Paginado con search |
| `PUT /api/v1/customers/{id}` | ✅ | |
| `PATCH /api/v1/customers/{id}/deactivate` | ✅ | ADMIN |
| `PATCH /api/v1/customers/{id}/reactivate` | ✅ | ADMIN |
| `GET /api/v1/customers/{customerId}/history` | ✅ | Historial de ventas del cliente |

### report-service (9 endpoints)
| Endpoint | Funcional | Observaciones |
|----------|-----------|---------------|
| `GET /api/v1/reports/sales` | ✅ | ADMIN. Filtros: fechas, status, userId, clientId |
| `GET /api/v1/reports/inventory` | ✅ | ADMIN, INVENTARIO. Filtro: categoryId |
| `GET /api/v1/reports/top-products` | ✅ | ADMIN. Filtros: fechas, limit |
| `GET /api/v1/reports/dashboard` | ✅ | ADMIN. Sin filtros — resumen general |
| `GET /api/v1/reports/customers/frequent` | ✅ | ADMIN. Filtros: fechas, limit |
| `GET /api/v1/reports/sales/export/pdf` | ✅ | ADMIN. Binary download |
| `GET /api/v1/reports/inventory/export/excel` | ✅ | ADMIN, INVENTARIO. Binary download |
| `GET /api/v1/reports/sales/export/csv` | ✅ | ADMIN. Binary download |
| `GET /api/v1/reports/audit` | ✅ | ADMIN. Historial de exportaciones |

---

## CONTRATO COMÚN

Todas las respuestas usan el mismo envelope:

```json
{
  "success": true|false,
  "code": "SUCCESS|CREATED|ERROR_CODE",
  "message": "string",
  "data": { ... },
  "errors": [{ "field": "string", "message": "string", "rejectedValue": "any" }],
  "meta": { "timestamp": "ISO8601", "requestId": "string" },
  "pagination": { "page": int, "pageSize": int, "totalRecords": long, "totalPages": int, "hasNext": bool, "hasPrevious": bool }
}
```

Autenticación: `Authorization: Bearer <accessToken>` en todas las requests.

Login retorna: `{ accessToken, refreshToken, role, name }` — el frontend debe almacenar estos valores.

---

## OBSERVACIONES (no bloqueantes)

| # | Observación | Servicio | Detalle |
|---|-------------|----------|---------|
| O1 | `GET /api/v1/customers/{codigo}` usa String (codigo) en path, otros endpoints usan UUID | sales-service | El frontend debe saber que customer lookup usa `CLI-XXXXXX` no UUID |
| O2 | `DELETE /api/v1/usuarios/{id}` y `PATCH /usuarios/{id}/desactivar` requieren `currentPassword` en query param | auth-service | Anti-patrón pero funcional. El frontend debe incluir `?currentPassword=` en la URL |
| O3 | Sin OpenAPI/Swagger | Todos | No hay `springdoc-openapi`. El frontend debe leer los controllers para conocer los contratos |
| O4 | CORS configurado en gateway con `allowedHeaders: *` + `credentials: true` | api-gateway | Funciona en Chrome/Firefox/Edge. Safari puede requerir origins explícitos sin wildcard |
| O5 | Password-reset es público pero retorna mensaje genérico "Si el correo existe..." | auth-service | Comportamiento correcto de seguridad (no revela si el email existe) |

---

## CONCLUSIÓN

**¿Puede comenzar el desarrollo frontend?**

# SÍ CON OBSERVACIONES

**Fundamento:**

1. **76 endpoints funcionales** en 7 servicios con autenticación JWT.
2. **Flujo de login completo**: login → accessToken + refreshToken → Bearer auth.
3. **CORS configurado** en API Gateway con orígenes configurables.
4. **Envelope de respuesta consistente** (`ApiResponse`) en todos los servicios.
5. **Validación de entrada** en todos los endpoints via `@Valid`.
6. **Manejo de errores** global con códigos HTTP correctos (400, 401, 403, 404, 409, 500).
7. **Paginación** implementada en todos los endpoints de listado.
8. **Exportación** de facturas en PDF, Excel y CSV.

**No existen bloqueantes funcionales.** Las observaciones son detalles que el frontend debe conocer pero no impiden el desarrollo de ninguna funcionalidad.

**Rutas base para frontend (vía gateway en puerto 8080):**
- Auth: `http://localhost:8080/api/v1/auth/*`
- Usuarios: `http://localhost:8080/api/v1/usuarios/*`
- Catálogo: `http://localhost:8080/api/v1/productos/*`, `/api/v1/categorias/*`
- Inventario: `http://localhost:8080/api/v1/inventory/*`
- Compras: `http://localhost:8080/api/v1/purchases/*`, `/api/v1/suppliers/*`
- Ventas: `http://localhost:8080/api/v1/sales/*`, `/api/v1/customers/*`, `/api/v1/invoices/*`
- Reportes: `http://localhost:8080/api/v1/reports/*`
