package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ClientPortal {

    private UUID id;
    private UUID projectId;
    private boolean isEnabled;
    private String welcomeMessage;
    private String allowedTicketTypes;
    private boolean showRoadmap;
    private boolean showReleases;
    private boolean showChangelog;
    private String customBranding;
    private Instant createdAt;
    private Instant updatedAt;

    public ClientPortal() {}

    public ClientPortal(UUID projectId) {
        this.projectId = projectId;
        this.isEnabled = false;
        this.showRoadmap = true;
        this.showReleases = true;
        this.showChangelog = true;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }

    public String getAllowedTicketTypes() { return allowedTicketTypes; }
    public void setAllowedTicketTypes(String allowedTicketTypes) { this.allowedTicketTypes = allowedTicketTypes; }

    public boolean isShowRoadmap() { return showRoadmap; }
    public void setShowRoadmap(boolean showRoadmap) { this.showRoadmap = showRoadmap; }

    public boolean isShowReleases() { return showReleases; }
    public void setShowReleases(boolean showReleases) { this.showReleases = showReleases; }

    public boolean isShowChangelog() { return showChangelog; }
    public void setShowChangelog(boolean showChangelog) { this.showChangelog = showChangelog; }

    public String getCustomBranding() { return customBranding; }
    public void setCustomBranding(String customBranding) { this.customBranding = customBranding; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
