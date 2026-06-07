# AUDITORÍA COMPLETA DE POSTGRESQL NEON - AxisERP Platform
**Fecha:** 2026-06-07  
**Estado:** Análisis exhaustivo de consistencia entre esquema SQL y entidades Java

---

## RESUMEN EJECUTIVO

**Arquitectura:** 6 microservicios con base de datos PostgreSQL independientes en Neon  
**Configuración:** `spring.jpa.hibernate.ddl-auto=validate` (no genera tablas automáticamente)  
**Estado:** ⚠️ **CRÍTICO** - Inconsistencias detectadas entre entidades Java y esquema esperado

---

## 1. TABLAS DEFINIDAS (Según entidades Java)

### Tabla 1: auth_service (Puerto: 8081)
| Tabla | Entidad Java | Estado | Observaciones |
|-------|--------------|--------|---------------|
| `profiles` | UserEntity | ⚠️ | ID es UUID sin @GeneratedValue |
| `roles` | RoleEntity | ✓ | @GeneratedValue(UUID) correcto |
| `audit_log` | AuditLogEntity | ⚠️ | Tabla compartida, DDL en schema-init.sql |
| `password_reset_tokens` | PasswordResetTokenEntity | ? | Entidad existe pero no revisada |
| `refresh_tokens` | RefreshTokenEntity | ? | Entidad existe pero no revisada |
| `token_blacklist` | TokenBlacklistEntity | ? | Entidad existe pero no revisada |
| `password_history` | PasswordHistoryEntity | ? | Entidad existe pero no revisada |

### Tabla 2: catalog_service (Puerto: 8082)
| Tabla | Entidad Java | Estado | Observaciones |
|-------|--------------|--------|---------------|
| `categories` | CategoryEntity | ✓ | @GeneratedValue(UUID), estructura completa |
| `products` | ProductEntity | ⚠️ | ID es UUID sin @GeneratedValue |
| `product_barcodes` | ProductBarcodeEntity | ? | Entidad existe pero no revisada |
| `catalog_audit_log` | CatalogAuditLogEntity | ? | Entidad existe pero no revisada |

### Tabla 3: sales_service (Puerto: 8083)
| Tabla | Entidad Java | Estado | Observaciones |
|-------|--------------|--------|---------------|
| `customers` | CustomerEntity | ✓ | @GeneratedValue(UUID) correcto |
| `sales` | SaleEntity | ✓ | @GeneratedValue(UUID) con @Version para optimistic locking |
| `sale_items` | SaleItemEntity | ✓ | @GeneratedValue(UUID) correcto |
| `invoices` | InvoiceEntity | ? | Entidad existe pero no revisada |
| `audit_log` | AuditLogEntity | ⚠️ | Tabla duplicada (problema de convención) |

### Tabla 4: inventory_service (Puerto: 8084)
| Tabla | Entidad Java | Estado | Observaciones |
|-------|--------------|--------|---------------|
| `inventory` | InventoryEntity | ⚠️ | ID es UUID sin @GeneratedValue, @Version presente |
| `inventory_movements` | InventoryMovementEntity | ✓ | @GeneratedValue(UUID) correcto |
| `adjustment_requests` | AdjustmentRequestEntity | ? | Entidad existe pero no revisada |
| `audit_log` | AuditLogEntity | ⚠️ | Tabla duplicada |

### Tabla 5: purchase_service (Puerto: 8085)
| Tabla | Entidad Java | Estado | Observaciones |
|-------|--------------|--------|---------------|
| `suppliers` | SupplierEntity | ? | Entidad existe pero no revisada |
| `purchases` | PurchaseEntity | ⚠️ | ID es UUID sin @GeneratedValue |
| `purchase_items` | PurchaseItemEntity | ⚠️ | ID es UUID sin @GeneratedValue |
| `audit_log` | AuditLogEntity | ⚠️ | Tabla duplicada |

### Tabla 6: report_service (Puerto: 8086)
| Tabla | Entidad Java | Estado | Observaciones |
|-------|--------------|--------|---------------|
| `report_templates` | ReportTemplateEntity | ? | Entidad existe pero no revisada |
| `report_cache` | ReportCacheEntity | ? | Entidad existe pero no revisada |
| `export_logs` | ExportLogEntity | ? | Entidad existe pero no revisada |

---

## 2. COLUMNAS Y TIPOS DETECTADOS

### Patrón Identificado: Inconsistencia en @GeneratedValue

#### ❌ PROBLEMAS CRÍTICOS:

