package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ClientPortalResponse(
        UUID id,
        UUID projectId,
        boolean isEnabled,
        String welcomeMessage,
        String allowedTicketTypes,
        boolean showRoadmap,
        boolean showReleases,
        boolean showChangelog,
        String customBranding,
        int clientCount,
        Instant createdAt,
        Instant updatedAt
) {}
