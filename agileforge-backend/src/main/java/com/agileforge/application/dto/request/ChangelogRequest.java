package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangelogRequest(
        @NotNull(message = "Release ID is required")
        UUID releaseId
) {}
