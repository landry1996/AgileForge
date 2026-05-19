package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class AuditEvent {

    private UUID id;
    private UUID organizationId;
    private UUID projectId;
    private UUID userId;
    private AuditAction action;
    private String entityType;
    private UUID entityId;
    private String details;
    private String ipAddress;
    private String userAgent;
    private AuditSeverity severity;
    private Instant createdAt;

    public AuditEvent() {}

    public AuditEvent(UUID organizationId, UUID projectId, UUID userId, AuditAction action,
                      String entityType, UUID entityId, String details, String ipAddress,
                      String userAgent, AuditSeverity severity) {
        this.organizationId = organizationId;
        this.projectId = projectId;
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.severity = severity != null ? severity : AuditSeverity.INFO;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public AuditSeverity getSeverity() { return severity; }
    public void setSeverity(AuditSeverity severity) { this.severity = severity; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
