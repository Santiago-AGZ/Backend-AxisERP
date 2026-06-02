# AxisERP Platform

Java 21 · Spring Boot 3.5.x · Maven 3.9.15 wrapper · PostgreSQL 16 · Docker Compose

## Build & Run

Each service is standalone (no aggregate POM). Build and run from the service directory:

```bash
cd <service>/ && ./mvnw clean package -DskipTests    # build
cd <service>/ && ./mvnw test                         # run tests
cd <service>/ && ./mvnw spring-boot:run              # run single service
docker compose up --build                            # run all (from root)
```

## Services & Ports

| Dir | Port | Package | SB |
|-----|------|---------|----|
| `api-gateway` | 8080 | `com.axiserp.gateway` | 3.5.0 |
| `auth-service` | 8081 | `com.axiserp.auth` | 3.5.14 |
| `catalog-service` | 8082 | `com.axiserp.catalog` | 3.5.14 |
| `inventory-service` | 8083 | `com.axiserp.inventory` | 3.5.14 |
| `report-service` | 8085 | `com.axiserp.report` | 3.5.0 |

## Critical Gotchas

### Spring Boot version mismatch
`api-gateway` and `report-service` use **3.5.0**; all others use **3.5.14**. Bumping the two laggards to 3.5.14 is recommended before sharing dependencies.

### Gateway has no routes
`api-gateway` pulls in `spring-cloud-starter-gateway` (Cloud 2025.0.0) but has zero route configuration — no `RouteLocator` bean, no YAML routes. It starts but proxies nothing.

### Master is scaffolding-only
All services on `master` contain only `Application.java`, `pom.xml`, `Dockerfile`, placeholder `application.properties`, and a trivial `contextLoads()` test. Full hexagonal architecture implementations with business logic, controllers, security, and tests live on feature branches (`feat/auth-service`, `feat/catalog-service`, etc.).

### Clean branches for merge
Each service has two branch variants:
- `feat/X-service` — diverged from master with cleanup commits
- `feat/X-service-clean` — single implementation commit rebased on initial commit; these are local-only (not pushed) and are the intended merge candidates

### No shared module
Every service has its own copy of `ApiResponse`, `JwtAuthenticationFilter`, `GlobalExceptionHandler` — significant duplication. No common library exists.

## Architecture (feature branches)

Hexagonal (Ports & Adapters) in every service:
```
com.axiserp.<service>/
  domain/model/       — entities, value objects, domain exceptions
  domain/service/     — domain services, factories
  application/dto/    — request/response DTOs
  application/service/— application services
  application/usecase/— use case implementations
  ports/input/        — use case contracts
  ports/output/       — repository/outbound contracts
  adapters/in/web/    — REST controllers, ApiResponse, GlobalExceptionHandler
  adapters/out/       — JPA entities, repos, external service adapters
  infrastructure/     — security filters, config (CORS, rate limiting)
```

## Auth & Inter-Service Communication (feature branches)

- **JWT**: ES256 (ECDSA P-256) via Supabase JWKS (`https://hbtcusxbkkefphunarwn.supabase.co/auth/v1/.well-known/jwks.json`)
- **auth-service**: Uses `spring-security-oauth2-resource-server` + `NimbusJwtDecoder`
- **Other services**: Manual JJWT parsing with JWKS public key caching (no OAuth2 Resource Server dependency)
- **Inter-service auth**: `X-Internal-Api-Key` header, checked by `InternalApiKeyFilter`
- **InternalApiKeyFilter used in**: all 6 services for cross-service calls

## API Response Format (feature branches)

All endpoints return `ApiResponse<T>`:
```json
{"success":bool, "code":"OK"|"...", "message":"...", "data":<T>, "errors":[...], "meta":{...}, "pagination":{...}}
```

## Database

- **Local dev**: Single shared `axiserp` database on Postgres 16 container (compose.yml)
- **Production**: Per-service Neon (Supabase) databases with HikariCP tuning for serverless pooling
- **JPA**: `ddl-auto=update` (scaffold); feature branches switched auth-service to `validate`
- All services reference `SPRING_DATASOURCE_URL` env var (set in compose.yml or `.env`)

## Testing

- **Framework**: JUnit 5 + Mockito + `@SpringBootTest` + `MockMvc`
- **Scaffold**: Only `contextLoads()`; feature branches have 70+ tests per service (unit + integration)
- **Run**: `./mvnw test` from service dir

## Dependencies of Note

- **Spring Boot Admin** (3.3.3) on auth-service only
- **Spring Cloud Gateway** on api-gateway only
- **Apache POI** 5.2.5 + **OpenPDF** 2.0.3 on report-service only
- **Lombok** 1.18.38 annotation processor on all service pom.xmls
- **No Springdoc/OpenAPI** in any service

## Git

- **Remote**: `https://github.com/Santiago-AGZ/Backend-AxisERP.git`
- **No CI, no hooks, no branch protection**
- All secrets in `.env` (gitignored); no credentials in source or compose.yml
- `.gitignore` excludes `.claude/`, `.agents/`, `.idea/`, `.vscode/`, `docs/`, `postman/`, `commerce-service/`, `*.log`, scaffold scripts, and reference docs
