package com.agileforge.application.dto.response;

import java.util.UUID;

public record RiskAlert(
        String category,
        String severity,
        String title,
        String description,
        String suggestion,
        UUID referenceId
) {}
