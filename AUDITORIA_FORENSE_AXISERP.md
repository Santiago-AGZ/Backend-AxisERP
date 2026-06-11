# AUDITORÍA FORENSE AXISERP — FRONTEND ↔ BACKEND

**Fecha:** 2026-06-11  
**Versión:** 1.0  
**Clasificación final:** **NO CERTIFICADO**

---

## RESUMEN EJECUTIVO

| Métrica | Valor |
|---------|-------|
| Backend endpoints totales | 85 (82 efectivos) |
| Frontend endpoints consumidos | 64 activos + 21 muertos |
| Endpoints backend huérfanos (no consumidos) | 21 (25%) |
| DTO mismatches CRÍTICOS | 33 |
| Flujos E2E rotos | 2 (CRÍTICO) |
| Issues React Query | 2 CRÍTICOS + 3 HIGH |
| Issues UX | 10 CRÍTICOS |
| Issues Seguridad | 1 HIGH |
| Archivos de código muerto | ~25 archivos |
| **Porcentaje real de integración** | **47.6%** |
| **Veredicto** | **NO CERTIFICADO PARA PRODUCCIÓN** |

---

## 1. MATRIZ COMPLETA BACKEND ↔ FRONTEND

### 1.1 Integración por microservicio

| Servicio | Endpoints Backend | Consumidos Frontend | Huérfanos | % Integración |
|----------|------------------|---------------------|-----------|---------------|
| auth-service | 23 | 12 (activos) | 11 | 52% |
| catalog-service | 12 | 12 | 0 | 100% |
| inventory-service | 11 | 11 | 0 | 100% |
| purchase-service | 12 | 12 | 0 | 100% |
| sales-service | 18 | 17 | 1 | 94% |
| report-service | 9 | 8 | 1 | 89% |
| **TOTAL** | **85** | **64 activos** | **21** | **75% (routing) / 48% (DTO)** |

### 1.2 Endpoints huérfanos (backend sin consumo frontend)

| # | Endpoint | Servicio | Severidad | Motivo |
|---|----------|----------|-----------|--------|
| 1 | `GET /auth/validate-token` | auth-service | MEDIO | Servicio definido pero no llamado por ningún componente |
| 2 | `GET /usuarios/{id}` | auth-service | MEDIO | Usado solo por hooks deprecados |
| 3 | `POST /auth/roles` | auth-service | MEDIO | UI no implementada para crear roles |
| 4 | `PUT /auth/roles/{id}` | auth-service | MEDIO | UI no implementada para editar roles |
| 5 | `DELETE /auth/roles/{id}` | auth-service | MEDIO | UI no implementada para eliminar roles |
| 6 | `GET /auth/permissions` | auth-service | BAJO | Stub backend (lista vacía) |
| 7 | `GET /auth/roles/{roleId}/permissions` | auth-service | BAJO | Stub backend (lista vacía) |
| 8 | `POST /auth/roles/{roleId}/permissions` | auth-service | BAJO | Stub backend ("no implementado") |
| 9 | `DELETE /auth/roles/{roleId}/permissions/{permId}` | auth-service | BAJO | Stub backend ("no implementado") |
| 10 | `POST /usuarios/{id}/reset-password` | auth-service | ALTO | Frontend lo define en capa deprecada, backend NO existe |
| 11 | `GET /productos/{id}` | catalog-service | BAJO | Solo usado por hooks deprecados |
| 12 | `GET /categorias/{id}` | catalog-service | BAJO | Solo usado por hooks deprecados |
| 13 | `GET /customers/{codigo}` | sales-service | MEDIO | Solo usado por hooks deprecados |
| 14 | `GET /sales/{id}` | sales-service | BAJO | Solo usado por hooks deprecados |
| 15 | `GET /invoices/{id}` | sales-service | BAJO | Solo usado por hooks deprecados |
| 16 | `GET /invoices/by-sale/{saleId}` | sales-service | BAJO | Solo usado por hooks deprecados |
| 17 | `GET /suppliers/{id}` | purchase-service | BAJO | Solo usado por hooks deprecados |
| 18 | `GET /purchases/{id}` | purchase-service | BAJO | Solo usado por hooks deprecados |
| 19 | `GET /inventory/products/{productId}` | inventory-service | BAJO | Solo usado por hooks deprecados |
| 20 | `GET /inventory/products/{productId}/movements` | inventory-service | MEDIO | Solo usado por hooks deprecados |
| 21 | `GET /reports/audit` | report-service | BAJO | No consumido, baja prioridad |

