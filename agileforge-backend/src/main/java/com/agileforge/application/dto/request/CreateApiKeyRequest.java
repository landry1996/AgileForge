package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public record CreateApiKeyRequest(
        @NotBlank(message = "Name is required")
        String name,

        List<String> permissions,

        Instant expiresAt
) {}
