package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record TicketDelayPredictionResponse(
        UUID ticketId,
        String ticketKey,
        boolean atRisk,
        int estimatedDelayDays,
        String riskLevel,
        List<String> reasons
) {}
