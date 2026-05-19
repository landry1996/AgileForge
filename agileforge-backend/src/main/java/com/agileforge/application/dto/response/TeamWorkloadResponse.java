package com.agileforge.application.dto.response;

import java.util.UUID;

public record TeamWorkloadResponse(
        UUID userId,
        String userName,
        int assignedTickets,
        int inProgressTickets,
        int completedThisSprint,
        int totalPointsAssigned
) {}
