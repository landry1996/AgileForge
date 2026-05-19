package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class TicketLink {

    private UUID id;
    private UUID sourceTicketId;
    private UUID targetTicketId;
    private TicketLinkType linkType;
    private Instant createdAt;
    private String createdBy;

    public TicketLink() {}

    public TicketLink(UUID sourceTicketId, UUID targetTicketId, TicketLinkType linkType, String createdBy) {
        this.sourceTicketId = sourceTicketId;
        this.targetTicketId = targetTicketId;
        this.linkType = linkType;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSourceTicketId() { return sourceTicketId; }
    public void setSourceTicketId(UUID sourceTicketId) { this.sourceTicketId = sourceTicketId; }

    public UUID getTargetTicketId() { return targetTicketId; }
    public void setTargetTicketId(UUID targetTicketId) { this.targetTicketId = targetTicketId; }

    public TicketLinkType getLinkType() { return linkType; }
    public void setLinkType(TicketLinkType linkType) { this.linkType = linkType; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
