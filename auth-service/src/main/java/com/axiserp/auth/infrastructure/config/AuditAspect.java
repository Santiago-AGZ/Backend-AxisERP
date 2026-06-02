package com.axiserp.auth.infrastructure.config;

import java.util.Map;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.axiserp.auth.application.service.AuditService;
import com.axiserp.auth.domain.model.AuditLog.AuditAction;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        UUID entityId = null;
        if (!auditable.entityIdParam().isEmpty() && paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (auditable.entityIdParam().equals(paramNames[i])) {
                    if (args[i] instanceof UUID) {
                        entityId = (UUID) args[i];
                    }
                    break;
                }
            }
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = null;
        String userName = null;
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String) {
            try {
                userId = UUID.fromString((String) auth.getPrincipal());
            } catch (IllegalArgumentException e) {
                log.debug("audit_cannot_parse_user_id principal={}", auth.getPrincipal());
            }
            userName = auth.getName();
        }

        try {
            Object result = joinPoint.proceed();

            if (result != null && entityId == null) {
                try {
                    var getIdMethod = result.getClass().getMethod("getId");
                    Object idObj = getIdMethod.invoke(result);
                    if (idObj instanceof UUID) {
                        entityId = (UUID) idObj;
                    }
                } catch (NoSuchMethodException e) {
                    log.debug("audit_no_getId_method result_type={}", result.getClass().getSimpleName());
                }
            }

            if (entityId != null) {
                auditService.log(auditable.action(), auditable.entityType(), entityId,
                        userId, userName, Map.of(), null, null);
            }

            return result;
        } catch (Throwable ex) {
            log.warn("audit_method_failed action={} error={}", auditable.action(), ex.getMessage());
            throw ex;
        }
    }
}
