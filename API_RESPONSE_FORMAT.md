# FORMATO ESTANDARIZADO DE RESPUESTAS DE API - AxisERP

## 🎯 PRINCIPIOS FUNDAMENTALES

1. **Consistencia**: Todas las APIs devuelven el mismo formato
2. **Claridad**: Estructura simple y predecible
3. **Información**: Incluye status, datos, errores y metadatos
4. **Seguridad**: No expone información sensible
5. **Paginación**: Soporta listas grandes de forma eficiente
6. **Timestamps**: Auditoria de cuándo se hizo la request

---

## 📦 ESTRUCTURA BASE

```json
{
  "success": boolean,           // ✅ true o ❌ false
  "code": "string",             // Código de error/success
  "message": "string",          // Mensaje legible
  "data": object|array|null,    // El payload real
  "errors": array,              // Errores adicionales (validación, etc.)
  "meta": {                      // Metadatos
    "timestamp": "ISO8601",
    "requestId": "UUID",
    "path": "string",
    "method": "HTTP_METHOD"
  },
  "pagination": {                // Si es lista
    "page": number,
    "pageSize": number,
    "totalPages": number,
    "totalRecords": number,
    "hasNext": boolean,
    "hasPrevious": boolean
  }
}
```

---

## ✅ RESPUESTAS DE ÉXITO

### 1. Crear Recurso (201 Created)

```json
{
  "success": true,
  "code": "CREATED",
  "message": "Producto creado exitosamente",
  "data": {
    "codigo": "PROD-000001",
    "nombre": "Laptop Dell",
    "precio": 1500.00,
    "descripcion": "Laptop de 15 pulgadas"
  },
  "meta": {
    "timestamp": "2026-05-29T14:30:00Z",
    "requestId": "abc123-def456-ghi789",
    "path": "/api/v1/productos",
    "method": "POST"
  }
}
```

### 2. Obtener Recurso Único (200 OK)

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Producto encontrado",
  "data": {
    "codigo": "PROD-000001",
    "nombre": "Laptop Dell",
    "precio": 1500.00
  },
  "meta": {
    "timestamp": "2026-05-29T14:30:00Z",
    "requestId": "abc123-def456-ghi789",
    "path": "/api/v1/productos/PROD-000001",
    "method": "GET"
  }
}
```

### 3. Listar Recursos (200 OK)

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Productos recuperados exitosamente",
  "data": [
    {
      "codigo": "PROD-000001",
      "nombre": "Laptop Dell",
      "precio": 1500.00
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 10,
    "totalPages": 5,
    "totalRecords": 42,
    "hasNext": true,
    "hasPrevious": false
  },
  "meta": {
    "timestamp": "2026-05-29T14:30:00Z",
    "requestId": "abc123-def456-ghi789",
    "path": "/api/v1/productos?page=1&pageSize=10",
    "method": "GET"
  }
}
```

---

## ❌ RESPUESTAS DE ERROR

### Error de Validación (400 Bad Request)

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Error de validación en el request",
  "errors": [
    {
      "field": "codigo",
      "message": "Código requerido",
      "value": null
    }
  ],
  "meta": {
    "timestamp": "2026-05-29T14:30:00Z",
    "requestId": "abc123-def456-ghi789",
    "path": "/api/v1/productos",
    "method": "POST"
  }
}
```

### Conflicto (409 Conflict)

```json
{
  "success": false,
  "code": "CONFLICT",
  "message": "El código del producto ya existe",
  "errors": [
    {
      "field": "codigo",
      "message": "Código PROD-000001 ya está registrado"
    }
  ],
  "meta": {
    "timestamp": "2026-05-29T14:30:00Z",
    "requestId": "abc123-def456-ghi789",
    "path": "/api/v1/productos",
    "method": "POST"
  }
}
```

---

## 🔧 IMPLEMENTACIÓN EN JAVA

### Entity de Respuesta

```java
@Getter @Setter @Builder
public class ApiResponse<T> {
    private Boolean success;
    private String code;
    private String message;
    private T data;
    private List<ApiError> errors;
    private ApiMeta meta;
    private PaginationMeta pagination;
}

@Getter @Setter @Builder
public class ApiError {
    private String field;
    private String message;
    private Object value;
}

@Getter @Setter @Builder
public class ApiMeta {
    private LocalDateTime timestamp;
    private String requestId;
    private String path;
    private String method;
}
```

### Utility Class

```java
@Component
public class ApiResponseUtils {
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .code("SUCCESS")
            .message(message)
            .data(data)
            .meta(buildMeta())
            .build();
    }
    
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .code("CREATED")
            .message(message)
            .data(data)
            .meta(buildMeta())
            .build();
    }
    
    public static ApiResponse<?> error(String code, String message) {
        return ApiResponse.builder()
            .success(false)
            .code(code)
            .message(message)
            .data(null)
            .meta(buildMeta())
            .build();
    }
    
    private static ApiMeta buildMeta() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        
        return ApiMeta.builder()
            .timestamp(LocalDateTime.now())
            .requestId(UUID.randomUUID().toString())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .build();
    }
}
```

### Controller

```java
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
    
    @PostMapping
    public ResponseEntity<ApiResponse<ProductoDTO>> crear(
            @Valid @RequestBody CreateProductoRequest request) {
        ProductoDTO producto = service.crear(request);
        return ResponseEntity.status(201)
            .body(ApiResponseUtils.created(producto, "Producto creado exitosamente"));
    }
    
    @GetMapping("/{codigo}")
    public ResponseEntity<ApiResponse<ProductoDTO>> getByCodigo(
            @PathVariable String codigo) {
        ProductoDTO producto = service.getByCodigoVisible(codigo);
        return ResponseEntity.ok(
            ApiResponseUtils.success(producto, "Producto encontrado"));
    }
}
```

---

## ✅ REGLAS CLAVE

- ✅ Nunca expongas UUIDs internos en respuestas
- ✅ Siempre incluye requestId para debugging
- ✅ Incluye timestamp en todas las respuestas
- ✅ Usa códigos estándar (CREATED, SUCCESS, CONFLICT, etc.)
- ✅ Valida y devuelve errores claros
- ✅ Incluye paginación en listas
- ✅ No expongas stack traces en producción

