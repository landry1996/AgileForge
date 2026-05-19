package com.agileforge.application.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RoadmapItemResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        String category,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        String color,
        int position,
        UUID releaseId,
        UUID epicId,
        Instant createdAt
) {}
