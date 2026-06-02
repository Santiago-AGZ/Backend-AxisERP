# Auth Service — Reglas Pendientes

## R46: Operaciones críticas requieren reautenticación
**Estado:** NO IMPLEMENTADO
**Descripción:** Antes de ejecutar operaciones destructivas (eliminar usuario, cambiar rol ADMIN),
el sistema debe solicitar reautenticación (contraseña o segundo factor).
**Razón:** Complejidad de implementación — requiere flujo de reautenticación con OTP o contraseña.
**Plan:** Implementar como endpoint `POST /auth/reauth` que reciba credenciales y devuelva
un token temporal (5 min) requerido para operaciones críticas.

## R50: Registrar actividad sospechosa
**Estado:** PARCIAL
**Implementado:** audit_log registra LOGIN, LOGOUT, CREATE, UPDATE, DEACTIVATE.
**Pendiente:** Detección de patrones sospechosos (múltiples intentos fallidos desde misma IP,
cambios masivos de roles, accesos desde ubicaciones inusuales).
**Razón:** Requiere motor de reglas o análisis de patrones que excede el alcance actual.
**Actual:** Los intentos fallidos de login son manejados por Supabase Auth (R45 delegado).
