package com.agileforge.application.dto.response;

import java.util.List;

public record QualityAnalysisResponse(
        int score,
        List<String> issues,
        List<String> suggestions,
        String improvedTitle,
        String improvedDescription
) {}
