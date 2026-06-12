# AxisERP

> Evidencia R.A.2 | Desarrollo de Software III | Practicas DevOps

---

## Datos del Proyecto

| Item | Descripcion |
|---|---|
| **Nombre** | AxisERP |
| **Asignatura** | Desarrollo de Software III |
| **RA** | R.A.2 - Practicas DevOps |
| **Alcance** | Plataforma ERP empresarial con 7 microservicios Spring Boot |
| **Objetivo** | Implementar un sistema CI/CD automatizado con pipelines independientes por microservicio |
| **Arquitectura** | Microservicios + DDD + Hexagonal + Clean Architecture |
| **Integrantes** | (completar) |

---

## 1. Que es un Pipeline?

Un Pipeline en programacion es una secuencia de pasos o procesos conectados entre si, donde la salida de un paso se convierte en la entrada del siguiente. Su objetivo es automatizar tareas, organizar flujos de trabajo y reducir errores manuales.

**Ejemplo:** Cuando un programador sube cambios al repositorio:
1. Se descarga el codigo
2. Se compila el proyecto
3. Se ejecutan pruebas automaticas
4. Se construye una imagen Docker
5. Se publica en el registro de contenedores
6. Se despliega en la nube

---

## 2. Para que sirve un Pipeline?

- Automatizar procesos repetitivos
- Ejecutar pruebas automaticas
- Integrar y desplegar aplicaciones (CI/CD)
- Mejorar la eficiencia y calidad del software
- Reducir errores humanos
- Desplegar solo lo que cambio (no todo el sistema)

---

## 3. Pasos para Crear los Pipelines de AxisERP

### 3.1 Definir el objetivo

Automatizar la construccion, prueba, empaquetado en Docker y publicacion en Azure Container Registry de los 7 microservicios de AxisERP, asegurando que solo el servicio modificado se reconstruya.

### 3.2 Identificar las Etapas

Cada pipeline CI de AxisERP ejecuta 5 etapas:

| Etapa | Descripcion | Herramienta |
|---|---|---|
| **Descargar codigo** | Clonar repositorio | `actions/checkout@v4` |
| **Compilar** | Maven compile | JDK 21 + Maven |
| **Ejecutar pruebas** | JUnit test | Maven |
| **Empaquetar Docker** | Construir imagen multi-stage | Docker |
| **Publicar en ACR** | Push a Azure Container Registry | Docker + `az acr` |

### 3.3 Establecer el flujo de ejecucion

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  CODIGO      │───►│  COMPILAR    │───►│  EMPAQUETAR  │───►│  PUBLICAR    │
│  FUENTE      │    │  + PROBAR    │    │  (DOCKER)    │    │  (ACR)       │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
```

### 3.4 Automatizar cada Etapa

Cada microservicio tiene su propio pipeline CI que solo se dispara cuando cambian sus archivos:

```yaml
name: CI - Catalog Service

on:
  push:
    branches: [master]
    paths: [catalog-service/**]   # Solo si cambia catalog
  workflow_dispatch:               # Tambien manual

jobs:
  ci:
    uses: ./.github/workflows/template-ci.yml   # Template reutilizable
    with:
      service: catalog-service
      port: 8082
    secrets: inherit
```

El **template-ci.yml** contiene los pasos comunes compartidos por los 7 pipelines.

### 3.5 Monitorear Resultados

Cada ejecucion del pipeline muestra en GitHub Actions:
- Estado de cada etapa (pendiente, en progreso, exitoso, fallido)
- Logs detallados de compilacion y pruebas
- Tiempo total de ejecucion
- Historial de ejecuciones pasadas

---

## 4. Ejemplo Practico: Cambio en Catalog Service

```
1. Dev modifica catalog-service/ProductoService.java
2. git add, git commit, git push a master
3. GitHub Actions detecta cambio en catalog-service/**
4. CI - Catalog Service se dispara automaticamente
5. Etapa 1: Descarga el codigo (checkout)
6. Etapa 2: Compila con Maven (mvn compile)
7. Etapa 3: Ejecuta pruebas (mvn test)
8. Etapa 4: Construye imagen Docker (docker build)
9. Etapa 5: Publica en ACR (docker push)
10. Imagen disponible en: axiserp.azurecr.io/catalog-service:latest
```

---

## 5. Herramientas Utilizadas

| Categoria | Herramienta |
|---|---|
| **Lenguaje** | Java 21 |
| **Framework** | Spring Boot 3.5.x |
| **Base de datos** | PostgreSQL (Neon Serverless, 7 DBs) |
| **Autenticacion** | Supabase Auth (JWT ES256) |
| **Contenedores** | Docker (multi-stage builds) |
| **Registry** | Azure Container Registry (`axiserp.azurecr.io`) |
| **Orquestacion** | Azure Container Apps (7 servicios) |
| **CI/CD** | **GitHub Actions** (8 workflows) |
| **API Gateway** | Spring Cloud Gateway |
| **Control de versiones** | Git (GitHub) |

---

## 6. Pipelines del Proyecto

| Pipeline | Archivo | Se dispara con | Que hace |
|---|---|---|---|
| CI - Auth | `ci-auth.yml` | `auth-service/**` | Build + Test + Docker + Push ACR |
| CI - Catalog | `ci-catalog.yml` | `catalog-service/**` | Build + Test + Docker + Push ACR |
| CI - Inventory | `ci-inventory.yml` | `inventory-service/**` | Build + Test + Docker + Push ACR |
| CI - Sales | `ci-sales.yml` | `sales-service/**` | Build + Test + Docker + Push ACR |
| CI - Purchase | `ci-purchase.yml` | `purchase-service/**` | Build + Test + Docker + Push ACR |
| CI - Report | `ci-report.yml` | `report-service/**` | Build + Test + Docker + Push ACR |
| CI - Gateway | `ci-gateway.yml` | `api-gateway/**` | Build + Test + Docker + Push ACR |
| CD - Deploy | `cd-deploy.yml` | Push a master | Despliega los 7 servicios a Azure |

---

## 7. Arquitectura de Microservicios

```
┌──────────────────────────────────────────────────────────────┐
│                  AZURE CONTAINER APPS                         │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ GATEWAY  │  │   AUTH   │  │ CATALOG  │  │INVENTORY │    │
│  │  :8080   │  │  :8081   │  │  :8082   │  │  :8083   │    │
│  │ EXTERNAL │  │ INTERNAL │  │ INTERNAL │  │ INTERNAL │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │  SALES   │  │ PURCHASE │  │  REPORT  │                  │
│  │  :8084   │  │  :8086   │  │  :8085   │                  │
│  │ INTERNAL │  │ INTERNAL │  │ INTERNAL │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└──────────────────────────────────────────────────────────────┘
```

---

## 8. Ventajas de Utilizar Pipelines

- **Automatizacion**: cada push a master dispara CI automaticamente
- **Menos errores humanos**: las pruebas se ejecutan antes de publicar
- **Despliegue selectivo**: solo se reconstruye el servicio que cambio
- **Mayor velocidad**: pipelines en paralelo, sin dependencias innecesarias
- **Trazabilidad**: cada build tiene un tag unico en ACR
- **Escalabilidad**: cada servicio es independiente

---

## 9. Conclusion

AxisERP implementa un sistema de pipelines CI/CD basado en GitHub Actions donde cada microservicio tiene su propio pipeline independiente. Esto permite construir, probar y publicar solo los servicios que cambiaron, reduciendo tiempos de espera y riesgos. El sistema sigue las mejores practicas de DevOps: automatizacion, integracion continua, despliegue continuo y monitoreo de resultados.
