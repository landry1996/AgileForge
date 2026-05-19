package com.agileforge.application.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProjectHealthResponse(
        UUID projectId,
        int overallScore,
        Map<String, Integer> breakdown,
        String trend,
        List<String> recommendations
) {}
