package com.axiserp.auth.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.domain.service.PasswordValidator;
import com.axiserp.auth.ports.input.ResetPasswordUseCase;
import com.axiserp.auth.ports.output.SupabaseAuthPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordUseCaseImpl.class);

    private final SupabaseAuthPort supabaseAuthPort;

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordValidator.validate(newPassword);
        supabaseAuthPort.resetPassword(token, newPassword);
        log.info("password_reset_complete");
    }
}

