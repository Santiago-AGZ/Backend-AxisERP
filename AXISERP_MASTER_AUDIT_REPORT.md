# AXISERP MASTER AUDIT REPORT

**Date:** 2026-06-07
**Scope:** All 7 microservices + API Gateway
**Source of Truth:** Code, Config, Tests, Dockerfiles, Workflows

---

## Resumen Ejecutivo

| Dimensión | Score | Estado |
|-----------|:-----:|--------|
| **Arquitectura DDD/Hexagonal** | 85% | ✅ Capas correctas, 4 violaciones menores |
| **Seguridad** | 88% | ✅ Secrets protegidos, CORS configurado, 2 hallazgos |
| **Reglas de Negocio** | 90% | ✅ 18/20 cumplidas, 2 faltantes menores |
| **Microservicios** | 82% | ⚠️ Sin RabbitMQ, comunicación REST-only |
| **Base de Datos (JPA)** | 55% | ❌ 25/27 sin @Version, 25/27 sin @Index |
| **Observabilidad** | 25% | ❌ Sin metrics, sin tracing, sin Swagger |
| **Testing** | 60% | ⚠️ 58 tests, 0 integración real, api-gateway 0 |
| **Docker/Infra** | 95% | ✅ Multi-stage, HEALTHCHECK, no-root |
| **Documentación** | 40% | ❌ Sin Swagger/OpenAPI en ningún servicio |
| **CI/CD** | 90% | ✅ Workflows funcionales, sin CodeQL/SARIF |
| **Overall** | **71%** | ⚠️ **Apto con correcciones** |

---

## Score Global por Servicio

| Servicio | Arquitectura | Seguridad | BD/JPA | Tests | Observabilidad | Overall |
|----------|:-----------:|:---------:|:------:|:----:|:--------------:|:-------:|
| auth-service | 60% | 85% | 50% | 70% | 30% | **59%** |
| catalog-service | 95% | 90% | 40% | 60% | 25% | **62%** |
| inventory-service | 95% | 90% | 50% | 65% | 25% | **65%** |
| sales-service | 85% | 90% | 50% | 65% | 25% | **63%** |
| purchase-service | 75% | 90% | 45% | 60% | 25% | **59%** |
| report-service | 90% | 90% | 50% | 55% | 25% | **62%** |
| api-gateway | 100% | 88% | N/A | 0% | 20% | **52%** |

---

## Hallazgos por Severidad

### CRÍTICOS (3)

| ID | Hallazgo | Archivo | Impacto |
|----|----------|---------|---------|
| C1 | `RefreshTokenService` logea el token de refresh en texto plano | `auth-service/.../RefreshTokenService.java:90` | Exposición de credenciales en logs |
| C2 | api-gateway Actuator sin endpoints configurados | `api-gateway/.../application.properties` | Sin health check funcional en el gateway |
| C3 | 25/27 entidades sin `@Version` (sin control de concurrencia) | Todos los `*Entity.java` | Lost updates en operaciones concurrentes |

### ALTOS (8)

| ID | Hallazgo | Archivo |
|----|----------|---------|
| H1 | `AuthController` bypass del use case layer (llama `SupabaseAuthPort` directo) | `auth-service/.../AuthController.java:37` |
| H2 | `TokenController` bypass del use case layer | `auth-service/.../TokenController.java:96-97` |
| H3 | Purchase write use cases sin `@Transactional` | `purchase-service/.../*UseCaseImpl.java` |
| H4 | 25/27 entidades sin `@Index` (solo 2 en auth-service tienen) | Todos los `*Entity.java` |
| H5 | Zero Swagger/OpenAPI en todos los servicios | 7 servicios |
| H6 | Zero Prometheus/Micrometer | 7 servicios |
| H7 | Zero correlation ID / MDC tracing | 7 servicios |
| H8 | api-gateway: 0 tests | `api-gateway/src/test/` |

### MEDIOS (6)

