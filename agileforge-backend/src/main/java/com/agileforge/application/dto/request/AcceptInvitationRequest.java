package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(
        @NotBlank(message = "Token is required")
        String token
) {}
