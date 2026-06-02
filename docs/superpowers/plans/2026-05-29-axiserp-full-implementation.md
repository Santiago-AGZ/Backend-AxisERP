# AxisERP — Full Business Rules Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Verify all business rules from the requirements document, fix gaps in catalog-service and auth-service, create DB scripts for all services, and fully implement inventory-service, purchase-service, sales-service, and report-service using hexagonal architecture.

**Architecture:** Each service follows the same hexagonal architecture pattern established in auth-service and catalog-service: domain models (no framework deps), input/output ports (interfaces), application use cases (orchestration), and infrastructure adapters (Spring Data JPA, REST controllers). All services have their own Neon PostgreSQL database.

**Tech Stack:** Java 21, Spring Boot 3.5.x, Spring Data JPA, Spring Security, JJWT, Lombok, PostgreSQL (Neon), Hexagonal Architecture, REST APIs.

**Package pattern:** `com.axiserp.{service}.{domain|application|ports|infrastructure}`

---

## Gap Analysis (Found)

### catalog-service
- `Category.CategoryStatus` only has `ACTIVA`, `INACTIVA` — missing `ELIMINADA`
- `CategoryEntity.CategoryStatus` only has `ACTIVA`, `INACTIVA` — missing `ELIMINADA`
- `Product.ProductStatus` only has `ACTIVO`, `INACTIVO` — missing `ELIMINADO`
- `ProductEntity.ProductStatus` only has `ACTIVO`, `INACTIVO` — missing `ELIMINADO`
- No subcategory support (no `parent_id` in Category/CategoryEntity)
- DB script: `categories` table missing `ELIMINADA` in status column; `products` missing `ELIMINADO`
- `DeactivateCategoryUseCaseImpl` / `DeactivateProductUseCaseImpl` should set `ELIMINADA`/`ELIMINADO` for logical delete, but status enum is incomplete

### inventory-service
- Only skeleton: bare `InventoryMovement` domain model (id, productId, quantity)
- No `Inventory` aggregate (current stock, min/max stock)
- No movement types enum
- No business rule enforcement

### purchase-service
- Only skeleton: bare `Purchase` domain model (id, supplierId, totalAmount)
- No `Supplier` entity
- No `PurchaseItem` entity
- No status states or business rules

### sales-service
- Only skeleton: bare `Sale` domain model (id, customerId, totalAmount)
- No `Customer` entity
- No `SaleItem` entity
- No `Invoice` entity
- No status states or business rules

### report-service
- Only skeleton: generic `Report` domain model
- No actual report aggregation logic

### DB Scripts
- Only `catalog-service/database.sql` exists; all other services missing

---

## Task 1: Fix catalog-service — Status enums + subcategory support

**Files to modify:**
- `catalog-service/src/main/java/com/axiserp/catalog/domain/model/Category.java`
- `catalog-service/src/main/java/com/axiserp/catalog/domain/model/Product.java`
- `catalog-service/src/main/java/com/axiserp/catalog/infrastructure/adapters/out/persistence/entity/CategoryEntity.java`
- `catalog-service/src/main/java/com/axiserp/catalog/infrastructure/adapters/out/persistence/entity/ProductEntity.java`
- `catalog-service/src/main/java/com/axiserp/catalog/application/usecase/DeactivateCategoryUseCaseImpl.java`
- `catalog-service/src/main/java/com/axiserp/catalog/application/usecase/DeactivateProductUseCaseImpl.java`
- `catalog-service/src/main/java/com/axiserp/catalog/ports/output/CategoryRepositoryPort.java`
- `catalog-service/src/main/java/com/axiserp/catalog/infrastructure/adapters/out/persistence/adapter/CategoryRepositoryAdapter.java`
- `axisERP-db-scripts/catalog-service/database.sql`

**Business rules being fixed:**
- Categories rule 12: estados ACTIVA, INACTIVA, ELIMINADA
- Categories rule 15: eliminación lógica
- Products rule 12: estados ACTIVO, INACTIVO, ELIMINADO
- Products rule 16: eliminación lógica
- Categories rules 6-10: subcategorías (parent_id)

- [ ] **Step 1: Add `ELIMINADA` to `Category.CategoryStatus` and add `parentId` field**

```java
// Category.java — full replacement
package com.axiserp.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Category {
    private UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private CategoryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum CategoryStatus {
        ACTIVA, INACTIVA, ELIMINADA
    }

    public boolean isActive() { return this.status == CategoryStatus.ACTIVA; }
    public boolean isDeleted() { return this.status == CategoryStatus.ELIMINADA; }
}
```

- [ ] **Step 2: Add `ELIMINADO` to `Product.ProductStatus`**

```java
// Product.java — in ProductStatus enum, add ELIMINADO
public enum ProductStatus {
    ACTIVO, INACTIVO, ELIMINADO
}

// Also add isDeleted method:
public boolean isDeleted() { return this.status == ProductStatus.ELIMINADO; }
```

- [ ] **Step 3: Update `CategoryEntity` — add `ELIMINADA` and `parentId`**

```java
// Add to CategoryEntity:
@Column(name = "parent_id")
private UUID parentId;

// Update enum:
public enum CategoryStatus {
    ACTIVA, INACTIVA, ELIMINADA
}
```

- [ ] **Step 4: Update `ProductEntity` — add `ELIMINADO`**

```java
// Update ProductEntity.ProductStatus enum:
public enum ProductStatus {
    ACTIVO, INACTIVO, ELIMINADO
}
```

- [ ] **Step 5: Update `DeactivateCategoryUseCaseImpl` to use `ELIMINADA` for logical delete**

