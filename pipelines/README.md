# AxisERP DevOps - Operational Documentation

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    AZURE CONTAINER APPS                       │
│                                                              │
│  ┌─────────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │ API GATEWAY │  │   AUTH   │  │ CATALOG  │  │INVENTORY │ │
│  │   :8080     │  │  :8081   │  │  :8082   │  │  :8083   │ │
│  │  EXTERNAL   │  │ EXTERNAL │  │ INTERNAL │  │ INTERNAL │ │
│  └──────┬──────┘  └──────────┘  └──────────┘  └──────────┘ │
│         │                                                    │
│         │  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│         ├─►│  SALES   │  │ PURCHASE │  │  REPORT  │        │
│         │  │  :8084   │  │  :8086   │  │  :8085   │        │
│         │  │ INTERNAL │  │ INTERNAL │  │ INTERNAL │        │
│         │  └──────────┘  └──────────┘  └──────────┘        │
│         │                                                    │
│  ┌──────┴──────────────────────────────────────────────┐    │
│  │              ACR: axiserp.azurecr.io                  │    │
│  │  7 repos: auth,catalog,inventory,sales,purchase,     │    │
│  │           report,api-gateway                          │    │
│  └──────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
```

## CI/CD Flow

```
┌──────────┐    ┌───────────┐    ┌──────────┐    ┌──────────┐
│  PUSH to │───►│ CI PIPELINE│───►│ ACR PUSH │───►│ CD (DEV) │
│   main   │    │Build+Test+ │    │  images  │    │  auto    │
│          │    │Docker+Scan │    │          │    │          │
└──────────┘    └───────────┘    └──────────┘    └────┬─────┘
                                                       │
                                          ┌────────────┘
                                          ▼
                                   ┌──────────────┐
                                   │ INTEGRATION  │
                                   │    TESTS     │
                                   └──────┬───────┘
                                          │
                              ┌───────────┴───────────┐
                              ▼                       ▼
                      ┌──────────────┐       ┌──────────────┐
                      │   STAGING    │       │  PRODUCTION  │
                      │ (approval)   │──────►│  (approval)  │
                      └──────────────┘       └──────────────┘
```

## Pipeline Files

| File | Purpose |
|---|---|
| `ci-prod.yml` | CI: Compile -> Test -> Docker Build -> Push ACR -> Trivy Scan -> SBOM |
| `cd-prod.yml` | CD: Dev (auto) -> Integration Tests -> Staging (approval) -> Prod (approval) |
| `cd-template-env.yml` | Reusable CD template for environment-aware deployments |
| `variables-dev.yml` | Development environment variables |
| `variables-staging.yml` | Staging environment variables |
| `variables-prod.yml` | Production environment variables |

## Azure DevOps Setup

### 1. Service Connections (Project Settings > Service Connections)

```bash
# Azure Resource Manager
Name: axiserp-azure
Type: Azure Resource Manager
Scope: Subscription (48482985-813d-4a7c-a283-350c1d90799c)

