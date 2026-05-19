package com.agileforge.application.dto.response;

import java.util.UUID;

public record ApiKeyCreatedResponse(
        UUID id,
        String name,
        String key,
        String keyPrefix
) {}
