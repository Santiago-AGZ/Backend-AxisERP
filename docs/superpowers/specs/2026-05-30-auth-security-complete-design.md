# Auth-Service Security Implementation Design
**Date:** 2026-05-30  
**Project:** AxisERP Platform  
**Status:** APPROVED  

---

## 1. OBJETIVO

Implementar un sistema de seguridad completo para el auth-service que cumpla con:
- ✅ 55 reglas de negocio (auditoría completa)
- ✅ Estándares ISO27001 y GDPR
- ✅ Mejores prácticas de ERP financiero

---

## 2. DECISIONES ARQUITECTÓNICAS

### 2.1 Token Lifecycle
- **Access Token:** 15 minutos (JWT de Supabase)
- **Refresh Token:** 7 días (almacenado en Neon)
- **Justificación:** Balance entre seguridad y usabilidad en ERP

### 2.2 Token Blacklist Storage
- **Ubicación:** Neon PostgreSQL
- **Tabla:** `token_blacklist`
- **Justificación:** Persistencia entre reinicios, compartible en cluster

### 2.3 Reautenticación
- **Método:** OTP por email (6 dígitos)
- **Duración OTP:** 10 minutos
- **Duración Token OTP:** 5 minutos después de verificación
- **Operaciones que requieren:** Crear ADMIN, Cambiar email, Cambiar rol, Deactivar usuario
- **Justificación:** Cumplimiento normativo + verifica propiedad de email

### 2.4 Validación de Contraseña
- **Longitud:** 8-128 caracteres
- **Requisitos:** Mayúscula + minúscula + número + carácter especial
- **Caracteres especiales permitidos:** `@#$%^&*!`
- **No permitido:** Espacios en blanco
- **Justificación:** OWASP + regulaciones financieras

### 2.5 Testing
- **Automatizado:** JUnit (5 suites, ~50 test cases)
- **Manual:** Postman (validación de flujos end-to-end)
- **Justificación:** Confiabilidad + cobertura

---

## 3. NUEVAS ENTIDADES

### 3.1 TokenBlacklistEntity

```java
@Entity
@Table(name = "token_blacklist")
public class TokenBlacklistEntity {
    @Id
    private UUID id;                          // UUID del token
    
    @Column(nullable = false)
    private String tokenJti;                  // JWT claim "jti"
    
    @Column(nullable = false)
    private UUID userId;                      // Usuario
    
    @Column(nullable = false)
    private LocalDateTime revokedAt;          // Cuándo se revocó
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;          // Cuándo se limpia de BD
}
```

**Propósito:** Mantener registro de tokens revocados (logout)  
**Ciclo de vida:** Crear en logout, limpiar después de expiración  
**Índices:** `UNIQUE(tokenJti)`, `INDEX(userId)`, `INDEX(expiresAt)`

---

### 3.2 RefreshTokenEntity

```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false, unique = true)
    private String token;                     // Hash del token (no plain text)
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;          // 7 días
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "ip_address")
    private String ipAddress;                 // Auditoría
    
    @Column(name = "user_agent")
    private String userAgent;                 // Auditoría
}
```

**Propósito:** Almacenar refresh tokens para renovación de access tokens  
**Seguridad:** Token almacenado como hash (SHA-256)  
**Auditoría:** Registra IP y User-Agent

---

### 3.3 OtpTokenEntity

```java
@Entity
@Table(name = "otp_tokens")
public class OtpTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private String otpCode;                   // Hash de código (no plain)
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;          // 10 minutos
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "attempts")
    private Integer attempts = 0;             // Max 3 intentos
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;             // NULL = no usado
}
```

**Propósito:** Almacenar OTP para reautenticación  
**Seguridad:** OTP hash, no plain text  
**Protección:** Max 3 intentos fallidos

---

## 4. NUEVOS SERVICIOS

### 4.1 PasswordValidator
- **Ubicación:** `com.axiserp.auth.domain.service`
- **Métodos:**
  - `validate(String password)` → throws `WeakPasswordException`
  - `validateCreateUser(CreateUserRequest)` → throws `WeakPasswordException`

### 4.2 TokenBlacklistService
- **Ubicación:** `com.axiserp.auth.application.service`
- **Métodos:**
  - `revoke(String tokenJti, UUID userId)` → void
  - `isRevoked(String tokenJti)` → boolean
  - `cleanupExpired()` → void (scheduled task)

