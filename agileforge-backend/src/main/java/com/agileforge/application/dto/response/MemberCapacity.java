package com.agileforge.application.dto.response;

import java.util.UUID;

public record MemberCapacity(
        UUID userId,
        String userName,
        double availableHours,
        double plannedLeaveHours,
        int currentAssignedPoints,
        int loadPercentage
) {}
