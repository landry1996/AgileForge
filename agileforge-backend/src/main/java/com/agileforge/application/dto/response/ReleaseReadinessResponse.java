package com.agileforge.application.dto.response;

import java.util.UUID;

public record ReleaseReadinessResponse(
        UUID releaseId,
        String version,
        int totalTickets,
        int completedTickets,
        int openBugs,
        int unresolvedDependencies,
        int readinessScore,
        String recommendation
) {}
