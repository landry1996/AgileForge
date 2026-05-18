package com.agileforge.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "Comment content is required")
        @Size(min = 1, max = 10000, message = "Comment must not exceed 10000 characters")
        String content
) {}
