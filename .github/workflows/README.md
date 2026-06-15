# Pipeline CI/CD de AxisERP

## Que es un Pipeline

Un pipeline es una secuencia automatizada de pasos donde la salida de una etapa se convierte en la entrada de la siguiente. Su objetivo es automatizar tareas, reducir errores humanos y garantizar que el software pueda construirse, probarse y desplegarse de manera consistente.

En un proyecto sin pipelines, cada vez que un desarrollador hace un cambio debe compilar manualmente, ejecutar pruebas, construir una imagen y subirla al servidor. Con pipelines, todo esto ocurre automaticamente al subir codigo al repositorio.

En AxisERP los pipelines se implementan mediante GitHub Actions y cubren compilacion, pruebas unitarias, construccion de imagenes Docker, publicacion en Azure Container Registry y despliegue en Azure Container Apps.

---

## Objetivo de los Pipelines de AxisERP

Los pipelines automatizan las siguientes tareas:

- Compilacion de cada microservicio con Maven y JDK 21
- Ejecucion de pruebas unitarias
- Publicacion de reportes de prueba como artefactos
- Construccion de imagenes Docker sin cache
- Publicacion de imagenes en Azure Container Registry
- Despliegue automatico en Azure Container Apps
- Escaneo de seguridad con CodeQL

---

## Arquitectura Actual de Pipelines

```
Desarrollador
     |
     v
Push a GitHub (rama master o pull request)
     |
     v
CI del microservicio modificado
     |
     +-- Compilacion Maven
     +-- Pruebas unitarias
     +-- Publicacion de resultados
     +-- Build Docker (sin cache)
     +-- Push a Azure Container Registry (solo en master)
     |
     v
CD (se ejecuta solo si CI fue exitoso)
     |
      +-- Autenticacion Azure (usuario/password)
      +-- Despliegue en Azure Container Apps
```

---

## Workflows Existentes

Se encuentran 10 archivos de workflow en el directorio `.github/workflows/`:

| Workflow | Proposito |
|---|---|
| `template-ci.yml` | Plantilla reutilizable con las etapas comunes de CI |
| `ci-auth.yml` | CI del microservicio de autenticacion |
| `ci-catalog.yml` | CI del microservicio de catalogo |
| `ci-gateway.yml` | CI del API Gateway |
| `ci-inventory.yml` | CI del microservicio de inventario |
| `ci-sales.yml` | CI del microservicio de ventas |
| `ci-purchase.yml` | CI del microservicio de compras |
| `ci-report.yml` | CI del microservicio de reportes |
| `cd-deploy.yml` | Despliegue automatico en Azure |
| `security-scan.yml` | Escaneo de seguridad (CodeQL) |

---

## Flujo CI (Integracion Continua)

Cada microservicio tiene su propio workflow de CI. Cuando un desarrollador sube cambios a un microservicio, se ejecuta el workflow correspondiente.

Por ejemplo, si se modifica `catalog-service/`, se ejecuta `ci-catalog.yml`.

Cada workflow CI invoca a `template-ci.yml`, que contiene las siguientes etapas:

### 1. Descarga del codigo

Se obtiene la version mas reciente del repositorio mediante `actions/checkout@v4`.

### 2. Configuracion de JDK 21

Se instala Java 21 con distribucion Temurin y se habilita cache de Maven para acelerar compilaciones futuras.

### 3. Compilacion

Se ejecuta `mvn compile` para verificar que el proyecto compile correctamente.

### 4. Pruebas

Se ejecuta `mvn test` para correr las pruebas unitarias del microservicio.

### 5. Publicacion de resultados

Los reportes generados por Maven Surefire se almacenan como artefactos de GitHub Actions con una retencion de 14 dias. Esto permite revisar el detalle de fallos sin necesidad de ejecutar las pruebas localmente.

### 6. Empaquetado

Se ejecuta `mvn package -DskipTests` para generar el archivo JAR del microservicio.

### 7. Construccion de imagen Docker

Se construye la imagen Docker utilizando `docker build --no-cache`. La bandera `--no-cache` evita que Docker reuse capas de compilacion anteriores, garantizando que la imagen refleje exactamente el codigo actual.

### 8. Publicacion en Azure Container Registry

Si el cambio se realiza en la rama `master`, la imagen se publica en Azure Container Registry con dos etiquetas:

- `axiserp.azurecr.io/<servicio>:<run_number>` (etiqueta unica por ejecucion)
- `axiserp.azurecr.io/<servicio>:latest` (etiqueta de ultima version)

En pull requests no se publican imagenes en el registro. La construccion Docker se ejecuta solo como validacion.

---

## Flujo CD (Despliegue Continuo)

