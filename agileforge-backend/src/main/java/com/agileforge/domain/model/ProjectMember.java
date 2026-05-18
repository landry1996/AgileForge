package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ProjectMember {

    private UUID id;
    private UUID projectId;
    private UUID userId;
    private UUID roleId;
    private String roleCode;
    private Instant joinedAt;
    private boolean active;

    public ProjectMember() {}

    public ProjectMember(UUID projectId, UUID userId, UUID roleId) {
        this.projectId = projectId;
        this.userId = userId;
        this.roleId = roleId;
        this.joinedAt = Instant.now();
        this.active = true;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
