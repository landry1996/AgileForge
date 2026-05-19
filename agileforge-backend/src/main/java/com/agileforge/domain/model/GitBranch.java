package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class GitBranch {

    private UUID id;
    private UUID repositoryId;
    private UUID ticketId;
    private String branchName;
    private Instant createdAt;

    public GitBranch() {}

    public GitBranch(UUID repositoryId, UUID ticketId, String branchName) {
        this.repositoryId = repositoryId;
        this.ticketId = ticketId;
        this.branchName = branchName;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRepositoryId() { return repositoryId; }
    public void setRepositoryId(UUID repositoryId) { this.repositoryId = repositoryId; }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
