package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuditAlertRuleRequest(
        @NotBlank(message = "Rule name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Action pattern is required")
        @Size(max = 100, message = "Action pattern must not exceed 100 characters")
        String actionPattern,

        String severity,

        String notifyEmails
) {}
