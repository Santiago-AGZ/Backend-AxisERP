package com.axiserp.report.infrastructure.adapters.in.web.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomUserFactory.class)
public @interface WithCustomUser {
    String username() default "00000000-0000-0000-0000-000000000001";
    String role() default "ADMIN";
}

class WithCustomUserFactory implements WithSecurityContextFactory<WithCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            annotation.username(), null,
            List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role()))));
        return context;
    }
}
