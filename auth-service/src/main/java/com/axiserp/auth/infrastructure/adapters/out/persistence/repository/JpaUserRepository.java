package com.axiserp.auth.infrastructure.adapters.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.axiserp.auth.infrastructure.adapters.out.persistence.entity.UserEntity;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailOrName(String email, String name);

    boolean existsByEmail(String email);

    default List<UserEntity> findByFilters(String status, String search) {
        var spec = org.springframework.data.jpa.domain.Specification.<UserEntity>where(null);

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) ->
                cb.equal(cb.lower(root.get("status").as(String.class)), status.toLowerCase()));
        }
        if (search != null && !search.isBlank()) {
            var like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("email")), like)
                ));
        }
        return findAll(spec);
    }
}
