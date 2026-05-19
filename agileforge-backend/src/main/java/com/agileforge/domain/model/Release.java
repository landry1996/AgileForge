package com.agileforge.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Release {

    private UUID id;
    private UUID projectId;
    private String name;
    private String version;
    private String description;
    private ReleaseStatus status;
    private LocalDate startDate;
    private LocalDate releaseDate;
    private Instant releasedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Release() {}

    public Release(UUID projectId, String name, String version, String description,
                   LocalDate startDate, LocalDate releaseDate) {
        this.projectId = projectId;
        this.name = name;
        this.version = version;
        this.description = description;
        this.status = ReleaseStatus.PLANNING;
        this.startDate = startDate;
        this.releaseDate = releaseDate;
    }

    public boolean canRelease() {
        return status == ReleaseStatus.PLANNING || status == ReleaseStatus.IN_PROGRESS || status == ReleaseStatus.READY;
    }

    public boolean isReleased() {
        return status == ReleaseStatus.RELEASED;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReleaseStatus getStatus() { return status; }
    public void setStatus(ReleaseStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
