package com.agileforge.application.dto.response;

import java.util.UUID;

public record DoraMetricsResponse(
        UUID projectId,
        DeploymentFrequency deploymentFrequency,
        LeadTime leadTimeForChanges,
        MTTR meanTimeToRecovery,
        ChangeFailureRate changeFailureRate,
        String overallLevel
) {}
