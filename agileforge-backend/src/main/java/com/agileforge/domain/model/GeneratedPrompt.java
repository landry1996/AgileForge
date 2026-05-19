package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class GeneratedPrompt {

    private UUID id;
    private UUID ticketId;
    private UUID templateId;
    private String promptText;
    private UUID generatedBy;
    private Integer rating;
    private Instant createdAt;

    public GeneratedPrompt() {}

    public GeneratedPrompt(UUID ticketId, UUID templateId, String promptText, UUID generatedBy) {
        this.ticketId = ticketId;
        this.templateId = templateId;
        this.promptText = promptText;
        this.generatedBy = generatedBy;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { this.promptText = promptText; }

    public UUID getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(UUID generatedBy) { this.generatedBy = generatedBy; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
