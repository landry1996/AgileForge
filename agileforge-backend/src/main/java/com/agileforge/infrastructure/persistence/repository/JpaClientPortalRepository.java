package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.ClientPortalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaClientPortalRepository extends JpaRepository<ClientPortalEntity, UUID> {

    Optional<ClientPortalEntity> findByProjectId(UUID projectId);
}