### 1.3 Endpoints rotos (frontend → backend inválido)

| # | Endpoint | Problema | Evidencia | Severidad |
|---|----------|----------|-----------|-----------|
| 1 | `PATCH /usuarios/{id}/desactivar` | Falta `currentPassword` query param requerido | `services/auth.ts:126` no envía el param; `UserController.java:107` lo requiere con `@RequestParam` | **CRÍTICO** |
| 2 | `DELETE /usuarios/{id}` | Falta `currentPassword` query param requerido | `services/auth.ts:136` no envía el param; `UserController.java:142` lo requiere con `@RequestParam` | **CRÍTICO** |
| 3 | `POST /auth/login` | Campo `email` enviado vs `username` esperado | `types/auth.ts:55` → `email`; `LoginRequest.java:14` → `username` | **CRÍTICO** |

---

## 2. DTO MISMATCHES — EVIDENCIA EXACTA

### 2.1 Mismatches CRÍTICOS (rompen funcionalidad)

| # | Entidad | Campo Java | Campo TypeScript | Archivo Evidencia |
|---|---------|------------|------------------|-------------------|
| 1 | LoginRequest | `username` | `email` | `LoginRequest.java:14` vs `types/auth.ts:55` |
| 2 | CreateUserRequest | `role` | `roleId` | `CreateUserRequest.java:22` vs `types/auth.ts:74` |
| 3 | UpdateUserRequest | `role` | `roleId` | `UpdateUserRequest.java:22` vs `types/auth.ts:80` |
| 4 | ProductInventoryResponse | `productCodigo` | `productCode` | `ProductInventoryResponse.java:22` vs `types/inventory.ts:12` |
| 5 | ProductInventoryResponse | `depleted` | `outOfStock` | `ProductInventoryResponse.java:27` vs `types/inventory.ts:17` |
| 6 | ProductInventoryResponse | `lastMovementAt` | `lastUpdated` | `ProductInventoryResponse.java:28` vs `types/inventory.ts:19` |
| 7 | MovementResponse | `movementType` | `type` | `MovementResponse.java:25` vs `types/inventory.ts:27` |
| 8 | MovementResponse | `createdBy` | `userId` | `MovementResponse.java:33` vs `types/inventory.ts:31` |
| 9 | CustomerResponse | `codigo` | *(ausente)* | `CustomerResponse.java:20` vs `types/sales.ts:10-21` |
| 10 | CreateCustomerRequest | `codigo` | *(ausente)* | `CreateCustomerRequest.java:20` vs `types/sales.ts:50-57` |
| 11 | SaleResponse | `saleNumber` | `invoiceNumber` | `SaleResponse.java:23` vs `types/sales.ts:27` |
| 12 | SaleResponse | `tax` | `iva` | `SaleResponse.java:28` vs `types/sales.ts:29` |
| 13 | SaleResponse | `createdAt` | `date` | `SaleResponse.java:33` vs `types/sales.ts:36` |
| 14 | SaleResponse | `createdBy` | `userId` | `SaleResponse.java:31` vs `types/sales.ts:35` |
| 15 | SaleResponse | `status` (único) | `saleStatus` + `paymentStatus` | `SaleResponse.java:24` vs `types/sales.ts:32-33` |
| 16 | SaleItemRequest | `productName` (@NotNull) | *(ausente)* | `SaleItemRequest.java:26` vs `types/sales.ts:75-79` |
| 17 | SupplierResponse | `codigo` | *(ausente)* | `SupplierResponse.java:22` vs `types/purchase.ts:9-19` |
| 18 | CreateSupplierRequest | `codigo` (@NotBlank) | *(ausente)* | `CreateSupplierRequest.java:22` vs `types/purchase.ts:46-52` |
| 19 | PurchaseResponse | `purchaseNumber` | `orderNumber` | `PurchaseResponse.java:25` vs `types/purchase.ts:25` |
| 20 | PurchaseResponse | `status` (único) | `orderStatus` + `paymentStatus` | `PurchaseResponse.java:26` vs `types/purchase.ts:27-28` |
| 21 | PurchaseResponse | `createdAt` | `date` | `PurchaseResponse.java:34` vs `types/purchase.ts:31` |
| 22 | PurchaseItemResponse | `receivedQuantity` | `receivedQty` | `PurchaseItemResponse.java:23` vs `types/purchase.ts:42` |
| 23 | PurchaseItemRequest | `productName` (@NotBlank) | *(ausente)* | `PurchaseItemRequest.java:27` vs `types/purchase.ts:67-71` |
| 24 | PaginatedResponse | `totalRecords` | `totalElements` | `PaginatedResponse.java:14` vs `types/auth.ts:97` |
| 25 | UpdateCustomerRequest | *(ausente en Java)* | `documentType`, `documentNumber` requeridos | `UpdateCustomerRequest.java` vs `types/sales.ts:61-62` |
| 26 | CreateSaleRequest | *(ausente en Java)* | `paymentStatus` requerido | `CreateSaleRequest.java` vs `types/sales.ts:70` |
| 27 | CreatePurchaseRequest | *(ausente en Java)* | `status` requerido | `CreatePurchaseRequest.java` vs `types/purchase.ts:63` |
| 28 | UpdateSupplierRequest | `name` (@NotBlank) | `name` (opcional `?`) | `UpdateSupplierRequest.java:17` vs `types/purchase.ts:55` |
| 29 | UpdateUserRequest | `name` (@NotBlank) | `name` (opcional `?`) | `UpdateUserRequest.java:14` vs `types/auth.ts:78` |
| 30 | UpdateUserRequest | `email` (@NotBlank) | `email` (opcional `?`) | `UpdateUserRequest.java:18` vs `types/auth.ts:79` |
| 31 | SaleStatus enum | `PAGADA` | `PAGADO` | Género gramatical — `SaleStatus.java` vs `types/sales.ts:8` |
| 32 | PurchaseStatus enum | `PENDIENTE`, `APROBADA` | *(ausentes)* | `PurchaseStatus.java:4` vs `types/purchase.ts:7` |
| 33 | SaleStatus enum | `BORRADOR`, `CONFIRMADA` | *(ausentes)* | `SaleStatus.java:4` vs `types/sales.ts:8` |

