package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.AuditAlertRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAuditAlertRuleRepository extends JpaRepository<AuditAlertRuleEntity, UUID> {

    List<AuditAlertRuleEntity> findByOrganizationIdAndActiveTrue(UUID organizationId);
}
