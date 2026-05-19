package com.agileforge.application.dto.response;

import java.util.UUID;

public record PortfolioProjectSummary(
        UUID projectId,
        String projectName,
        String projectKey,
        int healthScore,
        int activeSprintProgress,
        long totalTickets,
        long openTickets
) {}
