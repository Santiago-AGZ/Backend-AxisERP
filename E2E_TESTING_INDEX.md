# ÍNDICE DE DOCUMENTOS E2E TESTING - AXISERP

**Fecha:** 2026-06-07  
**Versión:** 1.0  
**Estado:** COMPLETO

---

## DOCUMENTACIÓN ENTREGADA

Se han creado los siguientes documentos y scripts para E2E testing:

### 1. DOCUMENTOS DE PLANIFICACIÓN

#### `E2E_TESTING_PLAN.md` (Principal)
- **Tamaño:** ~8,000 líneas
- **Contenido:**
  - Descripción general de 8 flujos E2E críticos
  - Ambiente de testing (stack tecnológico, configuración)
  - 8 flujos detallados con:
    - Pre-condiciones
    - Pasos específicos
    - Expected results
    - Post-condiciones
    - Validaciones de base de datos
    - Comandos cURL completos para cada test
  - Scripts de validación
  - Template para reporte de resultados
  - Anexos con variables, roles, estados

#### `E2E_TESTING_QUICK_START.md` (Guía Rápida)
- **Tamaño:** ~600 líneas
- **Contenido:**
  - Inicio rápido en 5 minutos
  - 3 opciones de ejecución:
    - Testing manual con cURL (paso a paso)
    - Testing con Postman
    - Testing automatizado (CI/CD)
  - Consultas comunes con ejemplos
  - Resolución de problemas
  - Checklist pre-testing
  - Métricas de éxito

### 2. SCRIPTS EJECUTABLES

#### `scripts/check-services.sh`
- **Función:** Verifica que todos los 7 servicios estén corriendo
- **Puertos validados:** 8080 (Gateway), 8081-8086 (servicios)
- **Output:** Lista de servicios UP/DOWN
- **Uso:**
  ```bash
  bash scripts/check-services.sh
  ```

#### `scripts/run-e2e-complete.sh`
- **Función:** Ejecuta flujo E2E completo automatizado
- **Incluye:**
  - Login y obtención de token
  - Obtención de usuario autenticado
  - Obtención/creación de categoría
  - Creación de producto
  - Inicialización de inventario
  - Obtención de cliente
  - Creación de venta
  - Confirmación de venta
  - Obtención de reporte
  - Logout
  - Verificación de token invalidado
- **Uso:**
  ```bash
  ADMIN_EMAIL="admin@example.com" \
  ADMIN_PASSWORD="AdminPass123!" \
  bash scripts/run-e2e-complete.sh
  ```
- **Output:** Reporte con pass/fail de cada paso

#### `scripts/validate-database.sh`
- **Función:** Valida integridad de datos en todas las bases de datos
- **Validaciones:**
  - Conteos de usuarios, categorías, productos
  - Estados de inventario
  - Tipos de movimientos de stock
  - Estados de ventas y compras
  - Registros de auditoría
  - Últimas operaciones registradas
- **Uso:**
  ```bash
  bash scripts/validate-database.sh
  ```

### 3. COLECCIONES PARA HERRAMIENTAS

#### `postman/AxisERP-E2E-Tests.postman_collection.json`
- **Formato:** JSON (Postman Collection v2.1)
- **Requests incluidos:** 20+ endpoints
- **Estructura:**
  - Carpeta 1: Authentication (Login, Get User Info)
  - Carpeta 2: Catalog Management (Categories, Products)
  - Carpeta 3: Inventory Management (Initialize, Entry, Info)
  - Carpeta 4: Sales Management (Customers, Create, Confirm, Pay)
  - Carpeta 5: Reports (Sales, Inventory)
  - Carpeta 6: Logout
