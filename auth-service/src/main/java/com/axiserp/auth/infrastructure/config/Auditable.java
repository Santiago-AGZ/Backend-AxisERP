package com.axiserp.auth.infrastructure.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.axiserp.auth.domain.model.AuditLog.AuditAction;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    AuditAction action();

    String entityType();

    String entityIdParam() default "";
}
