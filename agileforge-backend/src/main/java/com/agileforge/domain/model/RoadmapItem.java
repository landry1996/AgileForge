package com.agileforge.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class RoadmapItem {

    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private String category;
    private RoadmapItemStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String color;
    private int position;
    private UUID releaseId;
    private UUID epicId;
    private Instant createdAt;

    public RoadmapItem() {}

    public RoadmapItem(UUID projectId, String title, String description, String category,
                       LocalDate startDate, LocalDate endDate, String color, int position) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = RoadmapItemStatus.PLANNED;
        this.startDate = startDate;
        this.endDate = endDate;
        this.color = color;
        this.position = position;
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

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public RoadmapItemStatus getStatus() { return status; }
    public void setStatus(RoadmapItemStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public UUID getReleaseId() { return releaseId; }
    public void setReleaseId(UUID releaseId) { this.releaseId = releaseId; }

    public UUID getEpicId() { return epicId; }
    public void setEpicId(UUID epicId) { this.epicId = epicId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
