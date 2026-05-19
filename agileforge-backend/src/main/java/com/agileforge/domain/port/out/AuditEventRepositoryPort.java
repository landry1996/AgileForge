package com.agileforge.domain.port.out;

import com.agileforge.domain.model.AuditAction;
import com.agileforge.domain.model.AuditAlertRule;
import com.agileforge.domain.model.AuditEvent;
import com.agileforge.domain.model.AuditSeverity;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepositoryPort {

    AuditEvent save(AuditEvent auditEvent);

    List<AuditEvent> findByOrganizationId(UUID organizationId, int page, int size);

    List<AuditEvent> findByUserId(UUID userId, int page, int size);

    List<AuditEvent> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    List<AuditEvent> findByAction(AuditAction action, int page, int size);

    List<AuditEvent> findBySeverity(AuditSeverity severity);

    long countByOrganizationId(UUID organizationId);

    List<AuditAlertRule> findAlertRulesByOrganizationId(UUID organizationId);

    AuditAlertRule saveAlertRule(AuditAlertRule alertRule);

    void deleteAlertRule(UUID ruleId);
}
