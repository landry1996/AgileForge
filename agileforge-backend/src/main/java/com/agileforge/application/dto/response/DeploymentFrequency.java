package com.agileforge.application.dto.response;

public record DeploymentFrequency(
        double deploymentsPerWeek,
        String level
) {}
