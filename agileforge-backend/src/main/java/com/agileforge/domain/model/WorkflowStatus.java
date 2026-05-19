package com.agileforge.domain.model;

import java.util.UUID;

public class WorkflowStatus {

    private UUID id;
    private UUID workflowId;
    private String name;
    private StatusCategory category;
    private int position;
    private String color;

    public WorkflowStatus() {}

    public WorkflowStatus(UUID workflowId, String name, StatusCategory category, int position, String color) {
        this.workflowId = workflowId;
        this.name = name;
        this.category = category;
        this.position = position;
        this.color = color;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StatusCategory getCategory() { return category; }
    public void setCategory(StatusCategory category) { this.category = category; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
