package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Label {

    private UUID id;
    private UUID projectId;
    private String name;
    private String color;
    private String description;
    private Instant createdAt;

    public Label() {}

    public Label(UUID projectId, String name, String color, String description) {
        this.projectId = projectId;
        this.name = name;
        this.color = color != null ? color : "#6B7280";
        this.description = description;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