```java
// The deactivate use case currently sets INACTIVA. 
// For logical delete (ELIMINADA), the use case should set category.setStatus(Category.CategoryStatus.ELIMINADA)
// Read the current file first, then update the logic so that the deactivate endpoint
// sets status to ELIMINADA (soft delete) and verifies no active products first.
```

- [ ] **Step 6: Update `DeactivateProductUseCaseImpl` to use `ELIMINADO` for logical delete**

```java
// Set product.setStatus(Product.ProductStatus.ELIMINADO) for logical delete
```

- [ ] **Step 7: Update DB script for catalog-service**

```sql
-- axisERP-db-scripts/catalog-service/database.sql
-- Add parent_id to categories, fix status values

DROP TABLE IF EXISTS product_barcodes CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id UUID REFERENCES categories(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVA' CHECK (status IN ('ACTIVA','INACTIVA','ELIMINADA')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    category_id UUID NOT NULL REFERENCES categories(id),
    purchase_price DECIMAL(10,2) NOT NULL,
    sale_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO' CHECK (status IN ('ACTIVO','INACTIVO','ELIMINADO')),
    created_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE product_barcodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id),
    barcode VARCHAR(50) NOT NULL UNIQUE,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categories_status ON categories(status);
CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_codigo ON products(codigo);
CREATE INDEX idx_product_barcodes_product_id ON product_barcodes(product_id);
```

- [ ] **Step 8: Commit**

```bash
git add catalog-service/src/ axisERP-db-scripts/catalog-service/database.sql
git commit -m "fix: add ELIMINADA/ELIMINADO status and parent_id subcategory support to catalog-service"
```

---

## Task 2: Create DB scripts for all missing services

**Files to create:**
- `axisERP-db-scripts/auth-service/database.sql`
- `axisERP-db-scripts/inventory-service/database.sql`
- `axisERP-db-scripts/purchase-service/database.sql`
- `axisERP-db-scripts/sales-service/database.sql`
- `axisERP-db-scripts/report-service/database.sql`

**Business rules covered:** All DB schema rules for each domain.

- [ ] **Step 1: Create auth-service/database.sql**

```sql
-- AxisERP - Auth Service Database Script
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS token_blacklist CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (status IN ('PENDIENTE','ACTIVO','INACTIVO','ELIMINADO')),
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO roles (name) VALUES ('ADMIN'), ('VENDEDOR'), ('INVENTARIO');

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    token VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    token VARCHAR(255) NOT NULL UNIQUE,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE token_blacklist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(1024) NOT NULL UNIQUE,
    blacklisted_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

- [ ] **Step 2: Create inventory-service/database.sql**

```sql
-- AxisERP - Inventory Service Database Script
DROP TABLE IF EXISTS inventory_movements CASCADE;
DROP TABLE IF EXISTS inventories CASCADE;

CREATE TABLE inventories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE,
    current_stock INT NOT NULL DEFAULT 0 CHECK (current_stock >= 0),
    min_stock INT NOT NULL DEFAULT 0 CHECK (min_stock >= 0),
    max_stock INT NOT NULL DEFAULT 0 CHECK (max_stock >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id UUID NOT NULL REFERENCES inventories(id),
    product_id UUID NOT NULL,
    movement_type VARCHAR(30) NOT NULL CHECK (movement_type IN (
        'INVENTARIO_INICIAL','ENTRADA','SALIDA',
        'AJUSTE_POSITIVO','AJUSTE_NEGATIVO','DEVOLUCION','ANULACION'
    )),
    quantity INT NOT NULL CHECK (quantity > 0),
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    reference_type VARCHAR(50),
    reference_id UUID,
    justification TEXT,
    notes TEXT,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inventories_product_id ON inventories(product_id);
CREATE INDEX idx_movements_inventory_id ON inventory_movements(inventory_id);
CREATE INDEX idx_movements_product_id ON inventory_movements(product_id);
CREATE INDEX idx_movements_type ON inventory_movements(movement_type);
CREATE INDEX idx_movements_created_at ON inventory_movements(created_at);
```

- [ ] **Step 3: Create purchase-service/database.sql**

```sql
-- AxisERP - Purchase Service Database Script
DROP TABLE IF EXISTS purchase_items CASCADE;
DROP TABLE IF EXISTS purchases CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;

CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    nit VARCHAR(20) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO' CHECK (status IN ('ACTIVO','INACTIVO','ELIMINADO')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    purchase_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'BORRADOR' CHECK (status IN ('BORRADOR','PENDIENTE','RECIBIDA','PAGADA','CANCELADA')),
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_id UUID NOT NULL REFERENCES purchases(id),
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    received_quantity INT NOT NULL DEFAULT 0 CHECK (received_quantity >= 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price > 0),
    subtotal DECIMAL(12,2) NOT NULL
);

CREATE UNIQUE INDEX idx_purchase_items_unique ON purchase_items(purchase_id, product_id);
CREATE INDEX idx_purchases_supplier_id ON purchases(supplier_id);
CREATE INDEX idx_purchases_status ON purchases(status);
CREATE INDEX idx_purchases_number ON purchases(purchase_number);
```

- [ ] **Step 4: Create sales-service/database.sql**

```sql
-- AxisERP - Sales Service Database Script
DROP TABLE IF EXISTS invoice_items CASCADE;
DROP TABLE IF EXISTS invoices CASCADE;
DROP TABLE IF EXISTS sale_items CASCADE;
DROP TABLE IF EXISTS sales CASCADE;
DROP TABLE IF EXISTS customers CASCADE;

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    document_type VARCHAR(20) NOT NULL DEFAULT 'CC',
    document_number VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVO' CHECK (status IN ('ACTIVO','INACTIVO','ELIMINADO')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    sale_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'BORRADOR' CHECK (status IN ('BORRADOR','PENDIENTE','CONFIRMADA','PAGADA','ANULADA')),
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL REFERENCES sales(id),
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price > 0),
    discount DECIMAL(10,2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(12,2) NOT NULL
);

