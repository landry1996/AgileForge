package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class IncidentEvent {

    private UUID id;
    private UUID incidentId;
    private UUID userId;
    private IncidentEventType eventType;
    private String message;
    private Instant createdAt;

    public IncidentEvent() {}

    public IncidentEvent(UUID incidentId, UUID userId, IncidentEventType eventType, String message) {
        this.incidentId = incidentId;
        this.userId = userId;
        this.eventType = eventType;
        this.message = message;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getIncidentId() { return incidentId; }
    public void setIncidentId(UUID incidentId) { this.incidentId = incidentId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public IncidentEventType getEventType() { return eventType; }
    public void setEventType(IncidentEventType eventType) { this.eventType = eventType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