**1. UserEntity (auth_service)**
```java
@Id
private UUID id;  // ❌ Sin @GeneratedValue - Debe ser asignado manualmente
```
**Esperado en BD:** `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`  
**Estado Actual:** ⚠️ Conflicto - Hibernate espera UUID pre-generado

**2. ProductEntity (catalog_service)**
```java
@Id
private UUID id;  // ❌ Sin @GeneratedValue
```
**Riesgo:** Errores si se intenta insertar sin especificar UUID

**3. PurchaseEntity (purchase_service)**
```java
@Id
private UUID id;  // ❌ Sin @GeneratedValue
```

**4. PurchaseItemEntity (purchase_service)**
```java
@Id
private UUID id;  // ❌ Sin @GeneratedValue
```

**5. InventoryEntity (inventory_service)**
```java
@Id
private UUID id;  // ❌ Sin @GeneratedValue
```

### ✓ IMPLEMENTACIONES CORRECTAS:

**RoleEntity, CategoryEntity, SaleEntity, CustomerEntity, SaleItemEntity, InventoryMovementEntity**
```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;  // ✓ Correcto
```

---

## 3. CONSTRAINTS DEFINIDOS

### PRIMARY KEYS (PK)
| Tabla | Columna | Estado | Nota |
|-------|---------|--------|------|
| `profiles` | id (UUID) | ✓ | Implícito en @Id |
| `roles` | id (UUID) | ✓ | Implícito en @Id |
| `categories` | id (UUID) | ✓ | Implícito en @Id |
| `products` | id (UUID) | ⚠️ | Sin @GeneratedValue |
| `customers` | id (UUID) | ✓ | Implícito en @Id |
| `sales` | id (UUID) | ✓ | Implícito en @Id |
| `sale_items` | id (UUID) | ✓ | Implícito en @Id |
| `inventory` | id (UUID) | ⚠️ | Sin @GeneratedValue |
| `inventory_movements` | id (UUID) | ✓ | Implícito en @Id |
| `purchases` | id (UUID) | ⚠️ | Sin @GeneratedValue |
| `purchase_items` | id (UUID) | ⚠️ | Sin @GeneratedValue |

### FOREIGN KEYS (FK) - DETECTADOS EN ENTIDADES

#### ❌ PROBLEMA MAYOR: Relaciones sin restricciones explícitas en BD

