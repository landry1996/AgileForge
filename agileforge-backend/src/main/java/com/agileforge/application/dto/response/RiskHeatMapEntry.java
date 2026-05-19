package com.agileforge.application.dto.response;

import java.util.UUID;

public record RiskHeatMapEntry(
        UUID projectId,
        String projectName,
        String riskLevel,
        int riskScore
) {}