### 2.2 Mismatches HIGH (datos no recibidos)

| # | Entidad | Campo Java | Campo TypeScript | Impacto |
|---|---------|------------|------------------|---------|
| 1 | ProductResponse | `margin`, `marginPercentage` | *(ausentes)* | Márgenes no visibles |
| 2 | ProductInventoryResponse | `maxStock` | *(ausente)* | Stock máximo no visible |
| 3 | MovementResponse | `justification`, `referenceType`, `referenceId` | *(ausentes)* | Justificación de ajustes no visible |
| 4 | SaleResponse | `notes` | *(ausente)* | Notas de venta no visibles |
| 5 | PurchaseResponse | `subtotal`, `tax` | *(ausentes)* | Subtotales e impuestos no visibles |
| 6 | PurchaseItemResponse | `pendingQuantity` | *(ausente)* | Cantidades pendientes no visibles |
| 7 | AuditLogResponse | `userId`, `userName`, `entityId` | `?` opcionales | Campos de auditoría marcados opcionales cuando son requeridos |
| 8 | Profile | `role` (String) | `role` (Role objeto anidado) | Tipo de role diverge |

---

## 3. REGLAS DE NEGOCIO VIOLADAS O EN RIESGO

| # | RN | Descripción | Estado | Evidencia |
|---|----|-------------|--------|-----------|
| RN-001 | No vender sin stock suficiente | ✅ IMPLEMENTADO | Backend: `Inventory.subtractStock()` lanza `InsufficientStockException` |
| RN-003 | Solo ADMIN gestiona usuarios | ✅ IMPLEMENTADO | Backend: `@PreAuthorize("hasRole('ADMIN')")` en UserController |
| RN-006 | Acciones críticas en auditoría | ⚠️ PARCIAL | Login/logout auditados. Falta verificar CRUD de todas las entidades |
| RN-012 | Usuario inactivo no se autentica | ✅ IMPLEMENTADO | `AuthenticateUserService.java:43-46` |
| RN-013 | IVA 19% sobre base gravable | ✅ IMPLEMENTADO | `CreateSaleUseCaseImpl.java:41`: `TAX_RATE = 0.19` |
| RN-014 | Descuentos >30% requieren ADMIN | ⚠️ PARCIAL | Backend OK. Frontend: cap de 30% en schema impide a ADMIN meter >30% (`VentasPage.tsx:42`) |
| RN-015 | Control concurrencia optimistic locking | ✅ IMPLEMENTADO | `@Version` en Inventory.java:24-25 |
| RN-016 | Refresh Token invalidado al desactivar/logout | ✅ IMPLEMENTADO | Logout: blacklist + revoke. Desactivar: verificado en `DeactivateUserUseCaseImpl` |

