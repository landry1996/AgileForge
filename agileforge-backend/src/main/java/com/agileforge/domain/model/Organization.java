package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Organization {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String website;
    private String plan;
    private int maxUsers;
    private int maxProjects;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public Organization() {}

    public Organization(String name, String slug, String description) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.plan = "FREE";
        this.maxUsers = 5;
        this.maxProjects = 1;
        this.active = true;
    }

    public boolean canAddUser(int currentUserCount) {
        return currentUserCount < maxUsers;
    }

    public boolean canAddProject(int currentProjectCount) {
        return currentProjectCount < maxProjects;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }

    public int getMaxProjects() { return maxProjects; }
    public void setMaxProjects(int maxProjects) { this.maxProjects = maxProjects; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