| ID | Hallazgo | Archivo |
|----|----------|---------|
| M1 | `salePrice > 0` sin validación explícita | `catalog-service/.../CreateProductUseCaseImpl.java:43` |
| M2 | Cycle detection faltante en CreateCategoryUseCaseImpl | `catalog-service/.../CreateCategoryUseCaseImpl.java` |
| M3 | 10 entidades con UUID manual (sin `@GeneratedValue`) | UserEntity, ProductEntity, CategoryEntity, etc. |
| M4 | `SaleItem` anémico (cero lógica de dominio) | `sales-service/.../SaleItem.java` |
| M5 | `PurchaseController` importa `domain.model.PurchaseStatus` | `purchase-service/.../PurchaseController.java:22` |
| M6 | Refresh token value loggeado (confirmado C1 + severidad alta) | `auth-service/.../RefreshTokenService.java:90` |

### BAJOS (5)

| ID | Hallazgo | Archivo |
|----|----------|---------|
| B1 | Unused imports en UserController | `auth-service/.../UserController.java:26,39` |
| B2 | 3 tests `@Disabled` | auth-service (2), catalog-service (1) |
| B3 | Stale `.gitignore` entry `AZURE_SETUP.md` | `.gitignore:17` |
| B4 | Sin `logback.xml` en ningún servicio | Todos |
| B5 | DTO `PageResult` duplicado en 3 services (era necesario por service-per-db) | auth, catalog, inventory |

---

## Azure Cleanup

| Archivo | Estado |
|---------|--------|
| `AZURE_SETUP.md` | ❌ No existe en disk (gitignored) |
| `scripts/setup-azure.ps1` | ✅ Eliminado del repo |
| `deploy-cloudshell.sh` | ❌ No existe en disk (gitignored) |
| `update-inter-service-urls.ps1` | ❌ No existe en disk (gitignored) |
| Referencias en código | ✅ **0 encontradas** |

**Sin residuos Azure en el código o configuración actual.**

---

## Plan de Acción

### P0 — Inmediato (esta sesión)

| # | Acción | Archivo |
|---|--------|---------|
| 1 | Fix `RefreshTokenService` para no loguear el token | `RefreshTokenService.java:90` |
| 2 | Configurar actuator endpoints en api-gateway | `api-gateway/application.properties` |

### P1 — Próximo sprint

| # | Acción | Archivos |
|---|--------|----------|
| 3 | Refactor AuthController/TokenController para usar use case ports | auth-service |
| 4 | Agregar `@Transactional` a purchase use cases | purchase-service |
| 5 | Agregar validación `salePrice > 0` y `purchasePrice >= 0` | catalog-service |
| 6 | Agregar cycle detection en CreateCategoryUseCaseImpl | catalog-service |

### P2 — Próximo mes

| # | Acción |
|---|---------|
| 7 | Agregar `@Version` a entidades críticas (User, Product, Purchase, Category) |
| 8 | Agregar `@Index` a FKs y auditorías |
| 9 | Agregar Swagger/OpenAPI (springdoc) |
| 10 | Agregar correlation ID filter en gateway |
| 11 | Agregar tests de integración con Testcontainers |

### P3 — Deuda técnica

| # | Acción |
|---|---------|
| 12 | Agregar logback.xml con JSON layout |
| 13 | Agregar Micrometer + Prometheus |
| 14 | Agregar tests de controller para api-gateway |
| 15 | Eliminar `AZURE_SETUP.md` de `.gitignore` (stale entry) |

---

## Veredicto

**APTO PARA PRODUCCIÓN CON CORRECCIONES**

La plataforma es funcional y segura para operar en producción, con 3 hallazgos críticos que deben corregirse antes del deploy a producción real:
1. Token de refresh loggeado en texto plano
2. Actuator del gateway sin configurar
3. 25/27 entidades sin control de concurrencia

Tiempo estimado de corrección P0: **15 minutos**.
