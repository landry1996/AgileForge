package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Invitation {

    private UUID id;
    private UUID organizationId;
    private String email;
    private String role;
    private String token;
    private InvitationStatus status;
    private UUID invitedBy;
    private Instant expiresAt;
    private Instant acceptedAt;
    private Instant createdAt;

    public Invitation() {}

    public Invitation(UUID organizationId, String email, String role, String token, UUID invitedBy, Instant expiresAt) {
        this.organizationId = organizationId;
        this.email = email;
        this.role = role;
        this.token = token;
        this.status = InvitationStatus.PENDING;
        this.invitedBy = invitedBy;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }

    public UUID getInvitedBy() { return invitedBy; }
    public void setInvitedBy(UUID invitedBy) { this.invitedBy = invitedBy; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Instant acceptedAt) { this.acceptedAt = acceptedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
