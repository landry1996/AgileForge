package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDocumentRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 300, message = "Title must be between 1 and 300 characters")
        String title,

        String content,

        String docType,

        UUID parentId,

        String status
) {}
