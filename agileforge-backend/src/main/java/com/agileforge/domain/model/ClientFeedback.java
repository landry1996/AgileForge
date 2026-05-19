package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ClientFeedback {

    private UUID id;
    private UUID portalId;
    private UUID ticketId;
    private UUID clientUserId;
    private FeedbackType type;
    private String content;
    private Integer rating;
    private Instant createdAt;

    public ClientFeedback() {}

    public ClientFeedback(UUID portalId, UUID ticketId, UUID clientUserId,
                          FeedbackType type, String content, Integer rating) {
        this.portalId = portalId;
        this.ticketId = ticketId;
        this.clientUserId = clientUserId;
        this.type = type;
        this.content = content;
        this.rating = rating;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortalId() { return portalId; }
    public void setPortalId(UUID portalId) { this.portalId = portalId; }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public UUID getClientUserId() { return clientUserId; }
    public void setClientUserId(UUID clientUserId) { this.clientUserId = clientUserId; }

    public FeedbackType getType() { return type; }
    public void setType(FeedbackType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
