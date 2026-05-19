package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Incident {

    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private UUID commanderId;
    private Instant startedAt;
    private Instant resolvedAt;
    private String rootCause;
    private String resolution;
    private String postMortem;
    private Instant createdAt;
    private Instant updatedAt;

    public Incident() {}

    public Incident(UUID projectId, String title, String description, IncidentSeverity severity) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = IncidentStatus.DETECTED;
        this.startedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }

    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }

    public UUID getCommanderId() { return commanderId; }
    public void setCommanderId(UUID commanderId) { this.commanderId = commanderId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public String getPostMortem() { return postMortem; }
    public void setPostMortem(String postMortem) { this.postMortem = postMortem; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
