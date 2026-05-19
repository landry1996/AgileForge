package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AuditAlertRuleResponse(
        UUID id,
        String name,
        String actionPattern,
        String severity,
        String notifyEmails,
        boolean isActive,
        Instant createdAt
) {}