---

## 4. PROBLEMAS UX — EVIDENCIA

### 4.1 CRÍTICOS (10)

| # | Página | Problema | Archivo:Línea |
|---|--------|----------|---------------|
| 1 | ClientesPage | Desactivar/Reactivar sin diálogo de confirmación | `ClientesPage.tsx:179` |
| 2 | ProveedoresPage | Desactivar/Reactivar sin diálogo de confirmación | `ProveedoresPage.tsx:161` |
| 3 | ProductosPage | Desactivar/Reactivar sin diálogo de confirmación | `ProductosPage.tsx:186` |
| 4 | CategoriasPage | Desactivar/Reactivar sin diálogo de confirmación | `CategoriasPage.tsx:175` |
| 5 | UsuariosPage | Usa `confirm()` nativo en vez de `ConfirmDialog` | `UsuariosPage.tsx:210` |
| 6 | InventarioPage | Sin manejo de errores en 3 queries de datos | `InventarioPage.tsx:75,85,90` |
| 7 | ReportesPage:Sales | Sin manejo de `isError` en query | `ReportesPage.tsx:141` |
| 8 | ReportesPage:Inventory | Sin manejo de `isError` en query | `ReportesPage.tsx:245` |
| 9 | ReportesPage:TopProducts | Sin manejo de `isError` en query | `ReportesPage.tsx:298` |
| 10 | ReportesPage:FrequentCustomers | Sin manejo de `isError` en query | `ReportesPage.tsx:52` |

### 4.2 HIGH (3)

| # | Página | Problema | Archivo:Línea |
|---|--------|----------|---------------|
| 11 | InventarioPage | Sin retry en queries fallidas | `InventarioPage.tsx:75,85,90` |
| 12 | InventarioPage | Depleted products sin loading state | `InventarioPage.tsx:90` |
| 13 | ReportesPage:Sales | Sin retry en query fallida | `ReportesPage.tsx:141` |

---

## 5. PROBLEMAS REACT QUERY

### 5.1 CRÍTICOS (2)

| # | Problema | Impacto | Archivo:Línea |
|---|----------|---------|---------------|
| 1 | Inventory mutations no invalidan dashboard | Dashboard muestra stock desactualizado 5 min | `InventarioPage.tsx:120-164` |
| 2 | voidSale no invalida dashboard | Dashboard muestra revenue incorrecto tras anular | `VentasPage.tsx:141-153` |

