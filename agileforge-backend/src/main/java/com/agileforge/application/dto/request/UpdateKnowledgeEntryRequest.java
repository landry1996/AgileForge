package com.agileforge.application.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateKnowledgeEntryRequest(
        @Size(max = 200)
        String title,

        String content,

        @Size(max = 500)
        String tags,

        Boolean isActive
) {}
