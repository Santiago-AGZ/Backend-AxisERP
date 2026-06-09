# AUDITORÍA ACADÉMICA FINAL — PIPELINE AXISERP

**Asignatura:** Desarrollo de Software III  
**Tutor:** José Javier Vaquiro Ladino  
**Autores:** Axel David Rubianes Valencia, Juan David Charry Medina, Miguel Angel Echeverry Solarte, Santiago Alvarez Gutiérrez  
**Universidad del Valle — Yumbo, 2026**

---

## A. VERIFICACIÓN DE LA CONSIGNA

| Requisito de la actividad | Estado | Evidencia |
|---------------------------|--------|-----------|
| Nombre del proyecto | ✅ | AxisERP |
| Alcance | ✅ | 7 microservicios, MVP definido |
| Objetivo | ✅ | General + 6 específicos |
| Arquitectura | ✅ | Microservicios + Hexagonal + Clean Architecture |
| Herramientas utilizadas | ✅ | Tabla completa (40+ tecnologías) |
| Integrantes | ✅ | 4 integrantes nombrados |
| Pipeline por proyecto | ✅ | `ci-template.yml` + `ci-full.yml` en `.github/workflows/` |
| Explicación detallada de cada etapa | ⚠️ Parcial | Pipeline implementado pero no documentado en el README del proyecto |
| Prácticas DevOps | ✅ | CI, automatización, calidad, seguridad |
| Automatización del flujo | ✅ | Build, tests, Docker, Trivy, Hadolint |
| Preparación para sustentación | ✅ | 2 documentos de 796 + 1109 líneas |

---

## B. ANÁLISIS DEL PIPELINE (ci-full.yml + ci-template.yml)

### Etapas implementadas

| Etapa | Implementado | Detalle |
|-------|-------------|---------|
| 1. Checkout | ✅ `actions/checkout@v4.1.7` | Clona el repositorio |
| 2. Validación estructura | ✅ Script bash | Verifica pom.xml y Dockerfile |
| 3. Setup JDK 21 | ✅ `actions/setup-java@v4.1.0` | Temurin, cache Maven |
| 4. Build + Tests | ✅ `mvn clean verify` | Compila + unit tests + integration tests |
| 5. Artefactos | ✅ `upload-artifact@v4.3.1` | JAR + test reports + SBOM |
| 6. Docker Buildx | ✅ `docker/setup-buildx-action@v3.1.0` | Build multi-arch |
| 7. Linting Docker | ✅ `hadolint/hadolint-action@v3.1.0` | Verifica Dockerfile |
| 8. Build Docker | ✅ `docker/build-push-action@v6.2.0` | 7 imágenes, cache GHA |
| 9. Verificación imagen | ✅ Script bash | Tamaño < 500MB |
| 10. Trivy scan | ✅ `aquasec/trivy:0.71.0` | CRITICAL + HIGH |

### Etapas faltantes

| Etapa | Impacto | Prioridad |
|-------|---------|-----------|
| **SonarQube / análisis estático** | ALTO | ❌ AUSENTE |
| **Deploy automático a Render** | ALTO | ❌ AUSENTE (Render auto-deploy desde GitHub, no desde Actions) |
| **Dependabot / Renovate** | MEDIO | ❌ AUSENTE |
| **Versionado semántico** | MEDIO | ❌ AUSENTE |
| **Pruebas de integración con BD real** | MEDIO | ❌ AUSENTE (Testcontainers declarado pero no usado) |
| **Pruebas de seguridad SAST** | MEDIO | ❌ AUSENTE |

---

## C. EVALUACIÓN DEVOPS

### C.1 Integración Continua (CI) — 70%

| Práctica | Estado |
|----------|--------|
| Build automático en cada push | ✅ |
| Tests automáticos | ✅ (313 tests, 0 failures) |
| Análisis estático de código | ❌ |
| Seguridad en dependencias | ⚠️ Parcial (solo Trivy en imágenes) |
| Linting de Docker | ✅ |
| Cache de dependencias | ✅ Maven cache |
| Reportes de tests | ✅ Upload de artifacts |

### C.2 Entrega Continua (CD) — 50%