**SaleItemEntity → SaleEntity**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "sale_id", nullable = false)
private SaleEntity sale;
```
**Esperado en BD:** `FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE`

**PurchaseEntity → PurchaseItemEntity**
```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "purchase_id")
private List<PurchaseItemEntity> items;
```
**Estado:** Relación one-to-many sin FK explícita en BD - ¡RIESGO DE INTEGRIDAD!

**SaleEntity → SaleItemEntity**
```java
@OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
private List<SaleItemEntity> items;
```

#### REFERENCIAS DIRECTAS (sin relaciones)
- `ProductEntity.categoryId` → `CategoryEntity.id` (sin FK)
- `InventoryEntity.productId` → `ProductEntity.id` (sin FK)
- `SaleEntity.customerId` → `CustomerEntity.id` (sin FK)
- `PurchaseEntity.supplierId` → `SupplierEntity.id` (sin FK)
- `SaleItemEntity.productId` → `ProductEntity.id` (sin FK)
- `PurchaseItemEntity.productId` → `ProductEntity.id` (sin FK)
- `InventoryMovementEntity.productId` → `ProductEntity.id` (sin FK)

### UNIQUE CONSTRAINTS

| Tabla | Columna(s) | Anotación | Estado |
|-------|-----------|-----------|--------|
| `profiles` | email | `@Column(...unique=true)` | ✓ |
| `categories` | name | `@Column(...unique=true)` | ✓ |
| `products` | codigo | `@Column(...unique=true)` | ✓ |
| `customers` | codigo | `@Column(...unique=true)` | ✓ |
| `customers` | document_number | `@Column(...unique=true)` | ✓ |
| `customers` | email | `@Column(...unique=true)` | ✓ |
| `sales` | sale_number | `@Column(...unique=true)` | ✓ |
| `inventory` | product_id | `@Column(...unique=true)` | ✓ |
| `purchases` | purchase_number | `@Column(...unique=true)` | ✓ |

### NOT NULL CONSTRAINTS

**UserEntity:**
- ✓ name (NOT NULL)
- ✓ email (NOT NULL)
- ✓ role_id (NOT NULL)
- ✓ status (NOT NULL)
- ✓ created_at (NOT NULL)
- ✓ updated_at (NOT NULL)
- ✓ failed_login_attempts (NOT NULL, DEFAULT 0)

**ProductEntity:**
- ✓ name (NOT NULL)
- ✓ codigo (NOT NULL)
- ✓ category_id (NOT NULL)
- ✓ purchase_price (NOT NULL)
- ✓ sale_price (NOT NULL)
- ✓ status (NOT NULL)
- ✓ created_at (NOT NULL)
- ✓ updated_at (NOT NULL)

**SaleEntity:**
- ✓ customer_id (NOT NULL)
- ✓ sale_number (NOT NULL)
- ✓ status (NOT NULL)
- ✓ subtotal (NOT NULL)
- ✓ discount (NOT NULL)
- ✓ tax (NOT NULL)
- ✓ total (NOT NULL)
- ✓ created_at (NOT NULL)
- ✓ updated_at (NOT NULL)

**CustomerEntity:**
- ✓ codigo (NOT NULL)
- ✓ name (NOT NULL)
- ✓ document_type (NOT NULL)
- ✓ document_number (NOT NULL)
- ✓ status (NOT NULL)
- ✓ created_at (NOT NULL)
- ✓ updated_at (NOT NULL)

### CHECK CONSTRAINTS (ENUMS)

| Tabla | Columna | Enum | Valores |
|-------|---------|------|---------|
| `profiles` | status | UserStatus | PENDIENTE, ACTIVO, INACTIVO, ELIMINADO |
| `roles` | - | - | Sin enum |
| `categories` | status | CategoryStatus | ACTIVA, INACTIVA, ELIMINADA |
| `products` | status | ProductStatus | ACTIVO, INACTIVO, ELIMINADO |
| `customers` | status | CustomerStatus | ACTIVO, INACTIVO |
| `customers` | document_type | DocumentType | CC, NIT, PASAPORTE, CE |
| `sales` | status | SaleStatus | BORRADOR, PENDIENTE, CONFIRMADA, PAGADA, ANULADA |
| `purchases` | status | PurchaseStatus | BORRADOR, ENVIADA, PENDIENTE, RECIBIDA, PAGADA, CANCELADA, APROBADA |
| `inventory_movements` | movement_type | MovementType | (Enum en modelo domain) |
| `audit_log` | action | AuditAction | LOGIN, LOGOUT, CREATE, UPDATE, DELETE, DEACTIVATE, REACTIVATE, VOID, PASSWORD_RESET_REQUEST, PASSWORD_RESET_COMPLETE |

**Implementación en Hibernate:** `@Enumerated(EnumType.STRING)` ✓ (almacena como VARCHAR, no como INT)

---

## 4. ÍNDICES - ANÁLISIS Y RECOMENDACIONES

### ÍNDICES IMPLÍCITOS (en PK y UNIQUE)
- `profiles_pkey` (id)
- `profiles_email_key` (email - UNIQUE)
- `roles_pkey` (id)
- `categories_pkey` (id)
- `categories_name_key` (name - UNIQUE)
- `products_pkey` (id)
- `products_codigo_key` (codigo - UNIQUE)
- `customers_pkey` (id)
- `customers_codigo_key` (codigo - UNIQUE)
- `customers_document_number_key` (document_number - UNIQUE)
- `customers_email_key` (email - UNIQUE)
- `sales_pkey` (id)
- `sales_sale_number_key` (sale_number - UNIQUE)
- `sale_items_pkey` (id)
- `inventory_pkey` (id)
- `inventory_product_id_key` (product_id - UNIQUE)
- `inventory_movements_pkey` (id)
- `purchases_pkey` (id)
- `purchases_purchase_number_key` (purchase_number - UNIQUE)
- `purchase_items_pkey` (id)

### ❌ ÍNDICES FALTANTES - CRÍTICOS

#### 1. **Foreign Key Columns (FALTA CREAR)**
```sql
-- sales_service
CREATE INDEX idx_sales_customer_id ON sales(customer_id);
CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items(product_id);

-- catalog_service
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);

-- inventory_service
CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_inventory_movements_inventory_id ON inventory_movements(inventory_id);
CREATE INDEX idx_inventory_movements_product_id ON inventory_movements(product_id);

-- purchase_service
CREATE INDEX idx_purchases_supplier_id ON purchases(supplier_id);
CREATE INDEX idx_purchase_items_purchase_id ON purchase_items(purchase_id);
CREATE INDEX idx_purchase_items_product_id ON purchase_items(product_id);

