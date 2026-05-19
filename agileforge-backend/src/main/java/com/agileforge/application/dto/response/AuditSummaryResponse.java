package com.agileforge.application.dto.response;

import java.util.Map;

public record AuditSummaryResponse(
        long totalEvents,
        long criticalEvents,
        long todayEvents,
        Map<String, Long> topActions,
        Map<String, Long> topUsers
) {}
