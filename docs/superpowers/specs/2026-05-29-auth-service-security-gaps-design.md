# Auth Service — Security & Business Rules Gap Resolution

**Date:** 2026-05-29

**Status:** Design — pending implementation

---

## Problem

Auth-service tiene 55 reglas de negocio de las cuales varias críticas no están implementadas:
- `User.isActive()` nunca se ejecuta en runtime (reglas 8-11)
- Sin protección contra fuerza bruta (reglas 43-44, OWASP A07)
- Sin flujo de recuperación de contraseña (reglas 26-29)
- Casos de uso no validan estado del usuario objetivo (reglas 9-10, 12)
- Sin JIT provisioning para usuarios creados fuera del flujo del auth-service

---

## Arquitectura de Solución

### 1. UserStatusFilter + JIT Provisioning (reglas 8-11, OWASP A01)

Nuevo filter en la cadena de seguridad de Spring que:
1. Lee `sub` (userId) del JWT autenticado
2. Busca perfil en tabla `profiles`
3. **Si no existe (JIT):** crea perfil automático desde claims del JWT:
   - `name` ← `user_metadata.name` (fallback: `email`)
   - `email` ← `email` claim
   - `role_id` ← lookup por `app_metadata.role` (default: INVENTARIO)
   - `status` ← `PENDIENTE`
   - `created_by` ← null (auto-provisioned)
   - Log de auditoría: `AuditAction.JIT_PROVISION`
4. **Si existe y no es ACTIVO:** retorna 403 "Usuario inactivo o eliminado"
5. **Si es ACTIVO:** `chain.doFilter()`

Posición en la cadena: después de `FirstLoginFilter`, antes de `BearerTokenAuthenticationFilter`.

```
SecurityFilterChain:
  FirstLoginFilter (promociona PENDIENTE→ACTIVO si email confirmado)
  → UserStatusFilter (JIT provision + bloquea !isActive())
  → BearerTokenAuthenticationFilter (JWT validation)
```

### 2. Validaciones en Casos de Uso (reglas 9-10, 12)

| Caso de Uso | Validación | HTTP Response |
|-------------|-----------|---------------|
| `CreateUserUseCaseImpl` | Verificar `adminId` creator tiene `isActive()` | 403 si no |
| `UpdateUserUseCaseImpl` | Verificar target user no es ELIMINADO | 404 si ELIMINADO |
| `DeactivateUserUseCaseImpl` | Agregar check contra ELIMINADO | 409 si ya ELIMINADO |
| `GetUserUseCaseImpl` | No retornar ELIMINADO (admin puede pedirlo explícitamente) | 404 por defecto |

### 3. Rate Limiting (reglas 43-44, OWASP A07)

**Biblioteca:** Bucket4j + `spring-boot-starter-cache` + Caffeine.

| Límite | Ventana | Máximo |
|--------|---------|--------|
| General (por IP) | 1 minuto | 100 requests |
| Escritura (POST/PUT/PATCH/DELETE, por usuario) | 1 minuto | 20 requests |
| Auth endpoints | 1 minuto | 5 requests |

Response: 429 Too Many Requests con header `Retry-After`.

### 4. Password Reset (reglas 26-29)

**Delegar a Supabase Auth.** No implementar lógica local.

Endpoint único:

```
POST /api/v1/auth/password-reset
Request: { "email": "user@example.com" }
Response: 200 { "message": "Si el correo existe, recibirás un enlace de recuperación" }
Response (si email no existe): 200 mismo mensaje (no revelar existencia)
```

El endpoint llama internamente a `POST https://<supabase>/auth/v1/recover` con el email. Supabase envía el correo con su template SMTP configurado.

---

## Archivos a Modificar

### Nuevos archivos
- `infrastructure/config/UserStatusFilter.java` — Filter + JIT provisioning
- `application/service/JitProvisioningService.java` — Lógica de auto-creación de perfil

### Archivos existentes a modificar
- `infrastructure/config/SecurityConfig.java` — Registrar `UserStatusFilter`
- `application/usecase/CreateUserUseCaseImpl.java` — Validar admin activo
- `application/usecase/UpdateUserUseCaseImpl.java` — Validar target no ELIMINADO
- `application/usecase/DeactivateUserUseCaseImpl.java` — Validar contra ELIMINADO
- `application/usecase/GetUserUseCaseImpl.java` — No retornar ELIMINADO por defecto
- `infrastructure/adapters/in/web/controller/AuthController.java` — Endpoint password-reset
- `pom.xml` — Agregar Bucket4j + Caffeine
- `infrastructure/adapters/out/supabase/SupabaseAdminAdapter.java` — Método `sendPasswordReset()`
- `ports/output/SupabaseAuthPort.java` — Nuevo método `sendPasswordReset()`

## Pruebas

- `UserStatusFilterTest` — 5 tests: JIT provision, bloquea INACTIVO, bloquea ELIMINADO, permite ACTIVO, skip actuator
- `CreateUserUseCaseImplTest` — agregar test admin inactivo → 403
- `UpdateUserUseCaseImplTest` — agregar test target ELIMINADO → 404
- `DeactivateUserUseCaseImplTest` — agregar test target ELIMINADO → 409

## Seguridad (OWASP)

| A01 Broken Access Control | UserStatusFilter + validación en use cases |
| A07 Authentication Failures | Rate limiting con Bucket4j, password reset delegado a Supabase |
| A09 Logging Failures | Audit log en JIT provision + rate limit events |
