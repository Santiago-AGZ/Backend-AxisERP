# AxisERP - Pipeline DevOps

> Evidencia R.A.2 | Desarrollo de Software III | Practicas DevOps

---

## Datos del Proyecto

| Item | Descripcion |
|---|---|
| **Nombre del proyecto** | AxisERP |
| **Alcance** | Plataforma ERP empresarial con 7 microservicios |
| **Objetivo** | Implementar CI/CD automatizado con pipelines independientes por microservicio |
| **Arquitectura** | Microservicios + DDD + Hexagonal + Clean Architecture |
| **Integrantes** | (completar) |

---

## 1. Que es un Pipeline?

Un Pipeline en programacion es una secuencia de pasos o procesos conectados entre si, donde la salida de un paso se convierte en la entrada del siguiente. Su objetivo es automatizar tareas, organizar flujos de trabajo y reducir errores manuales.

**Ejemplo:** Cuando un programador sube cambios al repositorio:
1. Se descarga el codigo
2. Se compila el proyecto
3. Se ejecutan pruebas automaticas
4. Si las pruebas son exitosas, se genera una version lista para produccion
5. La aplicacion se despliega automaticamente en el servidor

---

## 2. Para que sirve un Pipeline?

- Automatizar procesos repetitivos
- Ejecutar pruebas automaticas
- Integrar y desplegar aplicaciones (CI/CD)
- Mejorar la eficiencia y la calidad del software
- Reducir errores humanos

---

## 3. Pasos para Crear el Pipeline de AxisERP

### 3.1 Definir el objetivo

Automatizar la construccion, prueba, empaquetado en Docker y despliegue a Azure de los 7 microservicios de AxisERP, asegurando que solo el servicio modificado se reconstruya y despliegue.

### 3.2 Identificar las Etapas

Cada pipeline de AxisERP tiene 4 etapas:

| Etapa | Descripcion | Herramienta |
|---|---|---|
| **Descargar codigo** | Clonar el repositorio desde Azure Repos | `checkout` |
| **Compilar + Probar** | Maven compile + JUnit + JaCoCo | `Maven@4` |
| **Empaquetar Docker** | Construir imagen multi-stage | `Docker@2` |
| **Publicar en ACR** | Push a Azure Container Registry | `Docker@2` |
| **Desplegar** | Actualizar Azure Container App | `AzureCLI@2` |
| **Monitorear** | Health check + smoke tests | `curl` |

### 3.3 Establecer el flujo de ejecucion

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  CODIGO      │───►│  COMPILAR    │───►│  EMPAQUETAR  │───►│  PUBLICAR    │
│  FUENTE      │    │  + PROBAR    │    │  (DOCKER)    │    │  (ACR)       │
└──────────────┘    └──────────────┘    └──────────────┘    └──────┬───────┘
                                                                    │
                                                                    ▼