### 5.2 HIGH (3)

| # | Problema | Impacto | Archivo:Línea |
|---|----------|---------|---------------|
| 3 | receivePurchase no invalida inventory | Inventario desactualizado tras recibir compra | `ComprasPage.tsx:122` |
| 4 | confirmSale/paySale no invalidan inventory | Inventario desactualizado tras confirmar venta | `VentasPage.tsx:115-138` |
| 5 | Query key hardcodeado en customer history | Inconsistencia con `queryKeys` centralizado | `ClientesPage.tsx:93` |

---

## 6. PROBLEMAS SEGURIDAD

| # | Problema | Categoría OWASP | Severidad | Evidencia |
|---|----------|----------------|-----------|-----------|
| 1 | Access token + Refresh token en localStorage | A07: Auth Failures | **HIGH** | `src/lib/axios.ts:20,32` |
| 2 | Sin CSRF protection configurada en axios | A01: Broken Access Control | LOW | Mitigado por JWT Bearer |
| 3 | `.env.example` expone subdominio de Render | A02: Cryptographic Failures | LOW | `.env.example:6` |
| 4 | `RoleGuard` es solo client-side | A01: Broken Access Control | MEDIUM | Mitigado si backend valida cada request |
| 5 | Sin DOMPurify para defensa en profundidad | A03: XSS | LOW | React auto-escapa JSX |

---

## 7. PROBLEMAS SEO

| # | Problema | Severidad | Evidencia |
|---|----------|-----------|-----------|
| 1 | `robots` meta = `index, follow` en ERP interno | MEDIUM | `index.html:9` |
| 2 | Sin `og:image` a nivel sitio | MEDIUM | `SeoHead.tsx` |
| 3 | Sin `canonical` URLs por página | MEDIUM | `SeoHead.tsx` |
| 4 | ResetPasswordPage sin `description` meta | LOW | `ResetPasswordPage.tsx:56` |
| 5 | Sitemap solo tiene 2 URLs | LOW | `public/sitemap.xml` |

---

## 8. CÓDIGO MUERTO

### 8.1 Archivos completamente muertos (~25 archivos)

| Directorio | Archivos | Severidad |
|------------|----------|-----------|
| `src/hooks/` | `use-auth.ts`, `use-catalog.ts`, `use-sales.ts`, `use-purchase.ts`, `use-inventory.ts`, `use-role-access.ts`, `use-paginated-query.ts` | MEDIUM |
| `src/api/` | `auth.ts`, `catalog.ts`, `inventory.ts`, `purchase.ts`, `sales.ts` | LOW |
| `src/schemas/` | `auth.ts`, `catalog.ts`, `inventory.ts`, `purchase.ts`, `sales.ts` | **HIGH** |
| `src/types/` | `index.ts` (tipos legacy español) | **HIGH** |
| `src/components/` | `stat-card.tsx`, `section-header.tsx`, `ui/sidebar.tsx`, `ui/sheet.tsx` | MEDIUM |
| `src/lib/` | `validation.ts` | MEDIUM |

### 8.2 Dependencias no usadas

| Dependencia | Estado |
|-------------|--------|
| `@tanstack/react-table` | No importado en ningún archivo |
| `shadcn` | CLI tool, debería estar en devDependencies |

---

## 9. PLAN DE CORRECCIÓN

### CRÍTICO (corregir antes de producción)

