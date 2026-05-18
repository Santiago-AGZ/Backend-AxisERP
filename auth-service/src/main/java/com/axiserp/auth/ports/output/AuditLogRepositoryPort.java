package com.axiserp.auth.ports.output;

import java.util.List;
import java.util.UUID;

import com.axiserp.auth.domain.model.AuditLog;

/**
 * Port de salida para operaciones de persistencia del log de auditoría.
 */
public interface AuditLogRepositoryPort {

    /**
     * Persiste un registro de auditoría.
     *
     * @param auditLog dominio del registro a persistir
     * @return registro persistido con datos generados por BD
     */
    AuditLog save(AuditLog auditLog);

    /**
     * Busca registros de auditoría con filtros opcionales.
     *
     * @param userId     filtro por usuario (null para todos)
     * @param action     filtro por acción (null para todas)
     * @param entityType filtro por tipo de entidad (null para todos)
     * @param page       página (0-based)
     * @param size       tamaño de página
     * @return lista de registros de auditoría
     */
    List<AuditLog> findByFilters(UUID userId, String action, String entityType, int page, int size);
}
