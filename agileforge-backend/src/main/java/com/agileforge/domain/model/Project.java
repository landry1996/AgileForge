package com.agileforge.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Project {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String key;
    private String description;
    private String logoUrl;
    private ProjectType type;
    private ProjectVisibility visibility;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID leadId;
    private Instant createdAt;
    private Instant updatedAt;

    public Project() {}

    public Project(UUID organizationId, String name, String key, String description, ProjectType type) {
        this.organizationId = organizationId;
        this.name = name;
        this.key = key.toUpperCase();
        this.description = description;
        this.type = type;
        this.visibility = ProjectVisibility.PRIVATE;
        this.status = ProjectStatus.ACTIVE;
    }

    public enum ProjectType {
        SOFTWARE, DATA, INFRASTRUCTURE, MARKETING, HR, SUPPORT, PRODUCT
    }

    public enum ProjectVisibility {
        PUBLIC, PRIVATE
    }

    public enum ProjectStatus {
        ACTIVE, ARCHIVED, ON_HOLD, COMPLETED
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public ProjectType getType() { return type; }
    public void setType(ProjectType type) { this.type = type; }

    public ProjectVisibility getVisibility() { return visibility; }
    public void setVisibility(ProjectVisibility visibility) { this.visibility = visibility; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public UUID getLeadId() { return leadId; }
    public void setLeadId(UUID leadId) { this.leadId = leadId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
