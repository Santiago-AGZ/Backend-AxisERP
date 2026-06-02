package com.axiserp.auth.application.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.axiserp.auth.domain.model.AuditLog;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.ports.output.AuditLogRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación para registrar eventos de auditoría.
 * Centraliza el logging de acciones críticas del sistema (HU-032).
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    /**
     * Registra un evento de auditoría genérico.
     *
     * @param action tipo de acción realizada
     * @param entityType tipo de entidad afectada
     * @param entityId identificador de la entidad
     * @param userId usuario que realizó la acción
     * @param userName nombre del usuario
     * @param detail datos adicionales en formato JSON
     * @param ipAddress IP del cliente
     * @param userAgent agente del navegador
     */
    public void log(AuditAction action, String entityType, UUID entityId,
                    UUID userId, String userName, Map<String, Object> detail,
                    String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .userName(userName)
                .detail(detail)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        auditLogRepositoryPort.save(auditLog);
        log.info("audit_log action={} entity_type={} user_id={}", action, entityType, userId);
    }

    /**
     * Registra un intento de inicio de sesión.
     *
     * @param userId identificador del usuario
     * @param userName nombre del usuario
     * @param success si el login fue exitoso
     * @param ipAddress IP del cliente
     * @param userAgent agente del navegador
     */
    public void logLogin(UUID userId, String userName, boolean success,
                         String ipAddress, String userAgent) {
        log(AuditAction.LOGIN, "AUTH", userId, userId, userName,
            Map.of("success", success), ipAddress, userAgent);
    }

    /**
     * Registra un cierre de sesión.
     *
     * @param userId identificador del usuario
     * @param userName nombre del usuario
     * @param ipAddress IP del cliente
     * @param userAgent agente del navegador
     */
    public void logLogout(UUID userId, String userName,
                          String ipAddress, String userAgent) {
        log(AuditAction.LOGOUT, "AUTH", userId,
            userId, userName, Map.of(), ipAddress, userAgent);
    }
}
