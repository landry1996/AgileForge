package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "Role code is required")
        String roleCode
) {}