-- Tablas de auditoría
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_entity_id ON audit_log(entity_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp DESC);
```

#### 2. **Índices en campos de búsqueda frecuente (RECOMENDADO)**
```sql
-- Búsquedas por timestamp
CREATE INDEX idx_users_created_at ON profiles(created_at DESC);
CREATE INDEX idx_products_created_at ON products(created_at DESC);
CREATE INDEX idx_sales_created_at ON sales(created_at DESC);
CREATE INDEX idx_purchases_created_at ON purchases(created_at DESC);
CREATE INDEX idx_inventory_movements_created_at ON inventory_movements(created_at DESC);

-- Búsquedas por status
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_sales_status ON sales(status);
CREATE INDEX idx_purchases_status ON purchases(status);
CREATE INDEX idx_customers_status ON customers(status);

-- Búsquedas por usuario
CREATE INDEX idx_sales_created_by ON sales(created_by);
CREATE INDEX idx_purchases_created_by ON purchases(created_by);
```

---

## 5. INTEGRIDAD REFERENCIAL

### ✓ CORRECTAMENTE IMPLEMENTADO EN HIBERNATE
- `SaleEntity.items` → `CascadeType.ALL, orphanRemoval=true`
- `PurchaseEntity.items` → `CascadeType.ALL, orphanRemoval=true`

### ❌ PROBLEMAS DE INTEGRIDAD REFERENCIAL

**Relaciones débiles sin FK en BD:**

1. **ProductEntity ← SaleItemEntity, PurchaseItemEntity, InventoryEntity**
   - ¿Qué pasa si se elimina un producto?
   - **Riesgo:** Referencias huérfanas

2. **CategoryEntity ← ProductEntity**
   - Sin FK explícita en BD
   - **Riesgo:** Productos con category_id inválido

3. **SupplierEntity ← PurchaseEntity**
   - Sin FK explícita en BD
   - **Riesgo:** Compras con supplier_id inválido

4. **CustomerEntity ← SaleEntity**
   - Sin FK explícita en BD
   - **Riesgo:** Ventas con customer_id inválido

### RECOMENDACIÓN: Añadir FKs en BD
```sql
ALTER TABLE products 
ADD CONSTRAINT fk_products_category_id 
FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT;

ALTER TABLE sales 
ADD CONSTRAINT fk_sales_customer_id 
FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT;

ALTER TABLE sale_items 
ADD CONSTRAINT fk_sale_items_sale_id 
FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE;

ALTER TABLE sale_items 
ADD CONSTRAINT fk_sale_items_product_id 
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;

ALTER TABLE inventory 
ADD CONSTRAINT fk_inventory_product_id 
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

ALTER TABLE purchases 
ADD CONSTRAINT fk_purchases_supplier_id 
FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE RESTRICT;

ALTER TABLE purchase_items 
ADD CONSTRAINT fk_purchase_items_purchase_id 
FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE;

ALTER TABLE purchase_items 
ADD CONSTRAINT fk_purchase_items_product_id 
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;

ALTER TABLE inventory_movements 
ADD CONSTRAINT fk_inventory_movements_product_id 
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;
```

---

## 6. MIGRACIONES - ESTADO DE EJECUCIÓN

### CONFIGURACIÓN JPA
```properties
spring.jpa.hibernate.ddl-auto=validate
```
**Significado:** Hibernatedocker NO genera/actualiza tablas automáticamente.  
**Comportamiento:** Valida que las tablas existan, pero NO crea migraciones.

### PROBLEMAS DETECTADOS

1. **Sin sistema de migraciones formal**
   - ❌ No hay Flyway
   - ❌ No hay Liquibase
   - ❌ No hay versionamiento de cambios de esquema

2. **Script inicial incompleto**
   - Archivo: `sales-service/src/main/resources/schema-init.sql`
   - Contiene: Solo sequence y tabla audit_log
   - Falta: 90% de las tablas

3. **Inconsistencias entre servicios**
   - Cada servicio tiene su BD en Neon (correcto)
   - Pero NO hay DDL para crear las tablas automáticamente
   - Se asume que las tablas ya existen

### RECOMENDACIÓN CRÍTICA
Implementar Flyway migrations:

```
auth-service/src/main/resources/db/migration/
├── V1__init_auth_schema.sql
├── V2__add_password_history.sql
└── V3__add_indexes.sql