| # | Acción | Archivo(s) | Esfuerzo |
|---|--------|------------|----------|
| C1 | Agregar `currentPassword` query param a `deactivateUser()` y `deleteUser()` en frontend, O hacer `required=false` en backend | `services/auth.ts:126,136` / `UserController.java:107,142` | 30 min |
| C2 | Corregir campo `email` → `username` en `LoginRequest` del frontend | `types/auth.ts:55` y `services/auth.ts:86` | 15 min |
| C3 | Agregar invalidación de `reports.dashboard` en 6 mutations de inventario | `InventarioPage.tsx:120-164` | 15 min |
| C4 | Agregar invalidación de `reports.dashboard` en `voidMutation` | `VentasPage.tsx:141` | 5 min |
| C5 | Agregar `ConfirmDialog` a 5 páginas CRUD para desactivar/reactivar | `ClientesPage`, `ProveedoresPage`, `ProductosPage`, `CategoriasPage`, `UsuariosPage` | 2h |
| C6 | Agregar `isError` + `onRetry` a 4 tabs de ReportesPage + 3 queries de InventarioPage | `ReportesPage.tsx`, `InventarioPage.tsx` | 1h |

### ALTO (corregir en siguiente sprint)

| # | Acción | Archivo(s) | Esfuerzo |
|---|--------|------------|----------|
| A1 | Corregir 33 DTO mismatches (unificar nombres de campos) | `types/*.ts` (5 archivos) | 4h |
| A2 | Migrar tokens de localStorage a httpOnly cookies (o documentar riesgo aceptado) | `lib/axios.ts`, `stores/auth.ts`, backend CORS | 8h |
| A3 | Agregar invalidación de inventory en receivePurchase, confirmSale, paySale | `ComprasPage.tsx`, `VentasPage.tsx` | 15 min |
| A4 | Eliminar capa `src/api/` y `src/hooks/` deprecados | 12 archivos | 30 min |
| A5 | Eliminar `src/types/index.ts` y migrar referencias si existen | `types/index.ts` | 30 min |
| A6 | Refactorizar páginas para usar schemas de `src/schemas/` (o eliminar `src/schemas/`) | 5 archivos schema + 10 páginas | 4h |
| A7 | Implementar endpoints de permisos (o quitar UI) | `RoleController.java:84-105` | 4h |

### MEDIO (deuda técnica)

| # | Acción | Esfuerzo |
|---|--------|----------|
| M1 | Eliminar `@tanstack/react-table` y `shadcn` del package.json | 5 min |
| M2 | Agregar `og:image` y `canonical` al componente `SeoHead` | 1h |
| M3 | Cambiar `robots` meta a `noindex, nofollow` en `index.html` | 5 min |
| M4 | Bajar `staleTime` default de 5 min a 60-120s para datos transaccionales | 15 min |
| M5 | Eliminar `ConfirmDialog` no usado o refactorizar páginas para usarlo | 1h |
| M6 | Eliminar `StatCard`, `SectionHeader`, `usePaginatedQuery`, `useRoleAccess` | 15 min |
| M7 | Quitar invalidación innecesaria de dashboard en category/product mutations | 15 min |

### BAJO (mejora continua)

| # | Acción | Esfuerzo |
|---|--------|----------|
| B1 | Agregar DOMPurify para defensa en profundidad XSS | 30 min |
| B2 | Agregar `description` a ResetPasswordPage | 5 min |
| B3 | Agregar optimistic updates para toggles activate/deactivate | 2h |
| B4 | Consolidar schemas Zod duplicados entre páginas | 3h |
| B5 | Mostrar `supplierName` en vez de `supplierId` en ComprasPage | 30 min |

---

## 10. EVIDENCIA FOTOGRÁFICA DE HALLAZGOS

### 10.1 LoginRequest: username vs email (CRÍTICO)

```
BACKEND: auth-service/.../LoginRequest.java:14
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;   // <--- ESPERA 'username'

FRONTEND: types/auth.ts:55
    export interface LoginRequest {
      email: string;           // <--- ENVÍA 'email'
      password: string;
    }
```

### 10.2 deactivateUser sin currentPassword (CRÍTICO)

```
FRONTEND: services/auth.ts:126
    const response = await api.patch(`/usuarios/${id}/desactivar`)
    // NO envía currentPassword

BACKEND: UserController.java:104-107
    @PatchMapping("/usuarios/{id}/desactivar")
    public ResponseEntity<...> deactivateUser(
            @PathVariable UUID id,
            @RequestParam String currentPassword,  // REQUERIDO
            ...
```

