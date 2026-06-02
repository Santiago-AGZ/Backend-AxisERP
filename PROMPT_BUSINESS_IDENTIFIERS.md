# PROMPT: IMPLEMENTAR IDENTIFICADORES DE NEGOCIO EN AXISERP

## 🎯 OBJETIVO

Implementar sistema dual de identificadores en AxisERP:
- **UUID**: Identificador interno único (relaciones internas)
- **Código Visible**: Identificador de negocio legible (búsquedas y reportes)

---

## 📋 INSTRUCCIONES PARA CLAUDE

Cuando el usuario pida implementar identificadores de negocio, debes:

### PASO 1: ANÁLISIS INICIAL
```
1. Identificar qué entidades necesitan código visible
2. Definir formato del código (PROD-000001, CLI-000001, etc.)
3. Determinar si es secuencial o generado
4. Verificar uniqueness constraints
5. Revisar impacto en APIs existentes
```

### PASO 2: MODIFICAR BD
```sql
-- Para cada entidad que necesita código visible:
ALTER TABLE {tabla} ADD COLUMN codigo VARCHAR(50) UNIQUE NOT NULL;
CREATE INDEX idx_{tabla}_codigo ON {tabla}(codigo);

-- Para códigos auto-generados (ventas, compras, facturas):
CREATE SEQUENCE {tabla}_seq START 1;
```

### PASO 3: ACTUALIZAR ENTIDADES JAVA
```java
// Agregar a Entity:
@Column(unique = true, nullable = false, length = 50)
private String codigo;

// Si es auto-generado:
@PrePersist
protected void onCreate() {
    if (codigo == null) {
        codigo = generarCodigo();
    }
}

private String generarCodigo() {
    // Implementar lógica de generación
}
```

### PASO 4: CREAR DTOs
```java
// ResponseDTO - NO incluye UUID
public class {Entidad}ResponseDTO {
    private String codigo;          // ✅ MOSTRAR
    private String nombre;
    // NO incluir UUID
}

// SearchDTO - Incluye UUID para búsqueda
public class {Entidad}SearchDTO {
    private UUID id;                // Para búsqueda interna
    private String codigo;          // ✅ MOSTRAR al usuario
    private String nombre;
}
```

### PASO 5: ACTUALIZAR CONTROLLERS
```java
// Buscar por código visible
@GetMapping("/{codigo}")
public ResponseEntity<{Entidad}ResponseDTO> getByCodigoVisible(
        @PathVariable String codigo) {
    {Entidad} entity = service.findByCodigoVisible(codigo);
    return ResponseEntity.ok(new {Entidad}ResponseDTO(entity));
}

// NO permitir búsqueda por UUID
@GetMapping("/{id}")
// ❌ Cambiar para buscar por codigo, NO por id
```

### PASO 6: ACTUALIZAR SERVICIOS
```java
@Service
public class {Entidad}Service {
    
    // Buscar por código visible
    public {Entidad} findByCodigoVisible(String codigo) {
        return repository.findByCodigoIgnoreCase(codigo)
            .orElseThrow(() -> new EntityNotFoundException(codigo));
    }
    
    // Crear con validación de código
    public {Entidad} crear({Entidad}Request request) {
        if (repository.existsByCodigoIgnoreCase(request.getCodigo())) {
            throw new CodigoYaExisteException(request.getCodigo());
        }
        return repository.save({Entidad}.from(request));
    }
}
```

### PASO 7: ACTUALIZAR REPOSITORIES
```java
public interface {Entidad}Repository extends JpaRepository<{Entidad}Entity, UUID> {
    
    Optional<{Entidad}Entity> findByCodigoIgnoreCase(String codigo);
    
    boolean existsByCodigoIgnoreCase(String codigo);
    
    // Query customizado para búsquedas
    @Query("SELECT e FROM {Entidad}Entity e WHERE " +
           "LOWER(e.codigo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<{Entidad}Entity> search(@Param("searchTerm") String searchTerm);
}
```

### PASO 8: TESTS
```java
@Test
public void testGenerarCodigoUnico() {
    {Entidad} entity1 = service.crear(request);
    {Entidad} entity2 = service.crear(request);
    
    assertNotEquals(entity1.getCodigo(), entity2.getCodigo());
}

@Test
public void testBuscarPorCodigoVisible() {
    {Entidad} entity = service.crear(request);
    {Entidad} encontrado = service.findByCodigoVisible(entity.getCodigo());
    
    assertEquals(entity.getId(), encontrado.getId());
}

@Test
public void testCodigoDuplicadoThrowsException() {
    service.crear(request);
    
    assertThrows(CodigoYaExisteException.class, () -> {
        service.crear(request);  // Mismo código
    });
}
```

