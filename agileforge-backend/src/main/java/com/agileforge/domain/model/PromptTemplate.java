package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PromptTemplate {

    private UUID id;
    private UUID projectId;
    private String name;
    private PromptCategory category;
    private String template;
    private String variables;
    private boolean isGlobal;
    private int usageCount;
    private double rating;
    private Instant createdAt;
    private Instant updatedAt;

    public PromptTemplate() {}

    public PromptTemplate(UUID projectId, String name, PromptCategory category, String template, String variables) {
        this.projectId = projectId;
        this.name = name;
        this.category = category;
        this.template = template;
        this.variables = variables;
        this.isGlobal = false;
        this.usageCount = 0;
        this.rating = 0;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PromptCategory getCategory() { return category; }
    public void setCategory(PromptCategory category) { this.category = category; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }

    public boolean isGlobal() { return isGlobal; }
    public void setGlobal(boolean global) { isGlobal = global; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
