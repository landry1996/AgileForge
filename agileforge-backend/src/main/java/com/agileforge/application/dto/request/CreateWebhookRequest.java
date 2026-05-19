package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateWebhookRequest(
        @NotBlank(message = "URL is required")
        String url,

        String secret,

        List<String> events
) {}
