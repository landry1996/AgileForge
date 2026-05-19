package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateSavedFilterRequest(
        @NotBlank(message = "Filter name is required")
        @Size(max = 100, message = "Filter name must not exceed 100 characters")
        String name,

        @NotNull(message = "Filter configuration is required")
        Map<String, Object> filterConfig,

        boolean isShared
) {}