catalog-service/src/main/resources/db/migration/
├── V1__init_catalog_schema.sql
└── V2__add_indexes.sql

sales-service/src/main/resources/db/migration/
├── V1__init_sales_schema.sql
└── V2__add_indexes.sql
```

---

## 7. CONSISTENCIA: ENTIDADES JAVA vs ESQUEMA SQL

### MAPA DE ENTIDADES POR SERVICIO

#### auth-service
```
UserEntity → Table: profiles
├─ id (UUID, PK)
├─ name (VARCHAR 255, NOT NULL)
├─ email (VARCHAR 255, UNIQUE, NOT NULL, CITEXT)
├─ role_id (UUID, NOT NULL) - ⚠️ Sin FK explícita
├─ status (VARCHAR 20, ENUM)
├─ created_by (UUID)
├─ updated_by (UUID)
├─ last_login_at (TIMESTAMP)
├─ created_at (TIMESTAMP, NOT NULL)
├─ updated_at (TIMESTAMP, NOT NULL)
├─ deleted_at (TIMESTAMP)
└─ failed_login_attempts (INT, DEFAULT 0)

RoleEntity → Table: roles
├─ id (UUID, PK, AUTO)
├─ name (VARCHAR 100, NOT NULL)
├─ description (VARCHAR 255, NOT NULL)
├─ created_by (UUID)
├─ created_at (TIMESTAMP, NOT NULL)
├─ updated_at (TIMESTAMP, NOT NULL)
└─ deleted_at (TIMESTAMP)
```

#### catalog-service
```
CategoryEntity → Table: categories
├─ id (UUID, PK, AUTO)
├─ name (VARCHAR 100, UNIQUE, NOT NULL)
├─ description (VARCHAR 500)
├─ parent_id (UUID) - ⚠️ Permite jerarquía (self-referencing)
├─ status (VARCHAR 20, ENUM)
├─ created_by (UUID)
├─ updated_by (UUID)
├─ created_at (TIMESTAMP, NOT NULL)
└─ updated_at (TIMESTAMP, NOT NULL)

ProductEntity → Table: products
├─ id (UUID, PK) ❌ Sin @GeneratedValue
├─ name (VARCHAR 255, NOT NULL)
├─ codigo (VARCHAR 100, UNIQUE, NOT NULL)
├─ description (TEXT)
├─ category_id (UUID, NOT NULL) - ⚠️ Sin FK
├─ purchase_price (NUMERIC 10,2, NOT NULL)
├─ sale_price (NUMERIC 10,2, NOT NULL)
├─ status (VARCHAR 20, ENUM)
├─ created_by (UUID)
├─ created_at (TIMESTAMP, NOT NULL)
└─ updated_at (TIMESTAMP, NOT NULL)
```

#### sales-service
```
CustomerEntity → Table: customers
├─ id (UUID, PK, AUTO)
├─ codigo (VARCHAR 20, UNIQUE, NOT NULL)
├─ name (VARCHAR 255, NOT NULL)
├─ document_type (VARCHAR 20, ENUM: CC, NIT, PASAPORTE, CE)
├─ document_number (VARCHAR 50, UNIQUE, NOT NULL)
├─ email (VARCHAR 255, UNIQUE)
├─ phone (VARCHAR 50)
├─ address (VARCHAR 500)
├─ status (VARCHAR 20, ENUM: ACTIVO, INACTIVO)
├─ created_at (TIMESTAMP, NOT NULL)
└─ updated_at (TIMESTAMP, NOT NULL)

SaleEntity → Table: sales
├─ id (UUID, PK, AUTO)
├─ customer_id (UUID, NOT NULL) - ⚠️ Sin FK
├─ sale_number (VARCHAR 50, UNIQUE, NOT NULL)
├─ status (VARCHAR 20, ENUM)
├─ subtotal (NUMERIC 12,2, NOT NULL)
├─ discount (NUMERIC 12,2, NOT NULL)
├─ tax (NUMERIC 12,2, NOT NULL)
├─ total (NUMERIC 12,2, NOT NULL)
├─ notes (VARCHAR 1000)
├─ created_by (UUID)
├─ updated_by (UUID)
├─ version (BIGINT) - @Version para optimistic locking
├─ created_at (TIMESTAMP, NOT NULL)
└─ updated_at (TIMESTAMP, NOT NULL)

