# AXISERP — SALE CONFIRM FIX REPORT

## 1. CAUSA RAÍZ EXACTA

**Tipo:** Mismatch entre el formato que sales-service envía y el que inventory-service espera.

**Archivo emisor:** `sales-service/src/main/java/com/axiserp/sales/infrastructure/adapters/out/http/InventoryServiceAdapter.java`
**Método:** `checkAndExit()` (líneas 37-61) y `registerReturn()` (líneas 63-85)

**Archivo receptor:** `inventory-service/src/main/java/com/axiserp/inventory/infrastructure/adapters/in/web/controller/InventoryController.java`
**Método:** `registerExit()` (líneas 130-139)

---

## 2. EVIDENCIA DE CÓDIGO

### Sales-service envía (ANTES del fix)

`InventoryServiceAdapter.java:38-52`:
```java
String url = UriComponentsBuilder
        .fromHttpUrl(inventoryServiceUrl)
        .path("/api/v1/inventory/products/{productId}/exit")
        .queryParam("quantity", quantity)            // ← query param
        .queryParam("referenceType", referenceType)  // ← query param
        .queryParam("referenceId", referenceId)       // ← query param
        .buildAndExpand(productId)
        .toUriString();

HttpHeaders headers = buildHeaders();
HttpEntity<Void> entity = new HttpEntity<>(headers);  // ← SIN BODY
ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
```

**Request generado:**
```
POST /api/v1/inventory/products/{id}/exit?quantity=2&referenceType=VENTA&referenceId=...
Headers: X-Internal-Api-Key: ...
Body: (vacío)
```

### Inventory-service espera

`InventoryController.java:130-139`:
```java
@PostMapping("/products/{productId}/exit")
public ResponseEntity<ApiResponse<MovementResponse>> registerExit(
        @PathVariable UUID productId,
        @Valid @RequestBody MovementRequest request) {   // ← ESPERA BODY
```

**Espera:**
```
POST /api/v1/inventory/products/{id}/exit
Content-Type: application/json
Body: {"quantity": 2, "referenceType": "VENTA", "referenceId": "..."}
```

---

## 3. TABLA DE COMPARACIÓN

| Elemento | Sales envía | Inventory espera | ¿Coincide? |
|----------|------------|-----------------|------------|
| Método HTTP | `POST` | `@PostMapping` | ✅ |
| Path | `/products/{id}/exit` | `/products/{productId}/exit` | ✅ |
| **Content-Type** | **No enviado** | **`application/json`** | **❌** |
| **quantity** | **Query param** | **`@RequestBody` field** | **❌** |
| **referenceType** | **Query param** | **`@RequestBody` field** | **❌** |
| **referenceId** | **Query param** | **`@RequestBody` field** | **❌** |
| Body | Vacío (`HttpEntity<Void>`) | `MovementRequest` JSON | ❌ |

---

## 4. PATCH APLICADO

### Archivo modificado
`sales-service/src/main/java/com/axiserp/sales/infrastructure/adapters/out/http/InventoryServiceAdapter.java`

### Cambio en `checkAndExit()`

**Antes:**
```java
String url = UriComponentsBuilder
        .fromHttpUrl(inventoryServiceUrl)
        .path("/api/v1/inventory/products/{productId}/exit")
        .queryParam("quantity", quantity)
        .queryParam("referenceType", referenceType)
        .queryParam("referenceId", referenceId)
        .buildAndExpand(productId)
        .toUriString();

HttpHeaders headers = buildHeaders();
HttpEntity<Void> entity = new HttpEntity<>(headers);
ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
```

**Después:**
```java
String url = inventoryServiceUrl + "/api/v1/inventory/products/" + productId + "/exit";

HttpHeaders headers = buildHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
Map<String, Object> body = Map.of(
        "quantity", quantity,
        "referenceType", referenceType,
        "referenceId", referenceId != null ? referenceId.toString() : null);
HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
ResponseEntity<Void> response = restTemplate.postForEntity(url, entity, Void.class);
```

### Cambio en `registerReturn()`
Mismo patrón: reemplazar query params + body vacío por JSON body con `Map.of(...)`.

---

## 5. ARCHIVOS MODIFICADOS

| Archivo | Cambio |
|---------|--------|
| `sales-service/.../InventoryServiceAdapter.java` | Enviar quantity/referenceType/referenceId como JSON body en vez de query params |

---

## 6. VALIDACIÓN

| Servicio | Tests | Resultado |
|----------|-------|-----------|
| sales-service | 70 tests | ✅ BUILD SUCCESS, 0 failures |

**Commit:** `2041866`

**Para validar en producción:** Haz Manual Deploy del **sales-service** en Render. Luego prueba:
```
POST /api/v1/sales (crear venta)
PATCH /api/v1/sales/{id}/confirm (confirmar)
PATCH /api/v1/sales/{id}/pay (pagar)
GET /api/v1/invoices/by-sale/{saleId} (ver factura)
```
