package com.agileforge.application.dto.response;

import java.util.List;

public record SprintReportResponse(
        String summary,
        List<String> completedItems,
        List<String> blockers,
        List<String> recommendations,
        String velocityAssessment
) {}