CREATE UNIQUE INDEX idx_sale_items_unique ON sale_items(sale_id, product_id);

CREATE SEQUENCE invoice_number_seq START 1;

CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL UNIQUE REFERENCES sales(id),
    invoice_number BIGINT NOT NULL UNIQUE DEFAULT nextval('invoice_number_seq'),
    customer_snapshot JSONB NOT NULL,
    items_snapshot JSONB NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sales_customer_id ON sales(customer_id);
CREATE INDEX idx_sales_status ON sales(status);
CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_invoices_sale_id ON invoices(sale_id);
CREATE INDEX idx_invoices_number ON invoices(invoice_number);
```

- [ ] **Step 5: Create report-service/database.sql**

```sql
-- AxisERP - Report Service Database Script
DROP TABLE IF EXISTS export_history CASCADE;

CREATE TABLE export_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_type VARCHAR(50) NOT NULL,
    export_format VARCHAR(10) NOT NULL CHECK (export_format IN ('PDF','EXCEL','CSV')),
    filters JSONB,
    exported_by UUID NOT NULL,
    exported_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_export_history_exported_by ON export_history(exported_by);
CREATE INDEX idx_export_history_report_type ON export_history(report_type);
CREATE INDEX idx_export_history_exported_at ON export_history(exported_at);
```

- [ ] **Step 6: Commit DB scripts**

```bash
git add axisERP-db-scripts/
git commit -m "feat: add DB scripts for all services (auth, inventory, purchase, sales, report)"
```

---

## Task 3: Implement inventory-service (full hexagonal architecture)

**Context:** Pattern follows auth-service and catalog-service exactly. Java 21, Spring Boot 3.5.x, Lombok, Spring Data JPA, PostgreSQL.

**Business rules:** All 20 inventory rules from the requirements document.

**Base package:** `com.axiserp.inventory`

**Files to create/replace:**

### Domain Layer
- `domain/model/Inventory.java` — aggregate with current/min/max stock, productId, version (optimistic locking)
- `domain/model/InventoryMovement.java` — movement record (type, qty, prevStock, newStock, userId, notes, justification, referenceType, referenceId)
- `domain/model/MovementType.java` — enum: INVENTARIO_INICIAL, ENTRADA, SALIDA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO, DEVOLUCION, ANULACION
- `domain/exception/InsufficientStockException.java`
- `domain/exception/InventoryAlreadyInitializedException.java`
- `domain/exception/InventoryNotFoundException.java`
- `domain/exception/InvalidStockConfigException.java`
- `domain/exception/NegativeQuantityException.java`

### Ports
- `ports/input/InitializeInventoryUseCase.java`
- `ports/input/RegisterEntryUseCase.java`
- `ports/input/RegisterExitUseCase.java`
- `ports/input/RegisterAdjustmentUseCase.java`
- `ports/input/RegisterReturnUseCase.java`
- `ports/input/ReverseMovementUseCase.java`
- `ports/input/GetInventoryUseCase.java`
- `ports/input/ListMovementsUseCase.java`
- `ports/output/InventoryRepositoryPort.java`

### Application Layer
- `application/dto/request/InitializeInventoryRequest.java`
- `application/dto/request/RegisterMovementRequest.java`
- `application/dto/request/AdjustmentRequest.java`
- `application/dto/response/InventoryResponse.java`
- `application/dto/response/MovementResponse.java`
- `application/shared/RequestContext.java` (already exists — keep)
- `application/usecase/InitializeInventoryUseCaseImpl.java`
- `application/usecase/RegisterEntryUseCaseImpl.java`
- `application/usecase/RegisterExitUseCaseImpl.java`
- `application/usecase/RegisterAdjustmentUseCaseImpl.java`
- `application/usecase/RegisterReturnUseCaseImpl.java`
- `application/usecase/ReverseMovementUseCaseImpl.java`
- `application/usecase/GetInventoryUseCaseImpl.java`
- `application/usecase/ListMovementsUseCaseImpl.java`

### Infrastructure Layer
- `infrastructure/adapters/out/persistence/entity/InventoryEntity.java` — @Version for optimistic locking
- `infrastructure/adapters/out/persistence/entity/InventoryMovementEntity.java`
- `infrastructure/adapters/out/persistence/repository/JpaInventoryRepository.java`
- `infrastructure/adapters/out/persistence/repository/JpaMovementRepository.java` (already exists — update)
- `infrastructure/adapters/out/persistence/adapter/InventoryRepositoryAdapter.java` (already exists — replace)
- `infrastructure/adapters/in/web/controller/InventoryController.java` (already exists — replace)
- `infrastructure/adapters/in/web/exception/GlobalExceptionHandler.java`
- `infrastructure/config/SecurityConfig.java` (already exists — keep)
- `infrastructure/security/JwtAuthenticationFilter.java` (already exists — keep)

**Key implementation details:**
- `InventoryEntity` must have `@Version private Long version` for optimistic locking (rule 18-19)
- `InitializeInventoryUseCaseImpl`: check if inventory already exists for productId (rule 6), set INVENTARIO_INICIAL movement
- `RegisterExitUseCaseImpl`: check `currentStock >= quantity` before exit (rules 9, 10)
- `RegisterAdjustmentUseCaseImpl`: requires justification (rule 13)
- `ReverseMovementUseCaseImpl`: finds original movement, creates ANULACION movement (rule 14)
- All use cases: record previousStock and newStock in movement (rule 11)
- Controller: expose alerts endpoint (low stock where current <= min, depleted where current == 0)

- [ ] **Step 1: Create domain models**

```java
// Inventory.java
package com.axiserp.inventory.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Inventory {
    private UUID id;
    private UUID productId;
    private int currentStock;
    private int minStock;
    private int maxStock;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isLowStock() { return currentStock > 0 && currentStock <= minStock; }
    public boolean isDepleted() { return currentStock == 0; }
    public boolean canExit(int qty) { return currentStock >= qty; }
}
```

```java
// MovementType.java
package com.axiserp.inventory.domain.model;

