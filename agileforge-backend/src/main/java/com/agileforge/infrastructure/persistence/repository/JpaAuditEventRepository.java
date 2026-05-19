package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    List<AuditEventEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    List<AuditEventEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<AuditEventEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);

    List<AuditEventEntity> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    List<AuditEventEntity> findBySeverity(String severity);

    long countByOrganizationId(UUID organizationId);
}