- **Variables:**
  - BASE_URL (default: http://localhost:8080)
  - ADMIN_TOKEN (auto-populated)
  - PRODUCT_ID, SALE_ID, CUSTOMER_ID, etc. (auto-populated)
- **Tests incluidos:** Pre-request scripts y test assertions en cada request
- **Uso:**
  1. Importar en Postman
  2. Configurar environment
  3. Ejecutar requests en orden o usar "Run Collection"

---

## FLUJOS E2E DEFINIDOS

### Matriz de Flujos

| # | Flujo | Servicio | Rol | Tests | Estado |
|---|-------|----------|-----|-------|--------|
| 1 | Registro de Usuario | Auth | - | 4 | ✓ Documentado |
| 2 | Autenticación y Login | Auth | - | 5 | ✓ Documentado |
| 3 | Crear Producto | Catalog | ADMIN/INV | 5 | ✓ Documentado |
| 4 | Registrar Inventario | Inventory | ADMIN/INV | 7 | ✓ Documentado |
| 5 | Crear Pedido de Compra | Purchase | ADMIN/INV | 8 | ✓ Documentado |
| 6 | Crear Venta | Sales | ADMIN/VEND | 10 | ✓ Documentado |
| 7 | Consultar Reporte | Report | ADMIN/VEND | 9 | ✓ Documentado |
| 8 | Logout y Cierre | Auth | - | 6 | ✓ Documentado |

**Total:** 54 test cases documentados

### Cobertura Esperada

- **Servicios:** 7/7 (100%)
- **Endpoints:** 30+ (principal coverage)
- **Métodos HTTP:** GET, POST, PATCH (DELETE no en E2E)
- **Roles:** 3 (ADMIN, VENDEDOR, INVENTARIO)
- **Validaciones:** 
  - Response structure
  - HTTP status codes
  - Business rules
  - Database integrity
  - Security (authentication, authorization)

---

## CÓMO USAR ESTOS DOCUMENTOS

### Para QA Engineers

1. **Lectura inicial:**
   - Leer `E2E_TESTING_QUICK_START.md` (5 min)
   - Revisar `E2E_TESTING_PLAN.md` sección de Flujos (20 min)

2. **Preparación:**
   - Verificar checklist en Quick Start
   - Ejecutar `scripts/check-services.sh`

3. **Ejecución:**
   - Opción A: Seguir comandos cURL en Quick Start
   - Opción B: Usar colección Postman
   - Opción C: Ejecutar script automatizado

4. **Reporte:**
   - Usar template en `E2E_TESTING_PLAN.md` (Reporte de Resultados)
   - Completar con resultados obtenidos
   - Documentar issues encontrados

### Para Developers

1. **Entender flujos:**
   - Leer `E2E_TESTING_PLAN.md` (completo)
   - Entender validaciones de base de datos

2. **Ejecutar tests localmente:**
   - `bash scripts/run-e2e-complete.sh`
   - `bash scripts/validate-database.sh`

3. **Debugging:**
   - Ver logs: `docker-compose logs -f <service>`
   - Ejecutar queries SQL directamente en PostgreSQL
   - Usar comandos cURL del Quick Start para test aislado

### Para DevOps / CI-CD

1. **Integración en pipeline:**
   - Agregar `scripts/run-e2e-complete.sh` como step
   - Ejecutar después de `docker-compose up -d`
   - Agregar validación de database
   - Usar salida para reportes de build

2. **Configuración:**
   - Variables de ambiente (ver E2E_TESTING_PLAN.md Anexo A)
   - Timeout: 5+ minutos
   - Retry logic si aplica

### Para Product Managers

1. **Entender alcance:**
   - Leer sección "Flujos E2E Definidos" en este documento
   - Revisar tabla de flujos

2. **Validación de requisitos:**
   - Cada flujo cubre un caso de uso crítico
   - Pre/post condiciones aseguran realismo
   - Tests validan reglas de negocio (BUSINESS_RULES.md)

---

## CÓMO EJECUTAR (RESUMEN)

### Opción 1: Script Automatizado (Recomendado)

```bash
# 1. Verificar servicios
bash scripts/check-services.sh

# 2. Ejecutar tests
ADMIN_EMAIL="admin@example.com" \
ADMIN_PASSWORD="AdminPass123!" \
bash scripts/run-e2e-complete.sh

# 3. Validar datos
bash scripts/validate-database.sh
```

### Opción 2: Manual con cURL

```bash
# Ver E2E_TESTING_QUICK_START.md para:
# - Obtener token
# - Crear producto
# - Inicializar inventario
# - Crear venta
# - Confirmar y pagar
# - Obtener reportes
# - Logout
```

### Opción 3: Postman

```bash
# 1. Importar: postman/AxisERP-E2E-Tests.postman_collection.json
# 2. Configurar variables en environment
# 3. Run Collection o ejecutar requests manualmente
```

---

## MÉTRICAS Y CRITERIOS DE ÉXITO

### Éxito de Tests

✓ Todos los flujos ejecutan sin errores HTTP 5xx  
✓ Todas las validaciones de base de datos pasan  
✓ Tokens JWT se generan y revocan correctamente  
✓ Estados de entidades avanzan como se espera  
✓ Datos se replican entre servicios correctamente  

### Cobertura

- **Flujos E2E:** 8/8 (100%)
- **Test Cases:** 54+ (100%)
- **Servicios:** 7/7 (100%)
- **Endpoints principales:** 30+ (primary operations)

### Performance

- Script automatizado completo: ~5-10 minutos
- Validación de database: ~1 minuto
- Total: ~15 minutos para ciclo completo

---

## MANTENIMIENTO Y ACTUALIZACIONES

### Cuándo actualizar documentos

1. **Cambios en endpoints:**
   - Actualizar comandos cURL
   - Actualizar requests en Postman
   - Actualizar expected results

2. **Cambios en reglas de negocio:**
   - Actualizar validaciones de BD
   - Actualizar expected states

3. **Nuevos flujos:**
   - Agregar flujo a E2E_TESTING_PLAN.md
   - Agregar tests a scripts/run-e2e-complete.sh
   - Agregar requests a Postman collection

### Versioning

- **v1.0 (2026-06-07):** Initial release
  - 8 flujos E2E
  - 54 test cases
  - 3 scripts ejecutables
  - Colección Postman completa

---

## PREGUNTAS FRECUENTES

### ¿Puedo ejecutar un solo flujo?

**Sí.** Ver `E2E_TESTING_QUICK_START.md` para comandos individuales de cada flujo.

### ¿Qué pasa si un test falla?

Ver sección "Resolución de Problemas" en `E2E_TESTING_QUICK_START.md`.

### ¿Se pueden ejecutar tests en paralelo?

**No recomendado** para tests que modifican datos (crear producto, venta, etc.). Usar diferentes instancias de API Gateway si necesario.

### ¿Cómo agrego más tests?

1. Documentar en `E2E_TESTING_PLAN.md`
2. Agregar comando cURL en Quick Start
3. Agregar request en Postman collection
4. Agregar función en `scripts/run-e2e-complete.sh`

### ¿Cómo ejecuto tests en CI/CD?

Ver `E2E_TESTING_QUICK_START.md` sección "Testing Automatizado (CI/CD)".

---

## ARCHIVOS CREADOS - LISTA COMPLETA

```
axisERP-platform/
├── E2E_TESTING_PLAN.md (8,000+ líneas)
├── E2E_TESTING_QUICK_START.md (600+ líneas)
├── E2E_TESTING_INDEX.md (este archivo)
├── scripts/
│   ├── check-services.sh (ejecutable)
│   ├── run-e2e-complete.sh (ejecutable)
│   └── validate-database.sh (ejecutable)
└── postman/
    └── AxisERP-E2E-Tests.postman_collection.json
```

---

## INTEGRACIÓN CON DESARROLLO

### Git Workflow

```bash
# Antes de hacer commit
bash scripts/check-services.sh  # ✓ Servicios corriendo
bash scripts/run-e2e-complete.sh  # ✓ E2E tests pasan
bash scripts/validate-database.sh  # ✓ Datos consistentes

# Luego commit/push
git add .
git commit -m "feature: xyz - E2E tests passing"
```

### Pull Request Checklist

- [ ] E2E tests pasan localmente
- [ ] Database validations pasan
- [ ] No hay regresiones en flujos existentes
- [ ] Nuevos flujos documentados (si aplica)

---

## PRÓXIMOS PASOS RECOMENDADOS

1. **Ejecución inicial:** Ejecutar `scripts/run-e2e-complete.sh` para validar setup
2. **Integración CI/CD:** Agregar scripts a GitHub Actions / pipeline
3. **Monitoreo:** Ejecutar tests diarios/semanales
4. **Expansión:** Agregar más test cases según necesidad
5. **Performance:** Agregar tests de carga/stress (futuro)

---

## SOPORTE Y CONTACTO

Para preguntas o issues:

1. Revisar secciones relevantes en documentación
2. Consultar resolución de problemas en Quick Start
3. Ejecutar scripts de validación para diagnosticar
4. Crear issue con:
   - Descripción del problema
   - Output de test que falló
   - Logs de servicios
   - Estado de base de datos (validate-database.sh output)

---

**Documento de Referencia:** E2E Testing Index v1.0  
**Última Actualización:** 2026-06-07  
**Mantener actualizado:** Sí  
**Responsable:** QA & Development Team
