package com.agileforge.application.dto.request;

import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateSavedFilterRequest(
        @Size(max = 100, message = "Filter name must not exceed 100 characters")
        String name,

        Map<String, Object> filterConfig,

        Boolean isShared
) {}