SaleItemEntity → Table: sale_items
├─ id (UUID, PK, AUTO)
├─ sale_id (UUID, NOT NULL, FK →sales) ✓
├─ product_id (UUID, NOT NULL) - ⚠️ Sin FK
├─ product_name (VARCHAR 255, NOT NULL)
├─ quantity (INT, NOT NULL)
├─ unit_price (NUMERIC 10,2, NOT NULL)
├─ discount (NUMERIC 10,2, NOT NULL)
└─ subtotal (NUMERIC 10,2, NOT NULL)
```

#### inventory-service
```
InventoryEntity → Table: inventory
├─ id (UUID, PK) ❌ Sin @GeneratedValue
├─ product_id (UUID, NOT NULL, UNIQUE) - ⚠️ Sin FK
├─ current_stock (INT, NOT NULL)
├─ min_stock (INT, NOT NULL)
├─ max_stock (INT)
├─ reserved_stock (INT, NOT NULL)
├─ version (BIGINT) - @Version
├─ last_movement_at (TIMESTAMP)
├─ created_by (UUID)
├─ updated_by (UUID)
├─ created_at (TIMESTAMP, NOT NULL)
└─ updated_at (TIMESTAMP, NOT NULL)

InventoryMovementEntity → Table: inventory_movements
├─ id (UUID, PK, AUTO)
├─ inventory_id (UUID)
├─ product_id (UUID, NOT NULL) - ⚠️ Sin FK
├─ movement_type (VARCHAR 30, ENUM, NOT NULL)
├─ quantity (INT, NOT NULL)
├─ previous_stock (INT, NOT NULL)
├─ new_stock (INT, NOT NULL)
├─ reference_type (VARCHAR 50)
├─ reference_id (UUID)
├─ justification (VARCHAR 500)
├─ notes (VARCHAR 500)
├─ user_id (UUID, NOT NULL)
└─ created_at (TIMESTAMP, NOT NULL)
```

#### purchase-service
```
SupplierEntity → Table: suppliers (⚠️ No revisada)

PurchaseEntity → Table: purchases
├─ id (UUID, PK) ❌ Sin @GeneratedValue
├─ supplier_id (UUID, NOT NULL) - ⚠️ Sin FK
├─ purchase_number (VARCHAR, UNIQUE, NOT NULL)
├─ status (VARCHAR, ENUM)
├─ subtotal (NUMERIC 19,4, NOT NULL)
├─ tax (NUMERIC 19,4, NOT NULL)
├─ total (NUMERIC 19,4, NOT NULL)
├─ notes (VARCHAR)
├─ created_by (UUID)
├─ updated_by (UUID)
├─ created_at (TIMESTAMP)
└─ updated_at (TIMESTAMP)

