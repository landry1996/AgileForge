package com.agileforge.application.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateRoadmapItemRequest(
        @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @Size(max = 50, message = "Category must not exceed 50 characters")
        String category,

        String status,

        String startDate,

        String endDate,

        @Size(max = 7, message = "Color must be a valid hex color")
        String color,

        Integer position,

        UUID releaseId,

        UUID epicId
) {}
