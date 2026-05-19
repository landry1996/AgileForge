package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Attachment {

    private UUID id;
    private UUID ticketId;
    private String fileName;
    private long fileSize;
    private String contentType;
    private String storagePath;
    private UUID uploadedBy;
    private Instant createdAt;

    public Attachment() {}

    public Attachment(UUID ticketId, String fileName, long fileSize, String contentType,
                      String storagePath, UUID uploadedBy) {
        this.ticketId = ticketId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.storagePath = storagePath;
        this.uploadedBy = uploadedBy;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public UUID getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UUID uploadedBy) { this.uploadedBy = uploadedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