PurchaseItemEntity → Table: purchase_items
├─ id (UUID, PK) ❌ Sin @GeneratedValue
├─ purchase_id (UUID) - ⚠️ Sin FK
├─ product_id (UUID, NOT NULL) - ⚠️ Sin FK
├─ product_name (VARCHAR, NOT NULL)
├─ quantity (INT, NOT NULL)
├─ received_quantity (INT, NOT NULL)
├─ unit_price (NUMERIC 19,4, NOT NULL)
└─ subtotal (NUMERIC 19,4, NOT NULL)
```

---

## 8. PERFORMANCE - ÍNDICES EN COLUMNAS DE CONSULTA FRECUENTE

### Patrón de Consultas Típicas

#### 1. **Búsquedas por Usuario**
```java
// Usado en: Auditoría, historial de cambios
SELECT * FROM audit_log WHERE user_id = ? ORDER BY timestamp DESC;
SELECT * FROM sales WHERE created_by = ? ORDER BY created_at DESC;
```
**Índices necesarios:**
```sql
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp DESC);
CREATE INDEX idx_sales_created_by ON sales(created_by);
CREATE INDEX idx_sales_created_at ON sales(created_at DESC);
```

#### 2. **Búsquedas por Estado (Status)**
```java
// Usado en: Filtrando pendientes, confirmadas, etc.
SELECT * FROM sales WHERE status = ? ORDER BY created_at DESC;
SELECT * FROM purchases WHERE status = ? ORDER BY created_at DESC;
```
**Índices necesarios:**
```sql
CREATE INDEX idx_sales_status ON sales(status);
CREATE INDEX idx_purchases_status ON purchases(status);
CREATE INDEX idx_products_status ON products(status);
```

#### 3. **Búsquedas por Rango de Fechas**
```java
// Usado en: Reportes, auditoría temporal
SELECT * FROM sales WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC;
SELECT * FROM inventory_movements WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC;
```
**Índices necesarios:**
```sql
CREATE INDEX idx_sales_created_at ON sales(created_at DESC);
CREATE INDEX idx_inventory_movements_created_at ON inventory_movements(created_at DESC);
```

#### 4. **Búsquedas por Código/Número**
```java
// Usado en: Búsqueda rápida de producto, cliente, etc.
SELECT * FROM products WHERE codigo = ?;
SELECT * FROM customers WHERE codigo = ?;
SELECT * FROM sales WHERE sale_number = ?;
```
**Índices:** Ya existen (UNIQUE)

#### 5. **Búsquedas por Entidad Relacionada**
```java
// Usado en: Listar items de una venta
SELECT * FROM sale_items WHERE sale_id = ?;
SELECT * FROM inventory_movements WHERE product_id = ?;
```
**Índices necesarios:**
```sql
CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items(product_id);
CREATE INDEX idx_inventory_movements_product_id ON inventory_movements(product_id);
```

---

## 9. NORMALIZACIÓN - ANÁLISIS 3NF

### ✓ TERCERA FORMA NORMAL (3NF)

Todas las tablas principales cumplen 3NF:

1. **Primera Forma Normal (1NF):** ✓
   - Todos los atributos contienen valores atómicos
   - No hay grupos repetitivos

2. **Segunda Forma Normal (2NF):** ✓
   - Todas las entidades tienen PK definida (id UUID)
   - Todos los atributos no-PK dependen completamente de la PK

3. **Tercera Forma Normal (3NF):** ✓
   - No hay dependencias transitivas
   - Ejemplo: `SaleEntity.customerId` es una referencia, no datos desnormalizados

### ⚠️ EXCEPCIONES PERMITIDAS (DESNORMALIZACIÓN CONTROLADA)

**SaleItemEntity.productName (desnormalizado)**
```java
@Column(name = "product_name", nullable = false, length = 255)
private String productName;
```
**Justificación:** Mantiene información histórica del nombre del producto en el momento de la venta.  
**Beneficio:** Evita JOINs y preserva datos históricos.

**PurchaseItemEntity.productName (desnormalizado)**
```java
@Column(name = "product_name", nullable = false)
private String productName;
```
**Justificación:** Idem SaleItemEntity.

### RECOMENDACIÓN
La desnormalización es aceptable en tablas transaccionales (ventas, compras) para:
- Mantener integridad histórica
- Mejorar performance (evitar JOINs)
- Simplificar auditoría

---

## 10. AUDITORÍA - CAMPOS TEMPORAL Y DE USUARIO

### ✓ IMPLEMENTACIÓN CORRECTA

**Todas las entidades cumplen con el patrón de auditoría:**

```java
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@Column(name = "updated_at", nullable = false)
private LocalDateTime updatedAt;

@Column(name = "created_by")
private UUID createdBy;

@Column(name = "updated_by")
private UUID updatedBy;

