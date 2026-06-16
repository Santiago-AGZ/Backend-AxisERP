package com.axiserp.auth.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.domain.factory.UserFactory;
import com.axiserp.auth.domain.model.User;
import com.axiserp.auth.domain.service.PasswordValidator;
import com.axiserp.auth.ports.input.ResetPasswordUseCase;
import com.axiserp.auth.ports.output.SupabaseAuthPort;
import com.axiserp.auth.ports.output.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordUseCaseImpl.class);

    private final SupabaseAuthPort supabaseAuthPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordValidator.validate(newPassword);
        String email = supabaseAuthPort.resetPassword(token, newPassword);

        userRepositoryPort.findByEmail(email).ifPresentOrElse(user -> {
            User updated = UserFactory.withNewPassword(user, passwordEncoder.encode(newPassword));
            userRepositoryPort.save(updated);
            log.info("local_password_hash_updated email={}", email);
        }, () -> log.warn("user_not_found_locally email={}", email));

        log.info("password_reset_complete email={}", email);
    }
}
