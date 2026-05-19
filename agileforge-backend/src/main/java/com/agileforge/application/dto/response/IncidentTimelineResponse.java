package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record IncidentTimelineResponse(
        UUID incidentId,
        String title,
        String severity,
        String status,
        List<IncidentEventResponse> events,
        List<UUID> participants,
        long durationMinutes
) {}
