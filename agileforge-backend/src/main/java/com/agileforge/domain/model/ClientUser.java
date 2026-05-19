package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ClientUser {

    private UUID id;
    private UUID portalId;
    private String email;
    private String name;
    private String company;
    private boolean isActive;
    private Instant lastLoginAt;
    private Instant createdAt;

    public ClientUser() {}

    public ClientUser(UUID portalId, String email, String name, String company) {
        this.portalId = portalId;
        this.email = email;
        this.name = name;
        this.company = company;
        this.isActive = true;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortalId() { return portalId; }
    public void setPortalId(UUID portalId) { this.portalId = portalId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
