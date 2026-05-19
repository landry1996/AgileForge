package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record SprintCapacitySuggestionResponse(
        UUID projectId,
        int suggestedPoints,
        int confidence,
        List<Integer> historicalVelocities,
        String reasoning
) {}
