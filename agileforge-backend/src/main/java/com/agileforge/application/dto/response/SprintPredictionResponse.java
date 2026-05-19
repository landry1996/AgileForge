package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record SprintPredictionResponse(
        UUID sprintId,
        String sprintName,
        int completionProbability,
        int remainingPoints,
        int daysRemaining,
        double currentVelocityPerDay,
        List<String> riskFactors,
        String recommendation
) {}
