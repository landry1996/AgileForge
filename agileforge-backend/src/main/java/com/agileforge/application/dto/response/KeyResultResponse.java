package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record KeyResultResponse(
        UUID id,
        UUID objectiveId,
        String title,
        double targetValue,
        double currentValue,
        String unit,
        double startValue,
        int progress,
        Instant createdAt
) {}
