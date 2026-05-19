package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Document {

    private UUID id;
    private UUID projectId;
    private UUID parentId;
    private String title;
    private String content;
    private DocumentType docType;
    private DocumentStatus status;
    private int position;
    private UUID authorId;
    private UUID lastEditedBy;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;

    public Document() {}

    public Document(UUID projectId, String title, String content, DocumentType docType,
                    DocumentStatus status, UUID parentId, UUID authorId) {
        this.projectId = projectId;
        this.title = title;
        this.content = content;
        this.docType = docType != null ? docType : DocumentType.PAGE;
        this.status = status != null ? status : DocumentStatus.DRAFT;
        this.parentId = parentId;
        this.authorId = authorId;
        this.lastEditedBy = authorId;
        this.version = 1;
        this.position = 0;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public DocumentType getDocType() { return docType; }
    public void setDocType(DocumentType docType) { this.docType = docType; }

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public UUID getLastEditedBy() { return lastEditedBy; }
    public void setLastEditedBy(UUID lastEditedBy) { this.lastEditedBy = lastEditedBy; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