---

## 🔧 ENTIDADES A MODIFICAR (Orden de prioridad)

### 1️⃣ PRODUCTOS (catalog-service)
```
Tabla: products
Código: PROD-{secuencial}
Ejemplo: PROD-000001, PROD-000002
Generación: Manual + validación
Unique: SÍ
```

**Cambios:**
- [ ] Agregar columna `codigo` a products
- [ ] Crear index idx_products_codigo
- [ ] Actualizar ProductEntity
- [ ] Crear ProductResponseDTO
- [ ] Actualizar ProductController
- [ ] Agregar validación de código duplicado
- [ ] Crear tests

### 2️⃣ CLIENTES (sales-service)
```
Tabla: customers
Código: CLI-{secuencial}
Ejemplo: CLI-000001, CLI-000002
Generación: Manual + validación
Unique: SÍ
```

**Cambios:**
- [ ] Agregar columna `codigo` a customers
- [ ] Crear index
- [ ] Actualizar CustomerEntity
- [ ] Actualizar controllers
- [ ] Tests

### 3️⃣ PROVEEDORES (purchase-service)
```
Tabla: suppliers
Código: PROV-{secuencial}
Ejemplo: PROV-000001, PROV-000002
Generación: Manual + validación
Unique: SÍ
```

**Cambios:**
- [ ] Agregar columna `codigo` a suppliers
- [ ] Crear index
- [ ] Actualizar SupplierEntity
- [ ] Actualizar controllers
- [ ] Tests

### 4️⃣ VENTAS (sales-service)
```
Tabla: sales
Código: VTA-{AÑO}-{secuencial}
Ejemplo: VTA-2026-000001, VTA-2026-000002
Generación: Automática (año + secuencia)
Unique: SÍ
Sequence: venta_seq
```

**Cambios:**
- [ ] Agregar columna `sale_number` a sales
- [ ] Crear sequence venta_seq
- [ ] Actualizar SaleEntity con @PrePersist
- [ ] Actualizar SaleRepository con método de generación
- [ ] Tests de generación automática

### 5️⃣ COMPRAS (purchase-service)
```
Tabla: purchases
Código: COM-{AÑO}-{secuencial}
Ejemplo: COM-2026-000001
Generación: Automática (año + secuencia)
Unique: SÍ
Sequence: compra_seq
```

**Cambios:**
- [ ] Agregar columna `purchase_number` a purchases
- [ ] Crear sequence compra_seq
- [ ] Actualizar PurchaseEntity
- [ ] Tests

### 6️⃣ FACTURAS (sales-service)
```
Tabla: invoices
Código: FAC-{AÑO}-{secuencial}
Ejemplo: FAC-2026-000001
Generación: Automática (año + secuencia)
Unique: SÍ
Sequence: factura_seq
```

**Cambios:**
- [ ] Agregar columna `factura_number` a invoices
- [ ] Crear sequence factura_seq
- [ ] Actualizar InvoiceEntity
- [ ] Tests

---

## ✅ VALIDACIONES OBLIGATORIAS

```java
// 1. Código no puede estar vacío
@NotBlank(message = "Código requerido")
private String codigo;

// 2. Código no puede tener caracteres especiales
@Pattern(regexp = "^[A-Z0-9-]+$", message = "Código inválido")
private String codigo;

// 3. Código debe ser único
@Column(unique = true)
private String codigo;

// 4. Código no puede cambiar después de creación
// Implementar en setter o usar @Immutable
private String codigo;  // No permitir cambios
```

---

## 📊 EJEMPLO COMPLETO: PRODUCTOS

### BD Script
```sql
-- Agregar columna
ALTER TABLE products ADD COLUMN codigo VARCHAR(50) UNIQUE NOT NULL DEFAULT 'PROD-000000';

-- Crear índice
CREATE INDEX idx_products_codigo ON products(codigo);

-- Generar códigos iniciales para registros existentes
UPDATE products SET codigo = 'PROD-' || LPAD(id::text, 6, '0') 
WHERE codigo = 'PROD-000000';
```

