package com.agileforge.application.dto.response;

public record VelocityDataPoint(
        String sprintName,
        int committedPoints,
        int completedPoints,
        int sprintNumber
) {}
