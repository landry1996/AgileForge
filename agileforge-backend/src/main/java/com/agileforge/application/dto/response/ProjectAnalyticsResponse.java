package com.agileforge.application.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProjectAnalyticsResponse(
        UUID projectId,
        long totalTickets,
        long openTickets,
        long closedTickets,
        double averageVelocity,
        double averageCycleTimeDays,
        List<VelocityDataPoint> velocityHistory,
        Map<String, Long> ticketsByType,
        Map<String, Long> ticketsByPriority,
        Map<String, Long> ticketsByStatus
) {}
