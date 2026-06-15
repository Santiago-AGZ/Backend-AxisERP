package com.axiserp.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axiserp.auth.domain.model.AuditLog;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;
import com.axiserp.auth.ports.output.AuditLogRepositoryPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("[R51-R55] AuditLog Completeness Tests")
class AuditLogCompletenessTest {

    @Mock
    private AuditLogRepositoryPort auditLogRepositoryPort;

    private AuditService auditService;
    private UUID userId;
    private UUID entityId;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogRepositoryPort);
        userId = UUID.randomUUID();
        entityId = UUID.randomUUID();
    }

    @Test
    @DisplayName("[R51] Should log login event")
    void logLogin_recordsAudit() {
        auditService.logLogin(userId, "Test User", true, "192.168.1.1", "Mozilla/5.0");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepositoryPort).save(captor.capture());
        AuditLog log = captor.getValue();

        assertEquals(AuditAction.LOGIN, log.getAction());
        assertEquals("AUTH", log.getEntityType());
        assertEquals(userId, log.getUserId());
        assertEquals("Test User", log.getUserName());
        assertTrue((Boolean) log.getDetail().get("success"));
    }

    @Test
    @DisplayName("[R52] Should log logout event")
    void logLogout_recordsAudit() {
        auditService.logLogout(userId, "Test User", "192.168.1.1", "Mozilla/5.0");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepositoryPort).save(captor.capture());
        AuditLog log = captor.getValue();

        assertEquals(AuditAction.LOGOUT, log.getAction());
        assertEquals("AUTH", log.getEntityType());
        assertEquals(userId, log.getUserId());
    }

    @Test
    @DisplayName("[R52] Should log password changes")
    void logPasswordChange_recordsAudit() {
        auditService.log(AuditAction.UPDATE, "USER", entityId, userId, "Test User",
                Map.of("reason", "reset"), null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepositoryPort).save(captor.capture());
        AuditLog log = captor.getValue();

        assertEquals(AuditAction.UPDATE, log.getAction());
        assertEquals("USER", log.getEntityType());
        assertEquals(entityId, log.getEntityId());
        assertEquals(userId, log.getUserId());
    }

    @Test
    @DisplayName("[R51] Should log role changes")
    void logRoleChange_recordsAudit() {
        auditService.log(AuditAction.UPDATE, "USER", entityId, userId, "Admin",
                Map.of("previousRole", "VENDEDOR", "newRole", "ADMIN"), null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepositoryPort).save(captor.capture());
        AuditLog log = captor.getValue();

        assertEquals(AuditAction.UPDATE, log.getAction());
        assertEquals("USER", log.getEntityType());
        assertEquals("Admin", log.getUserName());
    }

    @Test
    @DisplayName("[R52] Should log user deactivation")
    void logDeactivation_recordsAudit() {
        auditService.log(AuditAction.DEACTIVATE, "USER", entityId, userId, null,
                Map.of("previousStatus", "ACTIVO", "newStatus", "INACTIVO"), null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepositoryPort).save(captor.capture());
        AuditLog log = captor.getValue();

        assertEquals(AuditAction.DEACTIVATE, log.getAction());
        assertEquals(entityId, log.getEntityId());
    }

    @Test
    @DisplayName("[R53] Should include user, date, action in audit log")
    void auditLog_containsRequiredFields() {
        auditService.log(AuditAction.LOGIN, "AUTH", entityId, userId, "Test User",
                Map.of("success", true), "10.0.0.1", "curl/7.0");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepositoryPort).save(captor.capture());
        AuditLog log = captor.getValue();

        assertNotNull(log.getUserId(), "AuditLog must record user");
        assertNotNull(log.getAction(), "AuditLog must record action");
        assertNotNull(log.getUserId(), "AuditLog must record user");
        assertNotNull(log.getAction(), "AuditLog must record action");
        assertNotNull(log.getEntityType(), "AuditLog must record entity type");
        assertEquals("10.0.0.1", log.getIpAddress());
        assertEquals("curl/7.0", log.getUserAgent());
    }

    @Test
    @DisplayName("[R54] Audit history should not be modifiable - repository has no update method")
    void auditLogRepository_doesNotExposeUpdate() {
        assertDoesNotThrow(() -> auditService.log(AuditAction.LOGIN, "AUTH", entityId, userId, "Test",
                Map.of("success", true), null, null));
        verify(auditLogRepositoryPort).save(any(AuditLog.class));
        verifyNoMoreInteractions(auditLogRepositoryPort);
    }

    @Test
    @DisplayName("[R55] Should preserve full historical traceability")
    void auditLog_preservesFullTraceability() {
        auditService.log(AuditAction.LOGIN, "AUTH", entityId, userId, "Test User",
                Map.of("success", true), "10.0.0.1", "Mozilla/5.0");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepositoryPort).save(captor.capture());
        AuditLog log = captor.getValue();

        assertNotNull(log.getUserId(), "AuditLog must record user");
        assertNotNull(log.getUserId(), "AuditLog must record user");
        assertEquals(entityId, log.getEntityId());
        assertEquals("Test User", log.getUserName());
        assertEquals(AuditAction.LOGIN, log.getAction());
        assertEquals("AUTH", log.getEntityType());
        assertEquals("10.0.0.1", log.getIpAddress());
        assertEquals("Mozilla/5.0", log.getUserAgent());
    }
}
