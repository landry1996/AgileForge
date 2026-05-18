package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaRoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByCode(String code);
}
