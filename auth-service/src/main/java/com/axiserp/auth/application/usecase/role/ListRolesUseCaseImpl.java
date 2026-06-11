package com.axiserp.auth.application.usecase.role;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axiserp.auth.application.dto.response.RoleResponse;
import com.axiserp.auth.ports.input.ListRolesUseCase;
import com.axiserp.auth.ports.output.RoleRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListRolesUseCaseImpl implements ListRolesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ListRolesUseCaseImpl.class);

    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> listAll() {
        log.debug("list_all_roles");
        return roleRepositoryPort.findAll().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .createdAt(role.getCreatedAt())
                        .updatedAt(role.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
