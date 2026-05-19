package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.AuditAction;
import com.agileforge.domain.model.AuditAlertRule;
import com.agileforge.domain.model.AuditEvent;
import com.agileforge.domain.model.AuditSeverity;
import com.agileforge.domain.port.out.AuditEventRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.AuditAlertRuleEntity;
import com.agileforge.infrastructure.persistence.entity.AuditEventEntity;
import com.agileforge.infrastructure.persistence.repository.JpaAuditAlertRuleRepository;
import com.agileforge.infrastructure.persistence.repository.JpaAuditEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AuditEventRepositoryAdapter implements AuditEventRepositoryPort {

    private final JpaAuditEventRepository eventRepository;
    private final JpaAuditAlertRuleRepository alertRuleRepository;

    public AuditEventRepositoryAdapter(JpaAuditEventRepository eventRepository,
                                       JpaAuditAlertRuleRepository alertRuleRepository) {
        this.eventRepository = eventRepository;
        this.alertRuleRepository = alertRuleRepository;
    }

    @Override
    public AuditEvent save(AuditEvent auditEvent) {
        AuditEventEntity entity = toEntity(auditEvent);
        AuditEventEntity saved = eventRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AuditEvent> findByOrganizationId(UUID organizationId, int page, int size) {
        return eventRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, PageRequest.of(page, size))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEvent> findByUserId(UUID userId, int page, int size) {
        return eventRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEvent> findByEntityTypeAndEntityId(String entityType, UUID entityId) {
        return eventRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEvent> findByAction(AuditAction action, int page, int size) {
        return eventRepository.findByActionOrderByCreatedAtDesc(action.name(), PageRequest.of(page, size))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEvent> findBySeverity(AuditSeverity severity) {
        return eventRepository.findBySeverity(severity.name())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countByOrganizationId(UUID organizationId) {
        return eventRepository.countByOrganizationId(organizationId);
    }

    @Override
    public List<AuditAlertRule> findAlertRulesByOrganizationId(UUID organizationId) {
        return alertRuleRepository.findByOrganizationIdAndActiveTrue(organizationId)
                .stream().map(this::alertRuleToDomain).toList();
    }

    @Override
    public AuditAlertRule saveAlertRule(AuditAlertRule alertRule) {
        AuditAlertRuleEntity entity = alertRuleToEntity(alertRule);
        AuditAlertRuleEntity saved = alertRuleRepository.save(entity);
        return alertRuleToDomain(saved);
    }

    @Override
    public void deleteAlertRule(UUID ruleId) {
        alertRuleRepository.deleteById(ruleId);
    }

    private AuditEventEntity toEntity(AuditEvent domain) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setId(domain.getId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setProjectId(domain.getProjectId());
        entity.setUserId(domain.getUserId());
        entity.setAction(domain.getAction() != null ? domain.getAction().name() : null);
        entity.setEntityType(domain.getEntityType());
        entity.setEntityId(domain.getEntityId());
        entity.setDetails(domain.getDetails());
        entity.setIpAddress(domain.getIpAddress());
        entity.setUserAgent(domain.getUserAgent());
        entity.setSeverity(domain.getSeverity() != null ? domain.getSeverity().name() : "INFO");
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private AuditEvent toDomain(AuditEventEntity entity) {
        AuditEvent domain = new AuditEvent();
        domain.setId(entity.getId());
        domain.setOrganizationId(entity.getOrganizationId());
        domain.setProjectId(entity.getProjectId());
        domain.setUserId(entity.getUserId());
        domain.setAction(entity.getAction() != null ? AuditAction.valueOf(entity.getAction()) : null);
        domain.setEntityType(entity.getEntityType());
        domain.setEntityId(entity.getEntityId());
        domain.setDetails(entity.getDetails());
        domain.setIpAddress(entity.getIpAddress());
        domain.setUserAgent(entity.getUserAgent());
        domain.setSeverity(entity.getSeverity() != null ? AuditSeverity.valueOf(entity.getSeverity()) : AuditSeverity.INFO);
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }

    private AuditAlertRuleEntity alertRuleToEntity(AuditAlertRule domain) {
        AuditAlertRuleEntity entity = new AuditAlertRuleEntity();
        entity.setId(domain.getId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setName(domain.getName());
        entity.setActionPattern(domain.getActionPattern());
        entity.setSeverity(domain.getSeverity() != null ? domain.getSeverity().name() : "WARNING");
        entity.setNotifyEmails(domain.getNotifyEmails());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private AuditAlertRule alertRuleToDomain(AuditAlertRuleEntity entity) {
        AuditAlertRule domain = new AuditAlertRule();
        domain.setId(entity.getId());
        domain.setOrganizationId(entity.getOrganizationId());
        domain.setName(entity.getName());
        domain.setActionPattern(entity.getActionPattern());
        domain.setSeverity(entity.getSeverity() != null ? AuditSeverity.valueOf(entity.getSeverity()) : AuditSeverity.WARNING);
        domain.setNotifyEmails(entity.getNotifyEmails());
        domain.setActive(entity.isActive());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