public enum MovementType {
    INVENTARIO_INICIAL, ENTRADA, SALIDA,
    AJUSTE_POSITIVO, AJUSTE_NEGATIVO, DEVOLUCION, ANULACION
}
```

```java
// InventoryMovement.java
package com.axiserp.inventory.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryMovement {
    private UUID id;
    private UUID inventoryId;
    private UUID productId;
    private MovementType movementType;
    private int quantity;
    private int previousStock;
    private int newStock;
    private String referenceType;
    private UUID referenceId;
    private String justification;
    private String notes;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: Create domain exceptions**

```java
// InsufficientStockException.java
package com.axiserp.inventory.domain.exception;
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(int requested, int available) {
        super("Stock insuficiente: solicitado=" + requested + " disponible=" + available);
    }
}
```

Create similarly:
- `InventoryAlreadyInitializedException.java`: `super("El inventario ya fue inicializado para este producto")`
- `InventoryNotFoundException.java`: `super("Inventario no encontrado para productId=" + productId)`
- `InvalidStockConfigException.java`: `super("Configuracion de stock invalida: max debe ser mayor que min")`
- `NegativeQuantityException.java`: `super("La cantidad debe ser mayor que cero")`

- [ ] **Step 3: Create input port interfaces**

```java
// InitializeInventoryUseCase.java
package com.axiserp.inventory.ports.input;
import com.axiserp.inventory.application.dto.request.InitializeInventoryRequest;
import com.axiserp.inventory.application.dto.response.InventoryResponse;
import java.util.UUID;
public interface InitializeInventoryUseCase {
    InventoryResponse initialize(InitializeInventoryRequest request, UUID userId);
}
```

Create similarly for each use case. `RegisterEntryUseCase` and `RegisterExitUseCase` and `RegisterReturnUseCase` have signature:
```java
InventoryResponse register(UUID productId, int quantity, String referenceType, UUID referenceId, String notes, UUID userId);
```

`RegisterAdjustmentUseCase`:
```java
InventoryResponse adjust(UUID productId, int quantity, MovementType type, String justification, UUID userId);
```

`ReverseMovementUseCase`:
```java
InventoryResponse reverse(UUID movementId, String justification, UUID userId);
```

`GetInventoryUseCase`:
```java
InventoryResponse getByProductId(UUID productId);
```

`ListMovementsUseCase`:
```java
List<MovementResponse> listByProductId(UUID productId);
```

- [ ] **Step 4: Create output port**

```java
// InventoryRepositoryPort.java
package com.axiserp.inventory.ports.output;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface InventoryRepositoryPort {
    Optional<Inventory> findByProductId(UUID productId);
    Inventory save(Inventory inventory);
    InventoryMovement saveMovement(InventoryMovement movement);
    Optional<InventoryMovement> findMovementById(UUID movementId);
    List<InventoryMovement> findMovementsByProductId(UUID productId);
}
```

- [ ] **Step 5: Create DTOs**

```java
// InitializeInventoryRequest.java
package com.axiserp.inventory.application.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InitializeInventoryRequest {
    @NotNull private UUID productId;
    @Min(0) private int initialStock;
    @Min(0) private int minStock;
    @Min(0) private int maxStock;
}
```

```java
// InventoryResponse.java
package com.axiserp.inventory.application.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryResponse {
    private UUID id;
    private UUID productId;
    private int currentStock;
    private int minStock;
    private int maxStock;
    private boolean lowStock;
    private boolean depleted;
    private LocalDateTime updatedAt;
}
```

```java
// MovementResponse.java
package com.axiserp.inventory.application.dto.response;
import com.axiserp.inventory.domain.model.MovementType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MovementResponse {
    private UUID id;
    private UUID productId;
    private MovementType movementType;
    private int quantity;
    private int previousStock;
    private int newStock;
    private String referenceType;
    private UUID referenceId;
    private String justification;
    private String notes;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 6: Create use case implementations**

```java
// InitializeInventoryUseCaseImpl.java
package com.axiserp.inventory.application.usecase;

import com.axiserp.inventory.application.dto.request.InitializeInventoryRequest;
import com.axiserp.inventory.application.dto.response.InventoryResponse;
import com.axiserp.inventory.domain.exception.InvalidStockConfigException;
import com.axiserp.inventory.domain.exception.InventoryAlreadyInitializedException;
import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.ports.input.InitializeInventoryUseCase;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class InitializeInventoryUseCaseImpl implements InitializeInventoryUseCase {
    private static final Logger log = LoggerFactory.getLogger(InitializeInventoryUseCaseImpl.class);
    private final InventoryRepositoryPort inventoryRepository;

    @Override
    @Transactional
    public InventoryResponse initialize(InitializeInventoryRequest req, UUID userId) {
        if (inventoryRepository.findByProductId(req.getProductId()).isPresent()) {
            throw new InventoryAlreadyInitializedException();
        }
        if (req.getMaxStock() > 0 && req.getMaxStock() <= req.getMinStock()) {
            throw new InvalidStockConfigException();
        }
        Inventory inventory = Inventory.builder()
            .productId(req.getProductId())
            .currentStock(req.getInitialStock())
            .minStock(req.getMinStock())
            .maxStock(req.getMaxStock())
            .build();
        Inventory saved = inventoryRepository.save(inventory);
        if (req.getInitialStock() > 0) {
            inventoryRepository.saveMovement(InventoryMovement.builder()
                .inventoryId(saved.getId())
                .productId(req.getProductId())
                .movementType(MovementType.INVENTARIO_INICIAL)
                .quantity(req.getInitialStock())
                .previousStock(0)
                .newStock(req.getInitialStock())
                .createdBy(userId)
                .build());
        }
        log.info("inventory_initialized productId={} initialStock={}", req.getProductId(), req.getInitialStock());
        return toResponse(saved);
    }

    private InventoryResponse toResponse(Inventory i) {
        return InventoryResponse.builder()
            .id(i.getId()).productId(i.getProductId())
            .currentStock(i.getCurrentStock()).minStock(i.getMinStock())
            .maxStock(i.getMaxStock()).lowStock(i.isLowStock())
            .depleted(i.isDepleted()).updatedAt(i.getUpdatedAt()).build();
    }
}
```

Implement similarly:
- `RegisterEntryUseCaseImpl`: find inventory, add qty, save movement type ENTRADA
- `RegisterExitUseCaseImpl`: check `canExit(qty)`, subtract qty, save movement type SALIDA
- `RegisterReturnUseCaseImpl`: add qty, save movement type DEVOLUCION
- `RegisterAdjustmentUseCaseImpl`: if AJUSTE_POSITIVO add qty, if AJUSTE_NEGATIVO subtract (check stock), justification required
- `ReverseMovementUseCaseImpl`: find original movement, create inverse, save ANULACION
- `GetInventoryUseCaseImpl`: find by productId or throw InventoryNotFoundException
- `ListMovementsUseCaseImpl`: find all movements for productId

- [ ] **Step 7: Create JPA entities**

```java
// InventoryEntity.java
package com.axiserp.inventory.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "inventories")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "current_stock", nullable = false)
    private int currentStock;

    @Column(name = "min_stock", nullable = false)
    private int minStock;

    @Column(name = "max_stock", nullable = false)
    private int maxStock;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() {
        createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now();
    }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
