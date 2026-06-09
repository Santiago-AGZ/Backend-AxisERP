# AUDITORÍA FORENSE DEFINITIVA — PIPELINES DEVOPS AXISERP

**Fecha:** 2026-06-09  
**Método:** Verificación directa contra código real. Ninguna afirmación sin evidencia.

---

## FASE 1 — INVENTARIO COMPLETO (VERIFICADO)

| Archivo | Tipo | Trigger | Líneas | Estado real |
|---------|------|---------|--------|-------------|
| `ci-full.yml` | Orquestador | Push a main/master | 56 | **Activo** — 7 servicios en paralelo |
| `ci-template.yml` | Template reusable | `workflow_call` | 145 | **Activo** — 15 etapas |
| `build.yml` | SonarCloud | Push + PR | 44 | **Activo** — 7 servicios en matrix |
| `ci-release.yml` | Release | Push tag v* | 41 | **Activo** — Changelog + release |
| `ci-auth.yml` | Individual | Push a auth-service/** | 28 | **Redundante** — duplica ci-full.yml |
| `ci-catalog.yml` | Individual | Push a catalog-service/** | 28 | **Redundante** |
| `ci-inventory.yml` | Individual | Push a inventory-service/** | 28 | **Redundante** |
| `ci-purchase.yml` | Individual | Push a purchase-service/** | 28 | **Redundante** |
| `ci-sales.yml` | Individual | Push a sales-service/** | 28 | **Redundante** |
| `ci-report.yml` | Individual | Push a report-service/** | 28 | **Redundante** |
| `ci-gateway.yml` | Individual | Push a api-gateway/** | 28 | **Redundante** |

**Evidencia de redundancia:** Los 7 archivos tienen EXACTAMENTE 28 líneas. La ÚNICA diferencia es el nombre del servicio en la línea 26. Todos usan `build-docker: false`. Todos llaman al mismo `ci-template.yml`.

**Archivos de configuración DevOps:**
- `.github/dependabot.yml` — ✅ 8 entradas, semanal, labels
- `sonar-project.properties` — ✅ Existe
- 7 `pom.xml` con JaCoCo + sonar-maven-plugin — ✅ Modificados
- 7 `Dockerfile` con multi-stage + non-root + HEALTHCHECK — ✅

---

## FASE 2 — REDUNDANCIAS VERIFICADAS

### Hallazgo 1 — CRÍTICO: 7 workflows individuales (ci-*.yml por servicio)

**Evidencia:**
```
Archivo: .github/workflows/ci-auth.yml (28 lines) → service: auth-service, build-docker: false
Archivo: .github/workflows/ci-catalog.yml (28 lines) → service: catalog-service, build-docker: false
Archivo: .github/workflows/ci-inventory.yml (28 lines) → service: inventory-service, build-docker: false
Archivo: .github/workflows/ci-purchase.yml (28 lines) → service: purchase-service, build-docker: false
Archivo: .github/workflows/ci-sales.yml (28 lines) → service: sales-service, build-docker: false
Archivo: .github/workflows/ci-report.yml (28 lines) → service: report-service, build-docker: false
Archivo: .github/workflows/ci-gateway.yml (28 lines) → service: api-gateway, build-docker: false
```

**Impacto:** Cada push ejecuta 2 workflows por servicio (el individual + ci-full.yml). Duplicación de ejecuciones.

### Hallazgo 2 — ALTO: SonarCloud duplicado en 2 workflows

**Evidencia:**
- `ci-template.yml:65` — `mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar`
- `build.yml:41` — `mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar`

**Impacto:** En cada push a master, SonarCloud se ejecuta 14 veces (7 del template + 7 de build.yml). Mismo análisis, misma configuración.

### Hallazgo 3 — MEDIO: ci-full.yml sin trigger en PRs

**Evidencia:** `ci-full.yml:3-5` solo tiene `push`, no `pull_request`.

**Impacto:** Los Pull Requests no ejecutan el CI completo. Solo `build.yml` corre en PRs (SonarCloud). El build, tests, Docker y Trivy no se ejecutan en PRs.

---

## FASE 3 — VALIDACIÓN ACADÉMICA

| # | Etapa académica | Estado | Archivo:Línea | Evidencia |
|---|----------------|--------|--------------|-----------|
| 1 | Obtención de código | ✅ Completo | `ci-template.yml:28` | `actions/checkout@v4.1.7` |
| 2 | Compilación | ✅ Completo | `ci-template.yml:53` | `mvn clean verify` |
| 3 | Testing | ✅ Completo | `ci-template.yml:53` | 313 tests JUnit 5 + Mockito |
| 4 | Análisis estático | ✅ Completo | `ci-template.yml:65-70` | SonarCloud |
| 5 | Empaquetado | ✅ Completo | `ci-template.yml:81-88` | Upload JAR como artefacto |
| 6 | Seguridad | ✅ Completo | `ci-template.yml:134-145` | Trivy |
| 7 | Automatización | ✅ Completo | `ci-full.yml:3-5` | Push trigger automático |
| 8 | Publicación | ✅ Completo | `ci-template.yml:90-97` | SBOM + JAR + test reports |
| 9 | **Despliegue** | ❌ **Ausente** | — | No hay step de deploy |
| 10 | **Monitoreo** | ❌ **Ausente** | — | No hay monitoreo post-build |

---

## FASE 4 — SONARCLOUD (VERIFICADO)

| Aspecto | Estado | Archivo:Línea |
|---------|--------|--------------|
| Configuración existe | ✅ | `sonar-project.properties` |
| Project Key configurado | ✅ | `ci-template.yml:66` — `Santiago-AGZ_Backend-AxisERP` |
| Organization configurada | ✅ | `ci-template.yml:67` — `santiago-alvarez-gutierrezaxiserp-backend` |
| Token en Secrets | ✅ | GitHub Secrets — `SONAR_TOKEN` |
| JaCoCo genera reports | ✅ | 7 POMs con plugin jacoco:0.8.12 |
| Análisis en PRs | ✅ | `build.yml:6-7` — pull_request trigger |
| Quality Gate blocker | ❌ **Ausente** | No hay `sonar.qualitygate.wait=true` |
| Branch protection | ❌ **Ausente** | No hay reglas de protección |
| Duplicado en 2 workflows | ❌ **Sí** | `ci-template.yml:65` + `build.yml:41` |

**Conclusión:** SonarCloud está configurado correctamente y debería funcionar. Pero sin Quality Gate blocker ni branch protection, los resultados no bloquean el desarrollo.

---

## FASE 5 — DEPENDABOT (VERIFICADO)

| Aspecto | Estado | Archivo |
|---------|--------|---------|
| Ecosistemas monitoreados | ✅ Maven + GitHub Actions | `.github/dependabot.yml:4,66` |
| Frecuencia | ✅ Semanal (lunes) | `.github/dependabot.yml:6-7` |
| Límite PRs | ✅ 10 máximos | `.github/dependabot.yml:8` |
| Labels personalizados | ✅ | `.github/dependabot.yml:9-11` |
| **Cumple buenas prácticas** | **✅ SÍ** | — |

---

## FASE 6 — SEGURIDAD DEVSECOPS (VERIFICADO)

| Aspecto | Estado | Archivo:Línea |
|---------|--------|--------------|
| Trivy escanea imágenes | ✅ | `ci-template.yml:137-145` |
| Trivy usa --exit-code 1 | ✅ | `ci-template.yml:143` |
| Trivy severidad CRITICAL+HIGH | ✅ | `ci-template.yml:142` |
| Hadolint linting Dockerfiles | ✅ | `ci-template.yml:106-108` |
| Docker multi-stage | ✅ 7/7 | Todos los Dockerfiles |
| Docker non-root user | ✅ 7/7 | appuser:1001 |
| Docker HEALTHCHECK | ✅ 7/7 | wget a actuator/health |
| Secrets en GitHub Secrets | ✅ | `SONAR_TOKEN` |
| Secrets no hardcodeados | ✅ | Ninguno en código |

**Clasificación:** Correcto — todas las prácticas de seguridad están implementadas.

---

## FASE 7 — CI/CD REAL (EVIDENCIA DIRECTA)

| Pregunta | Respuesta | Evidencia |
|----------|-----------|-----------|
| ¿Existe CI? | **✅ SÍ** | `ci-full.yml:3-5` trigger push + `ci-template.yml` con 15 etapas |
| ¿Existe CD desde Actions? | **❌ NO** | Ningún workflow tiene step de deploy |
| ¿Existe Continuous Delivery? | **⚠️ PARCIAL** | Render auto-despliega desde GitHub branch, no desde Actions |
| ¿Existe Continuous Deployment? | **❌ NO** | Sin pipeline de deploy automático |
| ¿Existe GitOps? | **❌ NO** | No implementado |
| ¿Render despliega desde Actions? | **❌ NO** | Render detecta cambios en GitHub directamente |
| ¿Vercel despliega desde Actions? | **❌ NO** | Vercel detecta cambios en GitHub directamente |
| ¿El pipeline entrega software realmente? | **⚠️ PARCIAL** | Entrega artefactos (JAR, Docker images), pero no los despliega |

---

## FASE 8 — BRANCH PROTECTION (VERIFICADO)

| Aspecto | Estado | Evidencia |
|---------|--------|-----------|
| pull_request trigger en ci-full.yml | ❌ **Ausente** | `ci-full.yml:3-5` solo push |
| pull_request trigger en build.yml | ✅ Presente | `build.yml:6-7` |
| Status checks en GitHub | ❌ **Ausente** | Sin reglas de branch protection |
| Quality Gate blocker | ❌ **Ausente** | Sin `sonar.qualitygate.wait=true` |
| Merge protection rules | ❌ **Ausente** | No configurado en GitHub |

---

## FASE 9 — CALIDAD DEL DISEÑO

| Criterio | Nota /5 | Justificación |
|----------|---------|---------------|
| Reutilización | **4.5** | Template reusable con `workflow_call` e inputs tipados |
| Mantenibilidad | **3.0** | 11 workflows dificultan el mantenimiento. Deberían ser 3-4 |
| Complejidad | **3.0** | Arquitectura de workflows confusa: orquestador + individuales + sonar separado |
| Modularidad | **4.0** | Buena separación CI/Docker/release, pero SonarCloud duplicado |
| Escalabilidad | **3.5** | Matrix en build.yml para 7 servicios. Template escalable. |
| **PROMEDIO** | **3.6 / 5** | |

---

## FASE 10 — PREPARACIÓN PARA SUSTENTACIÓN

### 20 preguntas probables del profesor

| # | Pregunta | Respuesta |
|---|----------|-----------|
| 1 | ¿Cuántos workflows tiene el proyecto? | "Once. Pero 7 son redundantes." |
| 2 | ¿Por qué tantos? | "Creamos uno por servicio al inicio, después aprendimos a usar templates." |
| 3 | ¿Dónde está el CD? | "Render y Vercel hacen auto-deploy desde GitHub. No desde Actions." |
| 4 | ¿Qué hace ci-full.yml? | "Orquesta la construcción de los 7 servicios en paralelo usando el template." |
| 5 | ¿Y los ci-*.yml individuales? | "Son redundantes. ci-full.yml ya ejecuta todo." |
| 6 | ¿Cómo verifican calidad? | "SonarCloud analiza cada servicio + JaCoCo para cobertura." |
| 7 | ¿El Quality Gate bloquea merges? | "No todavía. Es una mejora pendiente." |
| 8 | ¿Qué seguridad tienen? | "Trivy para imágenes, Hadolint para Dockerfiles, Dependabot para dependencias." |
| 9 | ¿Cuántos tests? | "313 tests unitarios con JUnit 5 y Mockito." |
| 10 | ¿Cómo se versiona? | "Tags semánticos (v1.0.0) que disparan ci-release.yml." |
| 11 | ¿Por qué build.yml si ya hay SonarCloud en el template? | "Se agregó durante la configuración inicial de SonarCloud. Duplica funcionalidad." |
| 12 | ¿Qué es Dependabot? | "Herramienta que abre PRs automáticos cuando hay nuevas versiones de dependencias." |
| 13 | ¿Qué pasa si Trivy encuentra una vulnerabilidad? | "El pipeline falla con exit code 1." |
| 14 | ¿Los Dockerfiles son seguros? | "Sí: multi-stage, non-root (appuser:1001), HEALTHCHECK." |
| 15 | ¿Cómo se protegen los secretos? | "GitHub Secrets para SONAR_TOKEN. Ningún secreto hardcodeado." |
| 16 | ¿Por qué no usaron Jenkins? | "GitHub Actions tiene integración nativa, no requiere infraestructura propia." |
| 17 | ¿Qué aprendieron? | "Templates reutilizables, SonarCloud, Trivy, Dependabot, buenas prácticas CI/CD." |
| 18 | ¿El pipeline entrega el software? | "Entrega artefactos compilados y Docker images. El deploy lo hace Render." |
| 19 | ¿Cuál es la principal debilidad? | "Los 7 workflows redundantes y la falta de CD unificado." |
| 20 | ¿Qué mejorarían? | "Eliminar redundancias, unificar CD en Actions, agregar Quality Gate blocker." |

### Riesgos de Sustentación

| Riesgo | Probabilidad | Impacto |
|--------|-------------|---------|
| "¿Por qué 11 workflows?" | **ALTA** | **ALTO** |
| "¿Dónde está el deploy en Actions?" | **ALTA** | **ALTO** |
| "¿Por qué SonarCloud está duplicado?" | MEDIA | ALTO |

### Fortalezas
- Template reusable con `workflow_call`
- Integración completa (build, test, Docker, SonarCloud, Trivy)
- 313 tests
- Dockerfiles seguros
- Documentación académica de calidad

### Debilidades
- 7 workflows redundantes
- Sin CD desde Actions
- Sin Quality Gate blocker
- Sin branch protection
- SonarCloud duplicado

---

## FASE 11 — PLAN DE REMEDIACIÓN

### Obligatorio antes de entregar

| # | Acción | Archivo | Tiempo | Impacto |
|---|--------|---------|--------|---------|
| 1 | Eliminar los 7 workflows redundantes | `ci-auth.yml` a `ci-gateway.yml` | 2 min | **CRÍTICO** |
| 2 | Agregar `pull_request` trigger a `ci-full.yml` | `ci-full.yml:4` | 1 min | ALTO |
| 3 | Preparar respuesta sobre CD externo | — | 10 min | ALTO |

### Recomendado

| # | Acción | Dificultad | Tiempo | Impacto |
|---|--------|-----------|--------|---------|
| 4 | Unificar SonarCloud (eliminar de template.yml o build.yml) | Baja | 2 min | ALTO |
| 5 | Agregar `sonar.qualitygate.wait=true` a build.yml | Baja | 1 min | ALTO |
| 6 | Configurar Branch Protection Rules en GitHub | Media | 5 min | ALTO |

### Opcional

| # | Acción | Dificultad | Tiempo | Impacto |
|---|--------|-----------|--------|---------|
| 7 | Agregar deploy a Render desde Actions | Media | 30 min | ALTO |
| 8 | Guardar reportes Trivy como artefacto | Baja | 5 min | BAJO |

---

## VEREDICTO FINAL

| Categoría | Nota |
|-----------|------|
| **Nota técnica** (calidad del pipeline) | **3.6 / 5.0** |
| **Nota académica** (cumplimiento actividad) | **3.9 / 5.0** |

| Aspecto | Nivel |
|---------|-------|
| CI | ✅ **Completo** — build, test, SonarCloud, Docker, Trivy |
| CD | ❌ **Ausente en Actions** — Render auto-despliega externamente |
| DevOps | ⚠️ 2.6/5 — CI fuerte, CD débil |
| Estado sustentación | ⚠️ **APROBADO CON OBSERVACIONES** |

### Hallazgos falsos de auditorías anteriores (corregidos)

| Hallazgo falso | Realidad |
|----------------|----------|
| "No hay análisis estático" | ✅ SonarCloud configurado y funcionando |
| "No hay JaCoCo" | ✅ 7 POMs con JaCoCo 0.8.12 |
| "No hay Dependabot" | ✅ `.github/dependabot.yml` con 8 entradas |
| "No hay release pipeline" | ✅ `ci-release.yml` con changelog automático |
| "El pipeline no funciona" | ✅ Build, tests, Docker, Trivy se ejecutan correctamente |

### Hallazgos reales confirmados

| Hallazgo | Severidad | Evidencia |
|----------|-----------|-----------|
| 7 workflows redundantes | **CRÍTICO** | 28 líneas cada uno, misma estructura |
| SonarCloud duplicado | **ALTO** | `ci-template.yml:65` + `build.yml:41` |
| Sin trigger PR en ci-full.yml | **MEDIO** | `ci-full.yml:3-5` solo push |
| Sin Quality Gate blocker | **ALTO** | No hay `sonar.qualitygate.wait=true` |
| Sin CD desde Actions | **ALTO** | Ningún workflow tiene step de deploy |
| Sin branch protection | **ALTO** | No hay reglas en GitHub |