# Docker Registry
Name: axiserp-acr-service-connection
Type: Docker Registry
Registry: axiserp.azurecr.io
Docker ID: axiserp
Password: (ACR admin password)
```

### 2. Variable Groups (Library > Variable Groups)

Create 3 groups:

**axiserp-dev** (import from `variables-dev.yml`)
**axiserp-staging** (import from `variables-staging.yml`)
**axiserp-prod** (import from `variables-prod.yml`)

**axiserp-prod-secrets** (mark as secret):
| Variable | Value |
|---|---|
| SUPABASE_URL | https://hbtcusxbkkefphunarwn.supabase.co |
| SUPABASE_JWT_ISSUER | https://hbtcusxbkkefphunarwn.supabase.co/auth/v1 |
| AUTH_DB_URL | jdbc:postgresql://ep-wispy-voice... |
| CATALOG_DB_URL | jdbc:postgresql://ep-rapid-night... |
| INVENTORY_DB_URL | jdbc:postgresql://ep-still-resonance... |
| SALES_DB_URL | jdbc:postgresql://ep-misty-waterfall... |
| PURCHASE_DB_URL | jdbc:postgresql://ep-quiet-union... |
| REPORT_DB_URL | jdbc:postgresql://ep-flat-dream... |
| TEST_ADMIN_EMAIL | santiagoalvarez374@gmail.com |
| TEST_ADMIN_PASSWORD | Admin123! |

### 3. Environments (Environments)

```
axiserp-staging    -> Approvals: 1 required
axiserp-production -> Approvals: 2 required, Branch control: main only
```

### 4. Create Pipelines

1. Go to **Pipelines > New Pipeline**
2. Select **Azure Repos Git** (your repo)
3. Choose **Existing Azure Pipelines YAML file**
4. Path: `/pipelines/ci-prod.yml` -> Save
5. Repeat for `/pipelines/cd-prod.yml`

## Service Registry

| Service | Port | Ingress | DB | Auth |
|---|---|---|---|---|
| api-gateway | 8080 | External | None | None |
| auth-service | 8081 | External | auth_db (Neon) | Supabase JWT ES256 |
| catalog-service | 8082 | Internal | catalog_db (Neon) | JWT + internal-api-key |
| inventory-service | 8083 | Internal | inventory_db (Neon) | JWT + internal-api-key |
| sales-service | 8084 | Internal | sales_db (Neon) | JWT + internal-api-key |
| purchase-service | 8086 | Internal | purchase_db (Neon) | JWT + internal-api-key |
| report-service | 8085 | Internal | report_db (Neon) | JWT + internal-api-key |

## Environment Variables Standard

**ALL variables MUST be lower-kebab-case:**

```
auth-db-url          supabase-url
auth-db-username     supabase-jwt-issuer
auth-db-password     supabase-service-role-key
internal-api-key     supabase-anon-key
jwt-secret           server-port
jwt-access-expiration   jpa-ddl-auto
jwt-refresh-expiration
```

## Docker Standards

- Multi-stage builds (maven:3.9-eclipse-temurin-21 -> eclipse-temurin:21-jre-alpine)
- Non-root user (appuser, UID 1001)
- HEALTHCHECK on /actuator/health
- ENTRYPOINT: ["java", "-jar", "app.jar"] (no shell interpolation)
- EXPOSE must match targetPort in Azure Container App

## Common Operations

### Build specific service locally
```bash
docker build -t axiserp.azurecr.io/auth-service:latest -f auth-service/Dockerfile auth-service/
```

### Push to ACR
```bash
az acr login --name axiserp
docker push axiserp.azurecr.io/auth-service:latest
```

### Update env vars on a service
```bash
az containerapp update --name auth-service -g axiserp-prod \
  --set-env-vars server-port=8081 jpa-ddl-auto=validate
```

### Set secrets
```bash
az containerapp secret set --name auth-service -g axiserp-prod \
  --secrets internal-api-key="your-key-here"
```

### Check service logs
```bash
az containerapp logs show --name auth-service -g axiserp-prod --tail 50
```

### Restart a service
```bash
az containerapp revision restart --name auth-service -g axiserp-prod \
  --revision auth-service--0000004
```

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| "This Container App is stopped" (404) | Scaled to zero | Set `--min-replicas 1` |
| ImagePullBackOff | Stale ACR credentials | `az containerapp registry set` |
| Gateway 500/timeout | Wrong service URL | Use `http://service-name` without port |
| Health 503 | Mail health fails | Verify `management.health.mail.enabled=false` |
| DB connection fails | Missing env var/secret | Check all `${db-url}`, `${db-username}`, `${db-password}` |

## Current Production URLs

```
Gateway: https://api-gateway.bravefield-65bde8ce.brazilsouth.azurecontainerapps.io
Auth:    https://auth-service.bravefield-65bde8ce.brazilsouth.azurecontainerapps.io
```

## Postman Collection

Use `postman/AxisERP_Azure.postman_environment.json` for API testing.
