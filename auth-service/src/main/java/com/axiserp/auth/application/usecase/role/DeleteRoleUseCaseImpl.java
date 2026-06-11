package com.axiserp.auth.application.usecase.role;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.ports.input.DeleteRoleUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteRoleUseCaseImpl implements DeleteRoleUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteRoleUseCaseImpl.class);

    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    @Transactional
    public void delete(UUID id) {
        roleRepositoryPort.deleteById(id);
        log.info("role_deleted id={}", id);
    }
}
