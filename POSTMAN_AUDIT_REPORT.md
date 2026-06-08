# AXISERP — POSTMAN AUDIT + VALIDATION REPORT

## GATEWAY STATUS

| Check | Result | Details |
|-------|--------|---------|
| Gateway Reachable | ✅ | `https://api-gateway-quvd.onrender.com` responde |
| Health Endpoint | ✅ UP | `/actuator/health` → `{"status":"UP"}` |
| Login Endpoint | ✅ Responde | HTTP 401 con estructura ApiResponse correcta |

---

## LOGIN TEST

| Campo | Request | Controller Espera | Resultado |
|-------|---------|-------------------|-----------|
| email/username | `santiagoalvarez374@gmail.com` | `email` (record) | ❌ CREDENCIALES INVÁLIDAS |
| password | `Admin123!` | `password` | — |

**Usuario no existe en el entorno desplegado o contraseña incorrecta.**

---

## USUARIOS EXISTENTES EN CÓDIGO

**No se encontraron seeders, data.sql, import.sql, CommandLineRunner, Flyway ni Liquibase.**

Los usuarios deben crearse a través de la API:
- `POST /api/v1/usuarios` (requiere rol ADMIN)
- O desde Supabase Auth directamente

No existen usuarios predefinidos en el código fuente.

---

## POSTMAN COLLECTION — PROBLEMAS DETECTADOS

### PROBLEMA 1: Campo incorrecto en Login (CRÍTICO)

**Archivo:** `auth-service/AxisERP-Auth-Service.postman_collection.json:39,72`

**Postman envía:**
```json
{ "username": "admin@axiserp.com", "password": "admin123" }
```

**Controller espera (AuthController.java:72):**
```java
private record LoginRequestBody(@NotBlank @Email String email,
                                 @NotBlank String password) {}
```

**Corrección necesaria:**
```json
{ "email": "admin@axiserp.com", "password": "admin123" }
```

### PROBLEMA 2: Test script lee token en nivel incorrecto (CRÍTICO)

**Archivo:** `auth-service/AxisERP-Auth-Service.postman_collection.json:54-55`

**Postman script actual:**
```javascript
pm.collectionVariables.set('access_token', json.accessToken);
pm.collectionVariables.set('refresh_token', json.refreshToken);
```

**La respuesta real es:**
```json
{
  "success": true,
  "code": "SUCCESS",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "role": "...",
    "name": "..."
  }
}
```

**Corrección necesaria:**
```javascript
pm.collectionVariables.set('access_token', json.data.accessToken);
pm.collectionVariables.set('refresh_token', json.data.refreshToken);
```

### PROBLEMA 3: base_url apunta a localhost (CONFIGURACIÓN)

**Archivo:** `auth-service/AxisERP-Auth-Service.postman_collection.json:10`

**Variable actual:** `"value": "http://localhost:8081"`

**Para entorno desplegado debería ser:**
```
https://api-gateway-quvd.onrender.com
```

**O usar variable de entorno Postman para alternar entre local/producción.**

---

## FLUJO DE AUTENTICACIÓN

| Paso | Endpoint | Autenticación | Estado |
|------|----------|---------------|--------|
| Login | `POST /api/v1/auth/login` | Pública | ✅ Endpoint responde |
| Obtener Token | — | — | ⚠️ Test script roto (lee nivel incorrecto) |
| Endpoint Protegido | Cualquier `@PreAuthorize` | Bearer Token | ❓ No verificado (login falla) |
| Refresh Token | `POST /api/v1/auth/refresh` | Pública | ❓ No verificado |
| Logout | `POST /api/v1/auth/logout` | Autenticado | ❓ No verificado |

---

## CONCLUSIONES

### Postman Collection: FAIL ❌

| Problema | Severidad | Impacto |
|----------|-----------|---------|
| Login usa `username` en vez de `email` | CRÍTICO | Login siempre falla con 400/422 |
| Script extrae token de nivel incorrecto | CRÍTICO | Variables `access_token` y `refresh_token` nunca se setean |
| `base_url` apunta a localhost:8081 | ALTO | No funciona contra entorno desplegado |
| Solo auth-service tiene colección | MEDIO | 6 servicios sin Postman |

### Entorno Desplegado: WARNING ⚠️

| Aspecto | Estado |
|---------|--------|
| Gateway accesible | ✅ |
| Health check responde | ✅ |
| Login endpoint funcional | ✅ (responde correctamente 401) |
| Usuario de prueba existe | ❌ No en entorno desplegado |
| Seed data en código | ❌ No existe |

### Credenciales de Prueba: No verificables

No se encontraron usuarios seed en el código. Las credenciales proporcionadas (`santiagoalvarez374@gmail.com` / `Admin123!`) no existen en el entorno desplegado.

---

## RECOMENDACIONES

1. **Corregir Postman collection** — Cambiar `username` → `email` en login body
2. **Corregir test script** — Leer `json.data.accessToken` en vez de `json.accessToken`
3. **Agregar seed data** — Crear `CommandLineRunner` o `import.sql` con usuarios de prueba
4. **Configurar variable `base_url`** — Usar variable de entorno Postman
5. **Generar colecciones** para catalog, inventory, purchase, sales, report, gateway
