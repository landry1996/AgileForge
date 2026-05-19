package com.agileforge.application.service;

import com.agileforge.application.dto.request.AuditFilterRequest;
import com.agileforge.application.dto.response.AuditSummaryResponse;
import com.agileforge.domain.model.AuditAction;
import com.agileforge.domain.model.AuditAlertRule;
import com.agileforge.domain.model.AuditEvent;
import com.agileforge.domain.model.AuditSeverity;
import com.agileforge.domain.port.out.AuditEventRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepositoryPort auditRepository;

    public AuditService(AuditEventRepositoryPort auditRepository) {
        this.auditRepository = auditRepository;
    }

    public AuditEvent log(UUID orgId, UUID projectId, UUID userId, AuditAction action,
                          String entityType, UUID entityId, String details,
                          String ipAddress, String userAgent) {
        AuditSeverity severity = determineSeverity(action);

        AuditEvent event = new AuditEvent(orgId, projectId, userId, action,
                entityType, entityId, details, ipAddress, userAgent, severity);

        AuditEvent saved = auditRepository.save(event);

        // Check alert rules
        checkAlertRules(orgId, action, severity);

        log.debug("Audit event logged: {} on {} by user {}", action, entityType, userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getByOrganization(UUID orgId, AuditFilterRequest filters) {
        if (filters.action() != null) {
            return auditRepository.findByAction(AuditAction.valueOf(filters.action()),
                    filters.getPage(), filters.getSize());
        }
        if (filters.userId() != null) {
            return auditRepository.findByUserId(filters.userId(),
                    filters.getPage(), filters.getSize());
        }
        return auditRepository.findByOrganizationId(orgId, filters.getPage(), filters.getSize());
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getByUser(UUID userId, int page, int size) {
        return auditRepository.findByUserId(userId, page, size);
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getByEntity(String entityType, UUID entityId) {
        return auditRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public AuditSummaryResponse getSummary(UUID orgId) {
        long totalEvents = auditRepository.countByOrganizationId(orgId);

        List<AuditEvent> criticalEvents = auditRepository.findBySeverity(AuditSeverity.CRITICAL);
        long criticalCount = criticalEvents.stream()
                .filter(e -> orgId.equals(e.getOrganizationId()))
                .count();

        // Today's events approximation
        Instant startOfToday = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        List<AuditEvent> recentEvents = auditRepository.findByOrganizationId(orgId, 0, 1000);
        long todayEvents = recentEvents.stream()
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isAfter(startOfToday))
                .count();

        // Top actions
        Map<String, Long> topActions = recentEvents.stream()
                .filter(e -> e.getAction() != null)
                .collect(Collectors.groupingBy(e -> e.getAction().name(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        // Top users
        Map<String, Long> topUsers = recentEvents.stream()
                .filter(e -> e.getUserId() != null)
                .collect(Collectors.groupingBy(e -> e.getUserId().toString(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        return new AuditSummaryResponse(totalEvents, criticalCount, todayEvents, topActions, topUsers);
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> exportAuditLog(UUID orgId, String fromDate, String toDate) {
        // Get all events for the organization, then filter by date range
        List<AuditEvent> allEvents = auditRepository.findByOrganizationId(orgId, 0, 10000);

        Instant from = fromDate != null ? LocalDate.parse(fromDate).atStartOfDay(ZoneOffset.UTC).toInstant() : Instant.MIN;
        Instant to = toDate != null ? LocalDate.parse(toDate).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : Instant.MAX;

        return allEvents.stream()
                .filter(e -> e.getCreatedAt() != null)
                .filter(e -> e.getCreatedAt().isAfter(from) && e.getCreatedAt().isBefore(to))
                .toList();
    }

    public AuditAlertRule createAlertRule(UUID orgId, String name, String actionPattern,
                                          String severity, String notifyEmails) {
        AuditSeverity sev = severity != null ? AuditSeverity.valueOf(severity) : AuditSeverity.WARNING;
        AuditAlertRule rule = new AuditAlertRule(orgId, name, actionPattern, sev, notifyEmails);
        AuditAlertRule saved = auditRepository.saveAlertRule(rule);
        log.info("Audit alert rule created: {} for org {}", name, orgId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AuditAlertRule> getAlertRules(UUID orgId) {
        return auditRepository.findAlertRulesByOrganizationId(orgId);
    }

    public void deleteAlertRule(UUID ruleId) {
        auditRepository.deleteAlertRule(ruleId);
        log.info("Audit alert rule deleted: {}", ruleId);
    }

    private AuditSeverity determineSeverity(AuditAction action) {
        return switch (action) {
            case DELETE, BULK_DELETE, PERMISSION_CHANGE, ROLE_CHANGE -> AuditSeverity.WARNING;
            case ACCESS_DENIED -> AuditSeverity.CRITICAL;
            default -> AuditSeverity.INFO;
        };
    }

    private void checkAlertRules(UUID orgId, AuditAction action, AuditSeverity severity) {
        if (orgId == null) return;

        List<AuditAlertRule> rules = auditRepository.findAlertRulesByOrganizationId(orgId);
        for (AuditAlertRule rule : rules) {
            if (matchesPattern(action.name(), rule.getActionPattern())) {
                log.warn("Audit alert triggered: '{}' for action {} in org {}",
                        rule.getName(), action, orgId);
                // In a full implementation, send email notifications here
            }
        }
    }

    private boolean matchesPattern(String actionName, String pattern) {
        if (pattern == null || pattern.isBlank()) return false;
        if (pattern.equals("*")) return true;
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*");
            return actionName.matches(regex);
        }
        return actionName.equalsIgnoreCase(pattern);
    }
}
