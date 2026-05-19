package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record TeamCapacityResponse(
        UUID projectId,
        UUID sprintId,
        double totalAvailableHours,
        double totalPlannedLeave,
        double netCapacity,
        List<MemberCapacity> members
) {}
