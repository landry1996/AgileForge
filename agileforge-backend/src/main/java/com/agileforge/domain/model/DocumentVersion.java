package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class DocumentVersion {

    private UUID id;
    private UUID documentId;
    private String title;
    private String content;
    private int version;
    private UUID editedBy;
    private String changeSummary;
    private Instant createdAt;

    public DocumentVersion() {}

    public DocumentVersion(UUID documentId, String title, String content, int version,
                           UUID editedBy, String changeSummary) {
        this.documentId = documentId;
        this.title = title;
        this.content = content;
        this.version = version;
        this.editedBy = editedBy;
        this.changeSummary = changeSummary;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public UUID getEditedBy() { return editedBy; }
    public void setEditedBy(UUID editedBy) { this.editedBy = editedBy; }

    public String getChangeSummary() { return changeSummary; }
    public void setChangeSummary(String changeSummary) { this.changeSummary = changeSummary; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
