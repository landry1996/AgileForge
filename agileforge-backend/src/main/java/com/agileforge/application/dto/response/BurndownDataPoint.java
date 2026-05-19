package com.agileforge.application.dto.response;

import java.time.LocalDate;

public record BurndownDataPoint(
        LocalDate date,
        int remainingPoints,
        double idealPoints
) {}