### Entity
```java
@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank
    private String codigo;  // PROD-000001
    
    @Column(nullable = false)
    private String nombre;
    
    // ... otros campos
}
```

### DTO Response
```java
public class ProductoResponseDTO {
    private String codigo;      // ✅ MOSTRAR
    private String nombre;
    private BigDecimal precio;
    private String descripcion;
    
    // NO incluir UUID
}
```

### Service
```java
@Service
public class ProductoService {
    
    @Autowired
    private ProductoRepository repository;
    
    public ProductoResponseDTO getByCodigoVisible(String codigo) {
        ProductEntity entity = repository.findByCodigoIgnoreCase(codigo)
            .orElseThrow(() -> new NotFoundException("Producto " + codigo + " no encontrado"));
        return new ProductoResponseDTO(entity);
    }
    
    public ProductoResponseDTO crear(CreateProductoRequest request) {
        if (repository.existsByCodigoIgnoreCase(request.getCodigo())) {
            throw new ConflictException("Código " + request.getCodigo() + " ya existe");
        }
        ProductEntity entity = ProductEntity.builder()
            .codigo(request.getCodigo())
            .nombre(request.getNombre())
            .precio(request.getPrecio())
            .build();
        return new ProductoResponseDTO(repository.save(entity));
    }
}
```

### Controller
```java
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
    
    @GetMapping("/{codigo}")
    public ResponseEntity<ProductoResponseDTO> getByCodigo(
            @PathVariable String codigo) {
        return ResponseEntity.ok(service.getByCodigoVisible(codigo));
    }
    
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(
            @RequestBody @Valid CreateProductoRequest request) {
        return ResponseEntity.status(201).body(service.crear(request));
    }
    
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> search(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(service.search(q));
    }
}
```

### Test
```java
@SpringBootTest
public class ProductoServiceTest {
    
    @Autowired
    private ProductoService service;
    
    @Test
    public void testCrearProductoConCodigoUnico() {
        CreateProductoRequest request = new CreateProductoRequest(
            "PROD-000001", "Laptop", new BigDecimal("1500.00"));
        
        ProductoResponseDTO response = service.crear(request);
        
        assertEquals("PROD-000001", response.getCodigo());
        assertNull(response.getId());  // UUID no incluido en DTO
    }
    
    @Test
    public void testCodigoDuplicadoThrowsException() {
        CreateProductoRequest request = new CreateProductoRequest(
            "PROD-000001", "Laptop", new BigDecimal("1500.00"));
        
        service.crear(request);
        
        assertThrows(ConflictException.class, () -> service.crear(request));
    }
    
    @Test
    public void testBuscarPorCodigoVisible() {
        CreateProductoRequest request = new CreateProductoRequest(
            "PROD-000001", "Laptop", new BigDecimal("1500.00"));
        
        ProductoResponseDTO creado = service.crear(request);
        ProductoResponseDTO encontrado = service.getByCodigoVisible("PROD-000001");
        
        assertEquals(creado.getCodigo(), encontrado.getCodigo());
    }
}
```

---

## 🚀 CÓMO USAR ESTE PROMPT

1. **Usuario pide:** "Implementa identificadores de negocio en AxisERP"
2. **Tú respondes:** "Voy a implementar sistema dual UUID + códigos visibles según las reglas"
3. **Pasos:**
   - Preguntar qué entidades priorizar
   - Analizar entidades actuales
   - Crear plan de modificación
   - Implementar en orden
   - Crear tests exhaustivos
   - Verificar que todo compila
   - Hacer commit

4. **Validar:**
   - Todos los códigos son únicos
   - Búsquedas funcionan por código
   - APIs no exponen UUIDs a usuarios
   - Tests pasen 100%
   - Documentación actualizada

---

## 📝 CHECKLIST FINAL

- [ ] Todas las entidades necesarias tienen código visible
- [ ] Códigos son únicos en BD
- [ ] DTOs no exponen UUIDs
- [ ] Búsqueda/filtros funcionan por código
- [ ] Códigos auto-generados para VTA, COM, FAC
- [ ] Validaciones en place (no vacío, formato correcto)
- [ ] Índices creados para performance
- [ ] Tests cubren casos happy path + errors
- [ ] Controllers aceptan búsqueda por código
- [ ] Documentación actualizada
- [ ] Commit creado y pusheado