### 4.3 RefreshTokenService
- **Ubicación:** `com.axiserp.auth.application.service`
- **Métodos:**
  - `create(UUID userId, String ipAddress, String userAgent)` → String (token)
  - `validate(String token)` → RefreshTokenEntity
  - `revoke(String token)` → void
  - `rotate(String oldToken)` → String (nuevo token)

### 4.4 OtpService
- **Ubicación:** `com.axiserp.auth.application.service`
- **Métodos:**
  - `requestOtp(UUID userId, String email)` → void (envía por email)
  - `verifyOtp(UUID userId, String code)` → String (otp token)
  - `invalidateOtp(UUID userId)` → void

---

## 5. NUEVOS ENDPOINTS (TokenController)

```
POST   /api/v1/auth/logout
       Header: Authorization: Bearer {accessToken}
       Body: { "refreshToken": "uuid-xxx" }
       Response: { "success": true }

POST   /api/v1/auth/refresh
       Body: { "refreshToken": "uuid-xxx" }
       Response: { "accessToken": "...", "expiresIn": 900 }

POST   /api/v1/auth/reauth-request
       Header: Authorization: Bearer {accessToken}
       Body: { "email": "user@example.com" }
       Response: { "success": true, "message": "OTP enviado" }

POST   /api/v1/auth/reauth-verify
       Body: { "otpCode": "123456" }
       Response: { "otpToken": "...", "expiresIn": 300 }

GET    /api/v1/auth/validate-token
       Header: Authorization: Bearer {accessToken}
       Response: { "valid": true, "userId": "...", "expiresAt": "..." }
```

---

## 6. VALIDACIONES DE CONTRASEÑA

Aplicadas en:
- `CreateUserUseCaseImpl` (creación de usuario)
- `UpdateUserUseCaseImpl` (cambio de contraseña)
- `SupabaseAdminAdapter.createUser()` (antes de enviar a Supabase)

**Reglas:**
1. Longitud: 8-128 caracteres
2. Mayúsculas: ≥1 `[A-Z]`
3. Minúsculas: ≥1 `[a-z]`
4. Números: ≥1 `[0-9]`
5. Especiales: ≥1 `[@#$%^&*!]`
6. Sin espacios

**Error:** `WeakPasswordException` → 400 Bad Request

---

## 7. OPERACIONES CRÍTICAS CON REAUTH

| Operación | Endpoint | Flujo |
|-----------|----------|-------|
| Crear ADMIN | POST `/api/v1/usuarios` | Solicitar OTP → Verificar → Crear |
| Cambiar email | PATCH `/api/v1/usuarios/{id}/cambiar-email` | Reauth OTP requerido |
| Cambiar rol | PATCH `/api/v1/usuarios/{id}/cambiar-rol` | Reauth OTP requerido |
| Deactivar usuario | PATCH `/api/v1/usuarios/{id}/desactivar` | Reauth OTP requerido |

**Implementación:** Decorator `@RequiresReauth` en use cases

---

## 8. FLUJOS COMPLETOS

### 8.1 LOGIN → TOKENS

```
1. POST /api/v1/auth/login
2. Supabase valida credenciales
3. Retorna JWT (access token)
4. Crear RefreshToken en BD
5. Retornar ambos tokens
```

### 8.2 REFRESH TOKEN

```
1. POST /api/v1/auth/refresh { refreshToken }
2. Validar refresh token en BD
3. Verificar no está expirado (7 días)
4. Generar nuevo access token
5. Retornar nuevo access token
```

### 8.3 LOGOUT

```
1. POST /api/v1/auth/logout { refreshToken }
2. Extraer UUID del JWT
3. Guardar en token_blacklist
4. Eliminar refresh token de BD
5. Retornar success
```

### 8.4 REAUTENTICACIÓN

```
A. RequestOtp:
   1. POST /api/v1/auth/reauth-request
   2. Generar código 6 dígitos
   3. Guardar hash en OtpTokenEntity (10 min)
   4. Enviar por email (Supabase)
   5. Retornar { success: true }

B. VerifyOtp:
   1. POST /api/v1/auth/reauth-verify { otpCode }
   2. Validar código (max 3 intentos)
   3. Generar OTP Token (5 min)
   4. Marcar OTP como usado
   5. Retornar { otpToken }

C. Usar OTP Token:
   1. Cliente envía operación crítica con header:
      Authorization: Bearer {otpToken}
   2. Validar que es OTP token (claim diferente)
   3. Permitir operación
   4. Invalidar OTP token
```

