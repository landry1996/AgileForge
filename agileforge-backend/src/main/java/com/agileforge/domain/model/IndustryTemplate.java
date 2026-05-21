package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class IndustryTemplate {

    private UUID id;
    private String name;
    private String description;
    private String industry;
    private String category;
    private String workflowDefinition;
    private String ticketTypes;
    private String boardColumns;
    private String defaultLabels;
    private String sampleTickets;
    private String icon;
    private int popularity;
    private boolean official;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getWorkflowDefinition() { return workflowDefinition; }
    public void setWorkflowDefinition(String workflowDefinition) { this.workflowDefinition = workflowDefinition; }
    public String getTicketTypes() { return ticketTypes; }
    public void setTicketTypes(String ticketTypes) { this.ticketTypes = ticketTypes; }
    public String getBoardColumns() { return boardColumns; }
    public void setBoardColumns(String boardColumns) { this.boardColumns = boardColumns; }
    public String getDefaultLabels() { return defaultLabels; }
    public void setDefaultLabels(String defaultLabels) { this.defaultLabels = defaultLabels; }
    public String getSampleTickets() { return sampleTickets; }
    public void setSampleTickets(String sampleTickets) { this.sampleTickets = sampleTickets; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getPopularity() { return popularity; }
    public void setPopularity(int popularity) { this.popularity = popularity; }
    public boolean isOfficial() { return official; }
    public void setOfficial(boolean official) { this.official = official; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
