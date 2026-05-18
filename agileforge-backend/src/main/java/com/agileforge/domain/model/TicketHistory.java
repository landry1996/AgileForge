package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class TicketHistory {

    private UUID id;
    private UUID ticketId;
    private UUID userId;
    private String field;
    private String oldValue;
    private String newValue;
    private Instant createdAt;

    public TicketHistory() {}

    public TicketHistory(UUID ticketId, UUID userId, String field, String oldValue, String newValue) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