---

## 9. CAMBIOS A CONTROLADORES EXISTENTES

### 9.1 CreateUserUseCaseImpl
- ✅ Agregar validación de contraseña (si aplica)
- ✅ Si es ADMIN, requerir reautenticación OTP

### 9.2 UpdateUserUseCaseImpl
- ✅ Si cambia email, requerir reautenticación OTP
- ✅ Si cambia rol, requerir reautenticación OTP

### 9.3 DeactivateUserUseCaseImpl
- ✅ Requerir reautenticación OTP

### 9.4 Filters existentes
- ✅ UserStatusFilter: Validar que token no esté en blacklist
- ✅ RateLimitingFilter: Sin cambios

---

## 10. TESTING STRATEGY

### 10.1 JUnit Tests (5 suites)
- `PasswordValidatorTest` (10 cases)
- `TokenBlacklistServiceTest` (8 cases)
- `RefreshTokenServiceTest` (10 cases)
- `OtpServiceTest` (12 cases)
- `TokenControllerTest` (10 cases)

**Total:** ~50 test cases, 100% coverage de flujos críticos

### 10.2 Manual Testing (Postman)
- Login → Access + Refresh Token
- Refresh Token (múltiples veces)
- Logout (valida blacklist)
- Crear usuario ADMIN con contraseña débil (debe fallar)
- Crear usuario ADMIN con contraseña fuerte (debe pedir OTP)
- Cambiar email (debe pedir OTP)
- Token expirado (debe fallar)

---

## 11. SEGURIDAD

### 11.1 Hash de Secrets
- Refresh Token: SHA-256
- OTP Code: Bcrypt

### 11.2 Auditoría
- Todos los logouts se registran en `audit_log`
- Todos los OTP se registran en `audit_log`
- IP y User-Agent en `refresh_tokens` y `audit_log`

### 11.3 Rate Limiting
- OTP: Max 3 intentos fallidos (10 min)
- Tokens: RateLimitingFilter existente (100 req/min por IP)

---

## 12. DOCKER & COMPOSICIÓN

### 12.1 Cambios a application.properties
```
# Ningún cambio - usa variables de entorno existentes
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.jpa.hibernate.ddl-auto=update  # Crea nuevas tablas automáticamente
```

### 12.2 Cambios a compose.yml
```
# Sin cambios - auth-service ya levanta correctamente
```

### 12.3 Colección Postman
- ✅ Actualizar con nuevos endpoints
- ✅ Pre-request scripts para tokens
- ✅ Ejemplos de flujos completos

---

## 13. CRONOGRAMA

| Fase | Tareas | Duración |
|------|--------|----------|
| 1 | Entidades + Servicios | 2h |
| 2 | Endpoints + Controllers | 2h |
| 3 | JUnit Tests | 2h |
| 4 | Validación en Postman | 1h |
| 5 | Docker verification | 30min |
| **TOTAL** | | **7.5h** |

---

## 14. CRITERIOS DE ÉXITO

- ✅ Todos los endpoints retornan 200/201/400 correctamente
- ✅ Tests JUnit: 100% pass rate
- ✅ Postman: Flujos end-to-end válidos
- ✅ Docker: Contenedor inicia sin errores
- ✅ Neon: Todas las tablas nuevas existen y sin errores
- ✅ Auditoría: Todos los eventos se registran correctamente
- ✅ Colección Postman actualizada con ejemplos

---

## 15. RIESGOS Y MITIGACIÓN

| Riesgo | Mitigación |
|--------|-----------|
| Email delay OTP | Timeout configurable en properties |
| Token collision | UUID v4 (probabilidad negligible) |
| Supabase API fail | Circuit breaker ya existe |
| Neon performance | Índices en tablas de blacklist |

---

## APROBACIONES

- ✅ **Diseño:** Aprobado por Santiago
- ✅ **Arquitectura:** Hexagonal + Clean DDD
- ✅ **Seguridad:** Cumple 55 reglas de negocio
- ✅ **ERP Ready:** Listo para producción

