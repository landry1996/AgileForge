package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class AuditAlertRule {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String actionPattern;
    private AuditSeverity severity;
    private String notifyEmails;
    private boolean isActive;
    private Instant createdAt;

    public AuditAlertRule() {}

    public AuditAlertRule(UUID organizationId, String name, String actionPattern,
                          AuditSeverity severity, String notifyEmails) {
        this.organizationId = organizationId;
        this.name = name;
        this.actionPattern = actionPattern;
        this.severity = severity;
        this.notifyEmails = notifyEmails;
        this.isActive = true;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getActionPattern() { return actionPattern; }
    public void setActionPattern(String actionPattern) { this.actionPattern = actionPattern; }

    public AuditSeverity getSeverity() { return severity; }
    public void setSeverity(AuditSeverity severity) { this.severity = severity; }

    public String getNotifyEmails() { return notifyEmails; }
    public void setNotifyEmails(String notifyEmails) { this.notifyEmails = notifyEmails; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