El workflow `cd-deploy.yml` se encarga del despliegue. A diferencia de los workflows CI que se activan directamente por push o pull request, el CD se activa mediante el evento `workflow_run`.

### Condiciones de ejecucion

- Se activa cuando cualquiera de los 7 workflows CI completa su ejecucion en la rama `master`.
- Solo procede si la ejecucion del CI fue exitosa (`conclusion == 'success'`).
- Tambien puede ejecutarse manualmente mediante `workflow_dispatch`.

### Etapas del despliegue

1. **Autenticacion en Azure**: Se utiliza `az login -u/-p` con las credenciales almacenadas en `secrets.AZURE_USER` y `secrets.AZURE_PASS`.
2. **Autenticacion en ACR**: Se ejecuta `az acr login` para autenticarse en el registro de contenedores.
3. **Actualizacion del Container App**: Se ejecuta `az containerapp update` para cada microservicio configurado en la matriz (auth-service, catalog-service, inventory-service, sales-service, purchase-service, report-service, api-gateway).

El despliegue actualiza el Container App con la imagen etiquetada con el numero de ejecucion del workflow CI que origino el despliegue (o `:latest` si se ejecuta manualmente).

---

## Pipeline de Seguridad

El workflow `security-scan.yml` ejecuta analisis estatico de seguridad:

### CodeQL Analysis

- Analisis estatico de codigo para Java/Kotlin.
- Compila todos los microservicios y ejecuta las consultas de seguridad de CodeQL.
- Se ejecuta en cada push a master, en cada pull request y semanalmente.
- Los resultados se publican en la pestana Security del repositorio de GitHub.

---

## Ejemplo Practico

**Escenario**: Un desarrollador modifica el microservicio `catalog-service` para agregar un nuevo endpoint.

**Paso a paso**:

1. El desarrollador crea una rama, realiza los cambios y abre un Pull Request hacia `master`.
2. Al abrir el PR, se activa `ci-catalog.yml`.
3. `ci-catalog.yml` invoca a `template-ci.yml` con `service: catalog-service`.
4. El template ejecuta:
   - `mvn compile` para verificar que compila.
   - `mvn test` para ejecutar las pruebas.
   - Se publican los resultados como artefactos.
   - `mvn package` para generar el JAR.
   - `docker build --no-cache` para verificar que la imagen se construye.
   - El paso de Push a ACR se salta porque no esta en la rama `master`.
5. Si el PR es aprobado y se fusiona a `master`, se ejecuta nuevamente `ci-catalog.yml`.
6. Al estar en `master`, el template ahora ejecuta el Push a ACR, publicando la imagen con dos etiquetas (numero de ejecucion y latest).
7. Al finalizar el CI exitosamente, se activa `cd-deploy.yml` mediante `workflow_run`.
8. `cd-deploy.yml` verifica que la ejecucion del CI fue exitosa.
9. Se autentica en Azure y en ACR.
10. Ejecuta `az containerapp update` para desplegar la nueva imagen de `catalog-service` en Azure Container Apps.

Los demas microservicios no se compilan, no se prueban ni se despliegan. Solo se procesa el servicio modificado.

---

## Ventajas del Pipeline Actual

- **Automatizacion completa del backend**: Todo el proceso, desde el codigo hasta la produccion, ocurre sin intervencion manual.
- **Despliegues controlados**: El CD solo se ejecuta si el CI fue exitoso. Un fallo en las pruebas detiene el despliegue.
- **Trazabilidad de cambios**: Cada ejecucion queda registrada en GitHub Actions con sus resultados, logs y artefactos.
- **Escalabilidad para nuevos servicios**: Agregar un nuevo microservicio requiere crear un CI de 12 lineas que invoque al template.
- **Separacion entre CI y CD**: La integracion continua y el despliegue continuo son workflows independientes con responsabilidades diferentes.
- **Despliegue por servicio modificado**: Solo se reconstruye y despliega el microservicio que cambio, reduciendo tiempos de ejecucion.
- **Seguridad basica**: CodeQL analiza el codigo fuente en busca de vulnerabilidades.
- **Cache de Maven**: Las dependencias se cachean entre ejecuciones para acelerar compilaciones sucesivas.

---

## Conclusión

Los pipelines de AxisERP implementan un flujo de integracion continua y despliegue continuo para 7 microservicios Spring Boot. La arquitectura utiliza un workflow reutilizable para evitar duplicacion de codigo, separa claramente la responsabilidad de CI y CD, e incluye un pipeline independiente de seguridad.

El diseno actual permite que el equipo de desarrollo se concentre en escribir codigo mientras los pipelines se encargan de validar, construir y desplegar cada cambio de forma automatica y consistente.