```

```java
// InventoryMovementEntity.java
package com.axiserp.inventory.infrastructure.adapters.out.persistence.entity;

import com.axiserp.inventory.domain.model.MovementType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "inventory_movements")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryMovementEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "inventory_id", nullable = false)
    private UUID inventoryId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private MovementType movementType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "previous_stock", nullable = false)
    private int previousStock;

    @Column(name = "new_stock", nullable = false)
    private int newStock;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    private String justification;
    private String notes;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
```

- [ ] **Step 8: Create JPA repositories**

```java
// JpaInventoryRepository.java
package com.axiserp.inventory.infrastructure.adapters.out.persistence.repository;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaInventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    Optional<InventoryEntity> findByProductId(UUID productId);
}
```

```java
// JpaMovementRepository.java (replace existing)
package com.axiserp.inventory.infrastructure.adapters.out.persistence.repository;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JpaMovementRepository extends JpaRepository<InventoryMovementEntity, UUID> {
    List<InventoryMovementEntity> findByProductIdOrderByCreatedAtDesc(UUID productId);
}
```

- [ ] **Step 9: Create repository adapter**

```java
// InventoryRepositoryAdapter.java (replace existing)
package com.axiserp.inventory.infrastructure.adapters.out.persistence.adapter;

import com.axiserp.inventory.domain.model.Inventory;
import com.axiserp.inventory.domain.model.InventoryMovement;
import com.axiserp.inventory.domain.model.MovementType;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryEntity;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.entity.InventoryMovementEntity;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.repository.JpaInventoryRepository;
import com.axiserp.inventory.infrastructure.adapters.out.persistence.repository.JpaMovementRepository;
import com.axiserp.inventory.ports.output.InventoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component @RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {
    private final JpaInventoryRepository inventoryRepo;
    private final JpaMovementRepository movementRepo;

    @Override
    public Optional<Inventory> findByProductId(UUID productId) {
        return inventoryRepo.findByProductId(productId).map(this::toDomain);
    }

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = toEntity(inventory);
        return toDomain(inventoryRepo.save(entity));
    }

    @Override
    public InventoryMovement saveMovement(InventoryMovement movement) {
        return toMovementDomain(movementRepo.save(toMovementEntity(movement)));
    }

    @Override
    public Optional<InventoryMovement> findMovementById(UUID movementId) {
        return movementRepo.findById(movementId).map(this::toMovementDomain);
    }

    @Override
    public List<InventoryMovement> findMovementsByProductId(UUID productId) {
        return movementRepo.findByProductIdOrderByCreatedAtDesc(productId)
            .stream().map(this::toMovementDomain).toList();
    }

    private Inventory toDomain(InventoryEntity e) {
        return Inventory.builder().id(e.getId()).productId(e.getProductId())
            .currentStock(e.getCurrentStock()).minStock(e.getMinStock())
            .maxStock(e.getMaxStock()).version(e.getVersion())
            .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
    }

    private InventoryEntity toEntity(Inventory i) {
        return InventoryEntity.builder().id(i.getId()).productId(i.getProductId())
            .currentStock(i.getCurrentStock()).minStock(i.getMinStock())
            .maxStock(i.getMaxStock()).version(i.getVersion()).build();
    }

    private InventoryMovement toMovementDomain(InventoryMovementEntity e) {
        return InventoryMovement.builder().id(e.getId()).inventoryId(e.getInventoryId())
            .productId(e.getProductId()).movementType(e.getMovementType())
            .quantity(e.getQuantity()).previousStock(e.getPreviousStock())
            .newStock(e.getNewStock()).referenceType(e.getReferenceType())
            .referenceId(e.getReferenceId()).justification(e.getJustification())
            .notes(e.getNotes()).createdBy(e.getCreatedBy()).createdAt(e.getCreatedAt()).build();
    }

    private InventoryMovementEntity toMovementEntity(InventoryMovement m) {
        return InventoryMovementEntity.builder().id(m.getId()).inventoryId(m.getInventoryId())
            .productId(m.getProductId()).movementType(m.getMovementType())
            .quantity(m.getQuantity()).previousStock(m.getPreviousStock())
            .newStock(m.getNewStock()).referenceType(m.getReferenceType())
            .referenceId(m.getReferenceId()).justification(m.getJustification())
            .notes(m.getNotes()).createdBy(m.getCreatedBy()).build();
    }
}
```

- [ ] **Step 10: Create REST controller**

```java
// InventoryController.java (replace existing)
package com.axiserp.inventory.infrastructure.adapters.in.web.controller;

