package com.agileforge.domain.model;

import java.util.UUID;

public class WorkflowTransition {

    private UUID id;
    private UUID workflowId;
    private String fromStatus;
    private String toStatus;

    public WorkflowTransition() {}

    public WorkflowTransition(UUID workflowId, String fromStatus, String toStatus) {
        this.workflowId = workflowId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }

    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }

    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }
}
