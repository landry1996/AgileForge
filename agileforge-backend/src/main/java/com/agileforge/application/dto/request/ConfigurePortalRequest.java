package com.agileforge.application.dto.request;

import java.util.List;

public record ConfigurePortalRequest(
        Boolean isEnabled,
        String welcomeMessage,
        List<String> allowedTicketTypes,
        Boolean showRoadmap,
        Boolean showReleases,
        Boolean showChangelog
) {}
