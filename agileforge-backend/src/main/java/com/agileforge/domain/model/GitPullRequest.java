package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class GitPullRequest {

    private UUID id;
    private UUID repositoryId;
    private UUID ticketId;
    private int prNumber;
    private String title;
    private PRStatus status;
    private String author;
    private String sourceBranch;
    private String targetBranch;
    private String url;
    private Instant createdAt;
    private Instant mergedAt;
    private Instant closedAt;

    public GitPullRequest() {}

    public GitPullRequest(UUID repositoryId, UUID ticketId, int prNumber, String title,
                          String author, String sourceBranch, String targetBranch, String url) {
        this.repositoryId = repositoryId;
        this.ticketId = ticketId;
        this.prNumber = prNumber;
        this.title = title;
        this.status = PRStatus.OPEN;
        this.author = author;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.url = url;
    }

    public boolean isOpen() {
        return status == PRStatus.OPEN;
    }

    public boolean isMerged() {
        return status == PRStatus.MERGED;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRepositoryId() { return repositoryId; }
    public void setRepositoryId(UUID repositoryId) { this.repositoryId = repositoryId; }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public int getPrNumber() { return prNumber; }
    public void setPrNumber(int prNumber) { this.prNumber = prNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public PRStatus getStatus() { return status; }
    public void setStatus(PRStatus status) { this.status = status; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSourceBranch() { return sourceBranch; }
    public void setSourceBranch(String sourceBranch) { this.sourceBranch = sourceBranch; }

    public String getTargetBranch() { return targetBranch; }
    public void setTargetBranch(String targetBranch) { this.targetBranch = targetBranch; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getMergedAt() { return mergedAt; }
    public void setMergedAt(Instant mergedAt) { this.mergedAt = mergedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}
