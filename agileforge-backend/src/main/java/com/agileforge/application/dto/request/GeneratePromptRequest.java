package com.agileforge.application.dto.request;

import java.util.UUID;

public record GeneratePromptRequest(
        UUID templateId,
        String customInstructions
) {}
