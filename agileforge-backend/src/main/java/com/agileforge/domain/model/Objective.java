package com.agileforge.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Objective {

    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private ObjectiveStatus status;
    private int progress;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private List<KeyResult> keyResults = new ArrayList<>();

    public Objective() {}

    public Objective(UUID projectId, String title, String description, String period,
                     LocalDate startDate, LocalDate endDate) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ObjectiveStatus.ACTIVE;
        this.progress = 0;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public ObjectiveStatus getStatus() { return status; }
    public void setStatus(ObjectiveStatus status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<KeyResult> getKeyResults() { return keyResults; }
    public void setKeyResults(List<KeyResult> keyResults) { this.keyResults = keyResults; }
}
