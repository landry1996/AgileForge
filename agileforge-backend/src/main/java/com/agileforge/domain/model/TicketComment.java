package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class TicketComment {

    private UUID id;
    private UUID ticketId;
    private UUID authorId;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    public TicketComment() {}

    public TicketComment(UUID ticketId, UUID authorId, String content) {
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