| Práctica | Estado | Detalle |
|----------|--------|---------|
| Deploy automático backend | ⚠️ **Semi-automático** | Render detecta cambios en GitHub y auto-despliega. No es Actions quien deploya. |
| Deploy automático frontend | ⚠️ **Semi-automático** | Vercel detecta cambios y auto-despliega. |
| Rollback | ❌ | No documentado en el pipeline |
| Aprobaciones (gates) | ❌ | No hay revisión manual antes de deploy |
| El pipeline SÍ genera los artefactos (JAR) que Render necesita | ✅ |

**Importante:** El flujo de despliegue está documentado en la Fase de Arquitectura (punto 10). Render y Vercel hacen deploy automático desde GitHub, pero **el pipeline de Actions no ejecuta el deploy directamente** — es el servicio PaaS quien lo hace.

---

## D. HERRAMIENTAS

| Herramienta | Estado | Adecuación |
|-------------|--------|------------|
| **GitHub Actions** | ✅ Implementado | Adecuado para el proyecto |
| **Maven** | ✅ Implementado | Estándar en proyectos Java |
| **JUnit 5 + Mockito** | ✅ Implementado | 313 tests |
| **Hadolint** | ✅ Implementado | Linting Docker |
| **Trivy** | ✅ Implementado | Seguridad en imágenes |
| **Docker** | ✅ Implementado | 7 imágenes multi-stage |
| **SonarQube** | ❌ **AUSENTE** | Crítico para análisis estático |
| **Dependabot** | ❌ **AUSENTE** | Actualización automática de dependencias |
| **Flyway** | ⚠️ Declarado pero no implementado | Migraciones BD |
| **Testcontainers** | ⚠️ Declarado pero no usado | Tests de integración |

---

## E. ARQUITECTURA VS PIPELINE

| Aspecto arquitectónico | Coherencia |
|------------------------|------------|
| 7 microservicios en paralelo | ✅ Coherente |
| Java 21 + Spring Boot | ✅ Coherente |
| Docker + multi-stage | ✅ Coherente |
| API Gateway | ✅ Incluido en pipeline |
| Despliegue en Render | ✅ Documentado en arquitectura |
| Despliegue en Vercel (frontend) | ✅ Documentado |
| Base de datos Neon/Supabase | ⚠️ No hay migraciones automáticas en el pipeline |

---

## F. PREPARACIÓN PARA SUSTENTACIÓN

### Preguntas probables del profesor y respuestas esperadas

| Pregunta | ¿Puede responder? | Evaluación |
|----------|------------------|------------|
| ¿Cuál es la arquitectura del sistema? | ✅ Sí — Microservicios + Hexagonal | Bien documentado |
| ¿Qué tecnologías usan y por qué? | ✅ Sí — Tabla con 40+ justificaciones | Excelente |
| ¿Cuántos microservicios tienen y qué hace cada uno? | ✅ Sí — 7 servicios definidos | Bien documentado |
| ¿Cómo funciona el pipeline? | ✅ Sí — Build, test, Docker, Trivy | Bien implementado |
| ¿Dónde está SonarQube? | ❌ **DEBIL** — No implementado | **Riesgo alto** |
| ¿Cómo hacen despliegue? | ✅ **Pueden explicar** — Render auto-deploy desde GitHub | Está documentado |
| ¿Cómo aseguran la calidad del código? | ⚠️ Parcial — Tests + Trivy, pero sin análisis estático | **Riesgo medio** |
| ¿Qué es CI/CD y cómo lo aplicaron? | ⚠️ Parcial — CI completo, CD semi-automático (Render/Vercel) | Aceptable |
| ¿Quién hizo qué? | ✅ 4 integrantes con roles definidos | Bien documentado |
| ¿Cómo manejan la seguridad? | ✅ JWT, BCrypt, roles, Trivy | Bien implementado |
| ¿Cómo versionan los artefactos? | ❌ No hay versionado semántico | **Riesgo medio** |

---

## G. RÚBRICA DE CALIFICACIÓN ACTUALIZADA

