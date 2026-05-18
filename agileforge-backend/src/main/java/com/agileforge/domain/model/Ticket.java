package com.agileforge.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Ticket {

    private UUID id;
    private UUID projectId;
    private String key;
    private long number;
    private String title;
    private String description;
    private TicketType type;
    private TicketStatus status;
    private TicketPriority priority;
    private UUID assigneeId;
    private UUID reporterId;
    private UUID epicId;
    private UUID parentId;
    private UUID sprintId;
    private Integer storyPoints;
    private Double estimatedHours;
    private Double loggedHours;
    private LocalDate dueDate;
    private String environment;
    private String component;
    private String labels;
    private String affectedVersion;
    private String fixVersion;
    private int qualityScore;
    private Instant createdAt;
    private Instant updatedAt;

    public Ticket() {}

    public Ticket(UUID projectId, String key, long number, String title, TicketType type,
                  TicketPriority priority, UUID reporterId) {
        this.projectId = projectId;
        this.key = key;
        this.number = number;
        this.title = title;
        this.type = type;
        this.status = TicketStatus.BACKLOG;
        this.priority = priority;
        this.reporterId = reporterId;
        this.qualityScore = 0;
    }

    public String getFullKey() {
        return key + "-" + number;
    }

    public boolean isBlocked() {
        return status == TicketStatus.BLOCKED;
    }

    public boolean isDone() {
        return status == TicketStatus.DONE || status == TicketStatus.CANCELLED;
    }

    public int calculateQualityScore() {
        int score = 0;
        if (title != null && title.length() >= 10) score += 15;
        if (description != null && description.length() >= 30) score += 20;
        if (storyPoints != null || estimatedHours != null) score += 10;
        if (assigneeId != null) score += 5;
        if (priority != null) score += 5;
        if (labels != null && !labels.isEmpty()) score += 5;
        if (component != null && !component.isEmpty()) score += 5;
        // Acceptance criteria and tests would add more but require separate entities
        this.qualityScore = score;
        return score;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public long getNumber() { return number; }
    public void setNumber(long number) { this.number = number; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketType getType() { return type; }
    public void setType(TicketType type) { this.type = type; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public UUID getAssigneeId() { return assigneeId; }
    public void setAssigneeId(UUID assigneeId) { this.assigneeId = assigneeId; }

    public UUID getReporterId() { return reporterId; }
    public void setReporterId(UUID reporterId) { this.reporterId = reporterId; }

    public UUID getEpicId() { return epicId; }
    public void setEpicId(UUID epicId) { this.epicId = epicId; }

    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }

    public UUID getSprintId() { return sprintId; }
    public void setSprintId(UUID sprintId) { this.sprintId = sprintId; }

    public Integer getStoryPoints() { return storyPoints; }
    public void setStoryPoints(Integer storyPoints) { this.storyPoints = storyPoints; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Double getLoggedHours() { return loggedHours; }
    public void setLoggedHours(Double loggedHours) { this.loggedHours = loggedHours; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getLabels() { return labels; }
    public void setLabels(String labels) { this.labels = labels; }

    public String getAffectedVersion() { return affectedVersion; }
    public void setAffectedVersion(String affectedVersion) { this.affectedVersion = affectedVersion; }

    public String getFixVersion() { return fixVersion; }
    public void setFixVersion(String fixVersion) { this.fixVersion = fixVersion; }

    public int getQualityScore() { return qualityScore; }
    public void setQualityScore(int qualityScore) { this.qualityScore = qualityScore; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
