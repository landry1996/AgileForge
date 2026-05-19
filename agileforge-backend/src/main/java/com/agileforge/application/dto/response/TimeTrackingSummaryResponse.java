package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record TimeTrackingSummaryResponse(
        UUID ticketId,
        double totalLogged,
        Double estimatedHours,
        List<TimeEntryResponse> entries
) {}
