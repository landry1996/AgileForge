package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class CapacityEntry {

    private UUID id;
    private UUID projectId;
    private UUID userId;
    private UUID sprintId;
    private double availableHours;
    private double plannedLeaveHours;
    private String notes;
    private Instant createdAt;

    public CapacityEntry() {}

    public CapacityEntry(UUID projectId, UUID userId, UUID sprintId, double availableHours,
                         double plannedLeaveHours, String notes) {
        this.projectId = projectId;
        this.userId = userId;
        this.sprintId = sprintId;
        this.availableHours = availableHours;
        this.plannedLeaveHours = plannedLeaveHours;
        this.notes = notes;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getSprintId() { return sprintId; }
    public void setSprintId(UUID sprintId) { this.sprintId = sprintId; }

    public double getAvailableHours() { return availableHours; }
    public void setAvailableHours(double availableHours) { this.availableHours = availableHours; }

    public double getPlannedLeaveHours() { return plannedLeaveHours; }
    public void setPlannedLeaveHours(double plannedLeaveHours) { this.plannedLeaveHours = plannedLeaveHours; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
