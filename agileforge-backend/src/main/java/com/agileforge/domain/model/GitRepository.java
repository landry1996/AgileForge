package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class GitRepository {

    private UUID id;
    private UUID projectId;
    private String provider;
    private String owner;
    private String repoName;
    private String defaultBranch;
    private boolean isActive;
    private Instant createdAt;

    public GitRepository() {}

    public GitRepository(UUID projectId, String owner, String repoName, String defaultBranch) {
        this.projectId = projectId;
        this.provider = "GITHUB";
        this.owner = owner;
        this.repoName = repoName;
        this.defaultBranch = defaultBranch;
        this.isActive = true;
    }

    public String getFullName() {
        return owner + "/" + repoName;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
