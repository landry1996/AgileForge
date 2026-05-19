package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record SprintMetricsResponse(
        UUID sprintId,
        String sprintName,
        int totalTickets,
        int completedTickets,
        int totalPoints,
        int completedPoints,
        List<BurndownDataPoint> burndownData,
        List<BurnupDataPoint> burnupData
) {}
