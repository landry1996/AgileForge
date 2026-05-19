package com.agileforge.application.dto.response;

import java.time.LocalDate;

public record BurnupDataPoint(
        LocalDate date,
        int totalScope,
        int completedPoints
) {}
