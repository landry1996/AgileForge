package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class WebhookSubscription {

    private UUID id;
    private UUID projectId;
    private String url;
    private String secret;
    private String events;
    private boolean isActive;
    private Instant lastTriggeredAt;
    private int failureCount;
    private Instant createdAt;

    public WebhookSubscription() {}

    public WebhookSubscription(UUID projectId, String url, String secret, String events) {
        this.projectId = projectId;
        this.url = url;
        this.secret = secret;
        this.events = events;
        this.isActive = true;
        this.failureCount = 0;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public String getEvents() { return events; }
    public void setEvents(String events) { this.events = events; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(Instant lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }

    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
