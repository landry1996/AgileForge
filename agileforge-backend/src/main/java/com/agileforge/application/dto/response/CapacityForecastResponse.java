package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record CapacityForecastResponse(
        UUID projectId,
        double currentSprintCapacity,
        double averageVelocity,
        int estimatedSprintsToComplete,
        List<String> bottleneckSkills
) {}
