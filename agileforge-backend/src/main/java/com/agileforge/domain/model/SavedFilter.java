package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SavedFilter {

    private UUID id;
    private UUID projectId;
    private UUID userId;
    private String name;
    private String filterConfig;
    private boolean isShared;
    private Instant createdAt;
    private Instant updatedAt;

    public SavedFilter() {}

    public SavedFilter(UUID projectId, UUID userId, String name, String filterConfig, boolean isShared) {
        this.projectId = projectId;
        this.userId = userId;
        this.name = name;
        this.filterConfig = filterConfig;
        this.isShared = isShared;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFilterConfig() { return filterConfig; }
    public void setFilterConfig(String filterConfig) { this.filterConfig = filterConfig; }

    public boolean isShared() { return isShared; }
    public void setShared(boolean shared) { this.isShared = shared; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
