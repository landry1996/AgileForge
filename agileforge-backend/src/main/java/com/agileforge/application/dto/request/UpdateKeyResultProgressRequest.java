package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateKeyResultProgressRequest(
        @NotNull(message = "Current value is required")
        Double currentValue
) {}
