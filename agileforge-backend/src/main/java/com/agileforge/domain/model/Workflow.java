package com.agileforge.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Workflow {

    private UUID id;
    private UUID projectId;
    private String name;
    private String ticketType;
    private boolean isDefault;
    private List<WorkflowStatus> statuses = new ArrayList<>();
    private List<WorkflowTransition> transitions = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public Workflow() {}

    public Workflow(UUID projectId, String name, String ticketType) {
        this.projectId = projectId;
        this.name = name;
        this.ticketType = ticketType;
        this.isDefault = false;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public List<WorkflowStatus> getStatuses() { return statuses; }
    public void setStatuses(List<WorkflowStatus> statuses) { this.statuses = statuses; }

    public List<WorkflowTransition> getTransitions() { return transitions; }
    public void setTransitions(List<WorkflowTransition> transitions) { this.transitions = transitions; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