| Criterio | Peso | Nota | Justificación |
|----------|------|------|--------------|
| 1. Información del proyecto | 10% | **5.0** | Nombre, objetivo, alcance, arquitectura, tecnologías, integrantes — TODO definido |
| 2. Etapas del pipeline | 25% | **3.5** | Build, test, Docker, Trivy OK. Faltan: SonarQube, deploy, monitoreo |
| 3. DevOps (CI/CD) | 25% | **3.5** | CI completa (sin Sonar). CD semi-automática vía Render/Vercel. |
| 4. Herramientas | 15% | **4.0** | Bien seleccionadas. Falta SonarQube y Dependabot. |
| 5. Arquitectura | 10% | **5.0** | Arquitectura coherente con el pipeline. Excelente documentación. |
| 6. Sustentación | 15% | **3.5** | Documentación sólida. Riesgo: SonarQube y versionado. |

### NOTA FINAL: 3.8 / 5.0 — ACEPTABLE

---

## H. VEREDICTO FINAL

### Fortalezas

1. **Documentación excepcional** — 2 documentos (796 + 1109 líneas) con análisis, arquitectura, requerimientos, casos de uso
2. **Pipeline funcional** — Build, tests, Docker, Trivy, Hadolint — todo ejecutándose correctamente
3. **313 tests unitarios** — Cobertura de pruebas sólida
4. **Arquitectura bien definida** — Microservicios + Hexagonal + DDD
5. **Despliegue documentado** — Render + Vercel con auto-deploy desde GitHub
6. **4 integrantes con roles** — Distribución de trabajo clara

### Debilidades

1. **Sin SonarQube** — Es la herramienta más esperada en un pipeline académico
2. **Sin Dependabot** — Actualización de dependencias no automatizada
3. **Sin versionado semántico** — No hay tags ni releases automáticos
4. **CD no está en Actions** — El deploy lo hacen Render/Vercel por su cuenta, no el pipeline
5. **Sin tests de integración con BD real** — Testcontainers declarado pero no implementado

### Elementos Faltantes

| Elemento | Prioridad | Impacto en nota |
|----------|-----------|----------------|
| SonarQube | **ALTA** | -0.5 puntos |
| Versionado semántico (tags/releases) | **MEDIA** | -0.3 puntos |
| Dependabot | **MEDIA** | -0.2 puntos |
| Tests de integración con Testcontainers | **MEDIA** | -0.2 puntos |

### Recomendaciones para subir la nota

| # | Acción | Dificultad | Impacto |
|---|--------|-----------|---------|
| 1 | Agregar SonarQube al pipeline (`sonarsource/sonarcloud-github-action`) | Media | +0.5 |
| 2 | Agregar GitHub Release + tag automático (`softprops/action-gh-release`) | Baja | +0.3 |
| 3 | Agregar Dependabot config (`.github/dependabot.yml`) | Baja | +0.2 |
| 4 | Agregar un test de integración con Testcontainers | Media | +0.2 |

### Riesgos para la Sustentación

| Riesgo | Probabilidad | Impacto |
|--------|-------------|---------|
| "¿Por qué no usaron SonarQube?" | **ALTA** | **ALTO** |
| "¿Quién se encargó del pipeline?" | **ALTA** | MEDIO |
| "¿Cómo harían deploy si Render no existiera?" | MEDIA | MEDIO |
| "¿Cómo miden la calidad del código?" | **ALTA** | **ALTO** |
| "¿El pipeline realmente entrega el software?" | MEDIA | MEDIO |

---

## VEREDICTO FINAL

# APROBADO — CON OBSERVACIONES

| Criterio | Valor |
|----------|-------|
| Nota actual | **3.8 / 5.0 — ACEPTABLE** |
| Con SonarQube + Dependabot | **4.3 / 5.0 — BUENO** |
| Con todas las mejoras | **4.6 / 5.0 — MUY BUENO** |

### ¿Qué salva el proyecto?
- Documentación completa y profesional (análisis + arquitectura)
- Pipeline real funcionando con 7 microservicios
- 313 tests, Docker, Trivy, Hadolint
- Despliegue documentado y operativo en Render

### ¿Qué lo baja?
- Ausencia de SonarQube (esperado en CI/CD académico)
- CD delegado a Render/Vercel (no orquestado por Actions)
- Sin versionado de artefactos

**Recomendación para el equipo:** Para la sustentación, preparen una respuesta sólida sobre por qué no incluyeron SonarQube y cómo planean integrarlo. Tambiénexpliquen que el CD se logra mediante la integración nativa de Render con GitHub, lo cual es una estrategia válida aunque diferente a un pipeline tradicional.