@PrePersist
protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
}
```

### Validación por Servicio

| Entidad | created_at | updated_at | created_by | updated_by | deleted_at | Status |
|---------|-----------|-----------|-----------|-----------|-----------|--------|
| UserEntity | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| RoleEntity | ✓ | ✓ | ✓ | ✗ | ✓ | ⚠️ |
| CategoryEntity | ✓ | ✓ | ✓ | ✓ | ✗ | ⚠️ |
| ProductEntity | ✓ | ✓ | ✓ | ✗ | ✗ | ⚠️ |
| CustomerEntity | ✓ | ✓ | ✗ | ✗ | ✗ | ⚠️ |
| SaleEntity | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| SaleItemEntity | ✗ | ✗ | ✗ | ✗ | ✗ | ❌ |
| InventoryEntity | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| InventoryMovementEntity | ✓ | ✗ | ✗ | ✗ | ✗ | ⚠️ |
| PurchaseEntity | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ |
| PurchaseItemEntity | ✗ | ✗ | ✗ | ✗ | ✗ | ❌ |

**Leyenda:**
- ✓ = Implementado
- ✗ = Falta
- ⚠️ = Incompleto
- ❌ = Crítico

### PROBLEMAS IDENTIFICADOS

1. **SaleItemEntity sin auditoría**
   - No tiene created_at, updated_at
   - No es rastreable cuándo se agregó el item

2. **PurchaseItemEntity sin auditoría**
   - Idem SaleItemEntity

3. **ProductEntity sin updated_by**
   - Quién actualiza el producto no se registra

4. **CustomerEntity sin auditoría de usuario**
   - created_by, updated_by no implementados

5. **InventoryMovementEntity**
   - Tiene user_id ✓
   - Pero falta updated_at (aunque los movimientos son inmutables)

---

## TABLAS Y COLUMNAS NO UTILIZADAS

### Potencialmente huérfanas

Basado en el código revisado, **NO se detectaron tablas o columnas no utilizadas**. Sin embargo, sin revisar todas las entidades, no se puede afirmar con certeza.

**Entidades no revisadas completamente:**
- PasswordResetTokenEntity
- RefreshTokenEntity
- TokenBlacklistEntity
- PasswordHistoryEntity
- ProductBarcodeEntity
- CatalogAuditLogEntity
- AdjustmentRequestEntity
- SupplierEntity
- InvoiceEntity
- ReportTemplateEntity, ReportCacheEntity, ExportLogEntity

---

## RESUMEN DE PROBLEMAS CRÍTICOS

### SEVERIDAD: CRÍTICA (Impacto inmediato)

1. **5 entidades sin @GeneratedValue(strategy = GenerationType.UUID)**
   - UserEntity
   - ProductEntity
   - PurchaseEntity
   - PurchaseItemEntity
   - InventoryEntity
   - **Impacto:** RuntimeException al intentar insertar sin UUID pre-generado

2. **Sin Foreign Keys en BD**
   - 15+ referencias sin constraint en la BD
   - **Impacto:** Posibles referencias huérfanas, integridad referencial débil

3. **Sin sistema de migraciones (Flyway/Liquibase)**
   - No hay versionamiento de cambios de esquema
   - **Impacto:** Difícil mantener consistencia entre ambientes

4. **SaleItemEntity y PurchaseItemEntity sin auditoría**
   - No se rastrea cuándo ni quién agregó los items
   - **Impacto:** Imposible auditar cambios en detalles de transacciones

5. **Tabla audit_log duplicada en múltiples servicios**
   - auth_service, sales_service, inventory_service, purchase_service
   - **Impacto:** Inconsistencia en auditoría, fragmentación de logs

### SEVERIDAD: ALTA (Requiere resolución)

6. **Índices faltantes en FK columns**
   - Sin índices en customer_id, product_id, category_id, etc.
   - **Impacto:** Queries lentas en JOINs y filtros

7. **ProductEntity sin updated_by**
   - Otros servicios referenciados no saben quién cambió el producto
   - **Impacto:** Auditoría incompleta

8. **CustomerEntity sin created_by/updated_by**
   - No se rastrea quién creó o actualizó clientes
   - **Impacto:** Auditoría incompleta en sales-service

---

## RECOMENDACIONES DE REMEDIACIÓN

### FASE 1: CORRECCIONES CRÍTICAS (1-2 días)

1. **Añadir @GeneratedValue a 5 entidades**
   ```java
   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   private UUID id;
   ```

2. **Crear Foreign Keys en BD**
   - 15+ ALTER TABLE statements

3. **Implementar Flyway migrations**
   - V1: Crear todas las tablas
   - V2: Añadir FKs
   - V3: Añadir índices

### FASE 2: AUDITORÍA COMPLETA (2-3 días)

4. **Añadir auditoría a SaleItemEntity y PurchaseItemEntity**
5. **Consolidar audit_log en única tabla o centralizar en auth-service**
6. **Añadir created_by/updated_by a CustomerEntity y ProductEntity**

### FASE 3: OPTIMIZACIÓN (1-2 días)

7. **Crear índices en FK columns**
8. **Crear índices en status, created_at, created_by columns**
9. **Validar EXPLAIN ANALYZE en queries comunes**

### FASE 4: VALIDACIÓN (1 día)

10. **Ejecutar spring.jpa.hibernate.ddl-auto=validate sin errores**
11. **Verificar integridad referencial con FKs**
12. **Ejecutar suite de tests

---

## CONCLUSIÓN

**Estado General: ⚠️ REQUIERE INTERVENCIÓN INMEDIATA**

El proyecto tiene una arquitectura de base de datos sólida en cuanto a normalización y estructura de datos, PERO presenta riesgos críticos en:

1. ❌ Inconsistencias entre anotaciones Hibernate y esquema de BD
2. ❌ Falta de integridad referencial explícita en BD
3. ❌ Ausencia de sistema de migraciones
4. ❌ Auditoría incompleta en entidades transaccionales

**Tiempo estimado de remediación:** 5-7 días con plan estructurado.