### 10.3 SaleResponse: status único vs saleStatus+paymentStatus (CRÍTICO)

```
BACKEND: SaleResponse.java:24
    private String status;  // "BORRADOR" | "PENDIENTE" | "CONFIRMADA" | "PAGADA" | "ANULADA"

FRONTEND: types/sales.ts:32-33
    paymentStatus: PaymentStatus;  // "PENDIENTE" | "PAGADO" | "ANULADO"
    saleStatus: SaleStatus;        // "PENDIENTE" | "CONFIRMADA" | "ANULADA"
```

### 10.4 productCodigo vs productCode (CRÍTICO)

```
BACKEND: ProductInventoryResponse.java:22
    private String productCodigo;  // Jackson serializa como "productCodigo"

FRONTEND: types/inventory.ts:12
    productCode: string;  // Espera "productCode" -> undefined
```

---

## 11. VEREDICTO FINAL

```
╔══════════════════════════════════════════════════════════════╗
║                                                              ║
║   RESULTADO DE AUDITORÍA FORENSE: NO CERTIFICADO             ║
║                                                              ║
║   Motivos:                                                   ║
║   1. 33 DTO mismatches CRÍTICOS (datos no llegan)            ║
║   2. 2 endpoints rotos (deactivateUser, deleteUser)          ║
║   3. 1 campo LoginRequest incorrecto (email vs username)     ║
║   4. 10 issues UX CRÍTICOS (sin diálogos de confirmación)    ║
║   5. 2 issues React Query CRÍTICOS (dashboard desactualizado)║
║   6. 25 archivos de código muerto                             ║
║                                                              ║
║   Porcentaje real de integración: 47.6%                      ║
║                                                              ║
║   Se requieren TODAS las correcciones CRÍTICAS (6 items)     ║
║   antes de considerar certificación para producción.         ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
```

---

## APÉNDICE: Archivos fuente analizados

### Backend (85 endpoints en 13 controladores)
- `auth-service/.../controller/AuthController.java`
- `auth-service/.../controller/TokenController.java`
- `auth-service/.../controller/RoleController.java`
- `auth-service/.../controller/UserController.java`
- `catalog-service/.../controller/ProductController.java`
- `catalog-service/.../controller/CategoryController.java`
- `inventory-service/.../controller/InventoryController.java`
- `purchase-service/.../controller/PurchaseController.java`
- `purchase-service/.../controller/SupplierController.java`
- `sales-service/.../controller/SaleController.java`
- `sales-service/.../controller/InvoiceController.java`
- `sales-service/.../controller/CustomerController.java`
- `report-service/.../controller/ReportController.java`

### Frontend (64 endpoints activos en 14 páginas)
- `src/services/auth.ts`, `catalog.ts`, `inventory.ts`, `purchase.ts`, `sales.ts`, `report.ts`
- `src/views/LoginPage.tsx`, `ResetPasswordPage.tsx`, `DashboardPage.tsx`
- `src/views/ClientesPage.tsx`, `ProveedoresPage.tsx`, `ProductosPage.tsx`, `CategoriasPage.tsx`
- `src/views/InventarioPage.tsx`, `ComprasPage.tsx`, `VentasPage.tsx`, `FacturasPage.tsx`
- `src/views/UsuariosPage.tsx`, `AuditLogPage.tsx`, `ReportesPage.tsx`
- `src/lib/axios.ts`, `src/lib/query.ts`, `src/lib/query-keys.ts`
- `src/stores/auth.ts`
- `src/types/api.ts`

### Documentación
- `BUSINESS_RULES.md` (598 líneas)
- `ARCHITECTURE.md` (340 líneas)
- `documentos/AxisERP_Documentacion_Fase_Analisis.docx.md`
- `documentos/AxisERP_Documentacion_Fase_Arquitectura.docx.md`
- `documentos/RF_AxisERP_v2_Final.xlsx - RF - Requerimientos Funcionales.csv`
