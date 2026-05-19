package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class KnowledgeEntry {

    private UUID id;
    private UUID projectId;
    private KnowledgeCategory category;
    private String title;
    private String content;
    private String tags;
    private boolean isActive;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public KnowledgeEntry() {}

    public KnowledgeEntry(UUID projectId, KnowledgeCategory category, String title, String content, String tags, UUID createdBy) {
        this.projectId = projectId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.isActive = true;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public KnowledgeCategory getCategory() { return category; }
    public void setCategory(KnowledgeCategory category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