┌──────────────┐    ┌──────────────┐                         ┌──────────────┐
│  MONITOREAR  │◄───│  DESPLEGAR   │◄────────────────────────│  IMAGEN EN   │
│  (HEALTH)    │    │  (AZURE)     │                         │  REGISTRY    │
└──────────────┘    └──────────────┘                         └──────────────┘
```

### 3.4 Automatizar cada Etapa

#### CI Pipeline (construye y publica)

Cada microservicio tiene su propio pipeline que solo se dispara cuando cambian sus archivos:

```yaml
# Ejemplo: ci-catalog.yml
trigger:
  branches: [master]
  paths:
    include: [catalog-service/**]    # Solo si cambia catalog

jobs:
  - template: template.yml            # Template reutilizable
    parameters:
      service: catalog-service
```

El **template.yml** contiene los pasos automatizados:

| Paso | Que hace | Script/Herramienta |
|---|---|---|
| **1. Descargar codigo** | `git checkout` del repo | Azure DevOps `checkout` |
| **2. Compilar** | `mvn clean compile` | `Maven@4` |
| **3. Ejecutar pruebas** | JUnit + JaCoCo (cobertura) | `Maven@4` |
| **4. Construir Docker** | `docker build` multi-stage | `Docker@2` |
| **5. Publicar en ACR** | `docker push` a `axiserp.azurecr.io` | `Docker@2` |
| **6. Escanear seguridad** | Trivy CRITICAL + HIGH | `aquasec/trivy` |

#### CD Pipeline (despliega)

Despliega a 3 ambientes con aprobaciones:

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│   DEV    │────►│ STAGING  │────►│   PROD   │
│(automatico)   │(1 aprobar)│    │(2 aprobar)│
└──────────┘     └──────────┘     └──────────┘
```

### 3.5 Monitorear Resultados

Al final de cada despliegue se ejecutan pruebas automaticas:

- `GET /actuator/health` → verifica que el servicio este UP
- `POST /api/v1/auth/login` → verifica que Supabase funcione
- `GET /api/v1/productos` → verifica que catalog responda
- `GET /api/v1/inventory/products` → verifica que inventory responda

Si alguna prueba falla, el pipeline se detiene y notifica el error.

---

## 4. Herramientas Utilizadas

| Categoria | Herramienta |
|---|---|
| **Lenguaje** | Java 21 |
| **Framework** | Spring Boot 3.5.x |
| **Base de datos** | PostgreSQL (Neon Serverless, 7 DBs) |
| **Autenticacion** | Supabase Auth (JWT ES256) |
| **Contenedores** | Docker (multi-stage builds) |
| **Registry** | Azure Container Registry (`axiserp.azurecr.io`) |
| **Orquestacion** | Azure Container Apps (7 servicios) |
| **CI/CD** | Azure DevOps Pipelines |
| **Calidad** | SonarCloud, Trivy, JaCoCo |
| **API Gateway** | Spring Cloud Gateway |
| **Control de versiones** | Git (Azure Repos) |

---

## 5. Pipelines del Proyecto

### Pipelines CI (7 pipelines - 1 por microservicio)

| Pipeline | Archivo | Trigger | Construye |
|---|---|---|---|
| CI-Auth | `ci-auth.yml` | `auth-service/**` | auth-service |
| CI-Catalog | `ci-catalog.yml` | `catalog-service/**` | catalog-service |
| CI-Inventory | `ci-inventory.yml` | `inventory-service/**` | inventory-service |
| CI-Sales | `ci-sales.yml` | `sales-service/**` | sales-service |
| CI-Purchase | `ci-purchase.yml` | `purchase-service/**` | purchase-service |
| CI-Report | `ci-report.yml` | `report-service/**` | report-service |
| CI-Gateway | `ci-gateway.yml` | `api-gateway/**` | api-gateway |

### Pipeline CD (1 pipeline con environments)

| Pipeline | Archivo | Ambientes |
|---|---|---|
| CD | `cd.yml` | Dev (auto) → Staging (1 approver) → Prod (2 approvers) |

### Templates (2 reutilizables)

| Archivo | Funcion |
|---|---|
| `template.yml` | Pasos CI comunes (compile + docker + push + scan) |
| `cd-deploy.yml` | Pasos CD comunes (deploy a Azure + health check) |

---

## 6. Ventajas de este Diseno

- **Automatizacion**: cada push a master dispara CI y CD automaticamente
- **Menos errores**: las pruebas se ejecutan antes de desplegar
- **Despliegue selectivo**: solo se reconstruye el servicio que cambio
- **Rollback instantaneo**: `az containerapp update --image :version-anterior`
- **Seguridad**: Trivy escanea vulnerabilidades CRITICAL y HIGH
- **Trazabilidad**: cada build tiene un tag unico en ACR
- **Aprobaciones**: staging y produccion requieren aprobacion humana

---

## 7. Variables de Entorno Estandar

Todas normalizadas en **lower-kebab-case**:

```
auth-db-url, auth-db-username, auth-db-password
supabase-url, supabase-jwt-issuer, supabase-anon-key
jwt-secret, jwt-access-expiration, jwt-refresh-expiration
internal-api-key, server-port, jpa-ddl-auto
catalog-service-url, inventory-service-url, sales-service-url
purchase-service-url, report-service-url, auth-service-url
```

---

## 8. Arquitectura de Despliegue

```
┌─────────────────────────────────────────────────────────┐
│                 AZURE CONTAINER APPS                     │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │ GATEWAY  │  │   AUTH   │  │ CATALOG  │  │INVENTORY│ │
│  │  :8080   │  │  :8081   │  │  :8082   │  │  :8083  │ │
│  │ EXTERNAL │  │ INTERNAL │  │ INTERNAL │  │INTERNAL │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │  SALES   │  │ PURCHASE │  │  REPORT  │              │
│  │  :8084   │  │  :8086   │  │  :8085   │              │
│  │ INTERNAL │  │ INTERNAL │  │ INTERNAL │              │
│  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│              ACR: axiserp.azurecr.io                      │
│  7 repositorios de imagenes Docker                       │
└─────────────────────────────────────────────────────────┘
```
