package com.agileforge.application.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateDocumentRequest(
        @Size(min = 1, max = 300, message = "Title must be between 1 and 300 characters")
        String title,

        String content,

        String status,

        @Size(max = 500, message = "Change summary must not exceed 500 characters")
        String changeSummary
) {}