import com.axiserp.inventory.application.dto.request.AdjustmentRequest;
import com.axiserp.inventory.application.dto.request.InitializeInventoryRequest;
import com.axiserp.inventory.application.dto.response.InventoryResponse;
import com.axiserp.inventory.application.dto.response.MovementResponse;
import com.axiserp.inventory.ports.input.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InitializeInventoryUseCase initializeUseCase;
    private final RegisterEntryUseCase registerEntryUseCase;
    private final RegisterExitUseCase registerExitUseCase;
    private final RegisterAdjustmentUseCase registerAdjustmentUseCase;
    private final RegisterReturnUseCase registerReturnUseCase;
    private final ReverseMovementUseCase reverseMovementUseCase;
    private final GetInventoryUseCase getInventoryUseCase;
    private final ListMovementsUseCase listMovementsUseCase;

    private UUID currentUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @PostMapping("/initialize")
    public ResponseEntity<InventoryResponse> initialize(@Valid @RequestBody InitializeInventoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(initializeUseCase.initialize(req, currentUserId()));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryResponse> getByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(getInventoryUseCase.getByProductId(productId));
    }

    @GetMapping("/products/{productId}/movements")
    public ResponseEntity<List<MovementResponse>> getMovements(@PathVariable UUID productId) {
        return ResponseEntity.ok(listMovementsUseCase.listByProductId(productId));
    }

    @PostMapping("/products/{productId}/entry")
    public ResponseEntity<InventoryResponse> entry(@PathVariable UUID productId,
        @RequestParam int quantity, @RequestParam(required = false) String referenceType,
        @RequestParam(required = false) UUID referenceId, @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(registerEntryUseCase.register(productId, quantity, referenceType, referenceId, notes, currentUserId()));
    }

    @PostMapping("/products/{productId}/exit")
    public ResponseEntity<InventoryResponse> exit(@PathVariable UUID productId,
        @RequestParam int quantity, @RequestParam(required = false) String referenceType,
        @RequestParam(required = false) UUID referenceId, @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(registerExitUseCase.register(productId, quantity, referenceType, referenceId, notes, currentUserId()));
    }

    @PostMapping("/products/{productId}/return")
    public ResponseEntity<InventoryResponse> registerReturn(@PathVariable UUID productId,
        @RequestParam int quantity, @RequestParam(required = false) String referenceType,
        @RequestParam(required = false) UUID referenceId, @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(registerReturnUseCase.register(productId, quantity, referenceType, referenceId, notes, currentUserId()));
    }

    @PostMapping("/products/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjust(@PathVariable UUID productId,
        @Valid @RequestBody AdjustmentRequest req) {
        return ResponseEntity.ok(registerAdjustmentUseCase.adjust(productId, req.getQuantity(), req.getType(), req.getJustification(), currentUserId()));
    }

    @PostMapping("/movements/{movementId}/reverse")
    public ResponseEntity<InventoryResponse> reverse(@PathVariable UUID movementId,
        @RequestParam String justification) {
        return ResponseEntity.ok(reverseMovementUseCase.reverse(movementId, justification, currentUserId()));
    }
}
```

- [ ] **Step 11: Create GlobalExceptionHandler for inventory-service**

Same pattern as catalog-service GlobalExceptionHandler. Handle:
- `InsufficientStockException` → 409 CONFLICT
- `InventoryAlreadyInitializedException` → 409 CONFLICT
- `InventoryNotFoundException` → 404 NOT_FOUND
- `InvalidStockConfigException` → 400 BAD_REQUEST
- `NegativeQuantityException` → 400 BAD_REQUEST
- `MethodArgumentNotValidException` → 400 BAD_REQUEST
- `Exception` → 500 INTERNAL_SERVER_ERROR

- [ ] **Step 12: Update application.properties for inventory-service**

```properties
spring.application.name=inventory-service
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/axiserp_inventory}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
server.port=8087
management.endpoints.web.exposure.include=health,info
jwt.secret=${JWT_SECRET:a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2}
```

- [ ] **Step 13: Commit inventory-service implementation**

```bash
git add inventory-service/src/
git commit -m "feat: implement inventory-service with full hexagonal architecture and business rules"
```

---

## Task 4: Implement purchase-service (full hexagonal architecture)

**Business rules:** All Proveedores (rules 1-5) and Compras (rules 6-22) from requirements.

**Base package:** `com.axiserp.purchase`

**Files to create/replace:**

### Domain
- `domain/model/Supplier.java` — id, name, nit, phone, email, address, status, createdAt, updatedAt
- `domain/model/SupplierStatus.java` — enum: ACTIVO, INACTIVO, ELIMINADO
- `domain/model/Purchase.java` — id, supplierId, purchaseNumber, status, items, subtotal, tax, total, notes, createdBy, updatedBy
- `domain/model/PurchaseItem.java` — id, purchaseId, productId, productName, quantity, receivedQuantity, unitPrice, subtotal
- `domain/model/PurchaseStatus.java` — enum: BORRADOR, PENDIENTE, RECIBIDA, PAGADA, CANCELADA
- `domain/exception/SupplierNotFoundException.java`
- `domain/exception/SupplierInactiveException.java`
- `domain/exception/DuplicateNitException.java`
- `domain/exception/PurchaseNotFoundException.java`
- `domain/exception/PurchaseNotModifiableException.java`
- `domain/exception/DuplicateProductInPurchaseException.java`

### Ports
- `ports/input/CreateSupplierUseCase.java`
- `ports/input/GetSupplierUseCase.java`
- `ports/input/ListSuppliersUseCase.java`
- `ports/input/DeactivateSupplierUseCase.java`
- `ports/input/CreatePurchaseUseCase.java`
- `ports/input/GetPurchaseUseCase.java`
- `ports/input/ListPurchasesUseCase.java`
- `ports/input/ReceivePurchaseUseCase.java`
- `ports/input/UpdatePurchaseStatusUseCase.java`
- `ports/input/CancelPurchaseUseCase.java`
- `ports/output/SupplierRepositoryPort.java`
- `ports/output/PurchaseRepositoryPort.java`
- `ports/output/InventoryServicePort.java` — HTTP client port for inventory updates

### Application
- `application/dto/request/CreateSupplierRequest.java`
- `application/dto/request/CreatePurchaseRequest.java` — with nested list of PurchaseItemRequest
- `application/dto/request/ReceivePurchaseRequest.java` — with received quantities per item
- `application/dto/response/SupplierResponse.java`
- `application/dto/response/PurchaseResponse.java`
- `application/usecase/CreateSupplierUseCaseImpl.java`
- `application/usecase/GetSupplierUseCaseImpl.java`
- `application/usecase/ListSuppliersUseCaseImpl.java`
- `application/usecase/DeactivateSupplierUseCaseImpl.java`
- `application/usecase/CreatePurchaseUseCaseImpl.java` — validate supplier active, no duplicate products, calc subtotal/tax/total
- `application/usecase/GetPurchaseUseCaseImpl.java`
- `application/usecase/ListPurchasesUseCaseImpl.java`
- `application/usecase/ReceivePurchaseUseCaseImpl.java` — update receivedQty, if fully received → RECIBIDA, call inventoryPort.registerEntry per item
- `application/usecase/UpdatePurchaseStatusUseCaseImpl.java` — validate state transitions
- `application/usecase/CancelPurchaseUseCaseImpl.java` — only cancellable if BORRADOR or PENDIENTE

### Infrastructure
- `infrastructure/adapters/out/persistence/entity/SupplierEntity.java`
- `infrastructure/adapters/out/persistence/entity/PurchaseEntity.java`
- `infrastructure/adapters/out/persistence/entity/PurchaseItemEntity.java`
- `infrastructure/adapters/out/persistence/repository/JpaSupplierRepository.java`
- `infrastructure/adapters/out/persistence/repository/JpaPurchaseRepository.java`
- `infrastructure/adapters/out/persistence/repository/JpaPurchaseItemRepository.java`
- `infrastructure/adapters/out/persistence/adapter/SupplierRepositoryAdapter.java`
- `infrastructure/adapters/out/persistence/adapter/PurchaseRepositoryAdapter.java`
- `infrastructure/adapters/out/http/InventoryServiceAdapter.java` — RestTemplate/WebClient calling inventory-service
- `infrastructure/adapters/in/web/controller/SupplierController.java`
- `infrastructure/adapters/in/web/controller/PurchaseController.java`
- `infrastructure/adapters/in/web/exception/GlobalExceptionHandler.java`
- `infrastructure/config/RestTemplateConfig.java` — RestTemplate bean with INTERNAL_API_KEY header

**Key logic:**
- Tax rate: 19% (IVA Colombia)
- Purchase number: auto-generated UUID prefix (e.g., `PO-{UUID short}`)
- State machine: BORRADOR → PENDIENTE → RECIBIDA → PAGADA; BORRADOR/PENDIENTE → CANCELADA
- `ReceivePurchaseUseCaseImpl`: for each item, update receivedQuantity, call `inventoryPort.registerEntry(productId, quantity, "COMPRA", purchaseId, notes)`
- Cancelled/paid purchases cannot be modified

- [ ] **Step 1–12:** Follow same pattern as inventory-service — create all domain models, exceptions, ports, DTOs, use cases, entities, repos, adapters, controller, exception handler, application.properties.

- [ ] **Step 13: Commit purchase-service implementation**

```bash
git add purchase-service/src/
git commit -m "feat: implement purchase-service with suppliers, purchases, and hexagonal architecture"
```

---

## Task 5: Implement sales-service (full hexagonal architecture)

**Business rules:** All Clientes (rules 1-4), Ventas (rules 5-22), and Facturas (rules 23-27) from requirements.

**Base package:** `com.axiserp.sales`

**Domain models:**
- `Customer.java` — id, name, documentType, documentNumber, email, phone, address, status
- `CustomerStatus.java` — enum: ACTIVO, INACTIVO, ELIMINADO
- `Sale.java` — id, customerId, saleNumber, status, items, subtotal, discount, tax, total, notes, createdBy, updatedBy, version
- `SaleItem.java` — id, saleId, productId, productName, quantity, unitPrice, discount, subtotal
- `SaleStatus.java` — enum: BORRADOR, PENDIENTE, CONFIRMADA, PAGADA, ANULADA
- `Invoice.java` — id, saleId, invoiceNumber, customerSnapshot (String JSON), itemsSnapshot (String JSON), subtotal, discount, tax, total, issuedAt

**Ports input:**
- `CreateCustomerUseCase`, `GetCustomerUseCase`, `ListCustomersUseCase`, `DeactivateCustomerUseCase`
- `CreateSaleUseCase`, `GetSaleUseCase`, `ListSalesUseCase`
- `ConfirmSaleUseCase` — validate stock via inventoryPort, reduce stock, generate invoice
- `PaySaleUseCase` — set PAGADA
- `VoidSaleUseCase` — only ADMIN/VENDEDOR authorized, restore stock via inventoryPort.registerReturn, set ANULADA

**Ports output:**
- `CustomerRepositoryPort`, `SaleRepositoryPort`, `InvoiceRepositoryPort`
- `InventoryServicePort` — check stock (GET /api/v1/inventory/products/{id}) and register exit/return

**Key logic:**
- Tax rate: 19%
- Sale number: auto-generated
- Invoice number: sequential (from DB sequence `invoice_number_seq`)
- `ConfirmSaleUseCaseImpl`: 
  1. Validate sale is in BORRADOR/PENDIENTE
  2. For each item: call inventoryPort.checkStock(productId, quantity) — throw if insufficient
  3. For each item: call inventoryPort.registerExit(productId, quantity, "VENTA", saleId, null)
  4. Set sale status to CONFIRMADA
  5. Generate invoice with customer snapshot and items snapshot
- `VoidSaleUseCaseImpl`:
  1. Validate sale is CONFIRMADA or PAGADA
  2. For each item: call inventoryPort.registerReturn(productId, quantity, "DEVOLUCION_VENTA", saleId, null)
  3. Set sale status to ANULADA
- Concurrency: `SaleEntity` must have `@Version Long version` for optimistic locking
- Discount > 30% requires ADMIN role (rule 12)

**Files to create:** Same pattern as purchase-service but for sales domain. Include:
- All domain models, exceptions, ports, DTOs, use cases, entities, JPA repos, adapters, controllers, exception handler, RestTemplateConfig, application.properties

- [ ] **Step 1–13:** Create all files following the established pattern.

- [ ] **Step 14: Commit sales-service implementation**

```bash
git add sales-service/src/
git commit -m "feat: implement sales-service with customers, sales, invoices, and hexagonal architecture"
```

---

## Task 6: Implement report-service (full hexagonal architecture)

**Business rules:** All 13 report rules from requirements.

**Base package:** `com.axiserp.report`

**Architecture decision:** Report-service makes HTTP calls to other services using INTERNAL_API_KEY. It maintains its own `export_history` table.

**Domain models:**
- `ExportHistory.java` — id, reportType, exportFormat, filters (String JSON), exportedBy, exportedAt
- `ExportFormat.java` — enum: PDF, EXCEL, CSV
- `ReportType.java` — enum: VENTAS, INVENTARIO, TOP_PRODUCTOS, CLIENTES_FRECUENTES, DASHBOARD

**Ports input:**
- `GenerateSalesReportUseCase` — filters: dateFrom, dateTo, sellerId, customerId, status
- `GenerateInventoryReportUseCase` — filters: categoryId, lowStockOnly
- `GenerateTopProductsUseCase` — filters: dateFrom, dateTo, categoryId, limit
- `GenerateFrequentCustomersUseCase` — filters: dateFrom, dateTo, minPurchases
- `ExportReportUseCase` — take report type + filters + format, generate export, save to export_history

**Ports output:**
- `SalesServicePort` — HTTP client to sales-service
- `InventoryServicePort` — HTTP client to inventory-service
- `CatalogServicePort` — HTTP client to catalog-service
- `ExportHistoryRepositoryPort`

**Key implementation:**
- Use `RestTemplate` with `INTERNAL_API_KEY` header for inter-service calls
- CSV export: use simple StringBuilder (no external lib needed)
- Reports return structured data aggregated from multiple service calls
- All report endpoints: require authenticated user (JWT)
- Save export history on every export operation

**Files to create:** Same hexagonal pattern. Replace existing skeleton files.

- [ ] **Step 1–12:** Create all files following established pattern.

- [ ] **Step 13: Commit report-service implementation**

```bash
git add report-service/src/
git commit -m "feat: implement report-service with aggregated reports and export history"
```

---

## Self-Review

### Spec Coverage Check
- ✅ Auth service rules: existing implementation covers all 55 rules (JWT, rate limiting, audit, soft delete, password policy)
- ✅ Categories rules 1-19: covered by Task 1 (adding ELIMINADA + parent_id) and Task 2 (DB script)
- ✅ Products rules 1-23: covered by Task 1 (adding ELIMINADO) and Task 2 (DB script)
- ✅ Inventory rules 1-20: covered by Task 3 (all movement types, optimistic locking, stock validation, reversal)
- ✅ Suppliers rules 1-5: covered by Task 4
- ✅ Purchases rules 6-22: covered by Task 4 (state machine, partial reception, inventory update on receive)
- ✅ Customers rules 1-4: covered by Task 5
- ✅ Sales rules 5-22: covered by Task 5 (stock check, concurrency, void logic)
- ✅ Invoices rules 23-27: covered by Task 5 (sequential number, snapshot, immutable)
- ✅ Reports rules 1-13: covered by Task 6

### Placeholder Scan
- No TBD or TODO in code snippets
- Tax rate hardcoded at 19% (Colombian IVA) — this is explicit by design
- Discount limit at 30% for authorization — explicit

### Type Consistency
- `InventoryResponse` used consistently across inventory use cases
- `MovementType` enum referenced consistently
- `currentUserId()` helper used in all controllers

---

## Execution Order (Dependencies)

1. **Task 1** (catalog fixes) — independent
2. **Task 2** (DB scripts) — independent, can run parallel with Task 1
3. **Task 3** (inventory-service) — after Task 2
4. **Task 4** (purchase-service) + **Task 5** (sales-service) — after Task 3 (both depend on inventory)
5. **Task 6** (report-service) — after Tasks 4 and 5
