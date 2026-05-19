package com.agileforge.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ObjectiveResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        String period,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        int progress,
        List<KeyResultResponse> keyResults,
        Instant createdAt
) {}
