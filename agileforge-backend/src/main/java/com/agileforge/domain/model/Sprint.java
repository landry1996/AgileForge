package com.agileforge.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Sprint {

    private UUID id;
    private UUID projectId;
    private String name;
    private String goal;
    private SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer capacity;
    private Instant createdAt;
    private Instant updatedAt;

    public Sprint() {}

    public Sprint(UUID projectId, String name, String goal, LocalDate startDate, LocalDate endDate) {
        this.projectId = projectId;
        this.name = name;
        this.goal = goal;
        this.status = SprintStatus.PLANNING;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public enum SprintStatus {
        PLANNING,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    public boolean isActive() {
        return status == SprintStatus.ACTIVE;
    }

    public boolean canStart() {
        return status == SprintStatus.PLANNING;
    }

    public boolean canComplete() {
        return status == SprintStatus.ACTIVE;
    }

    public long getDurationDays() {
        if (startDate == null || endDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public SprintStatus getStatus() { return status; }
    public void setStatus(SprintStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
