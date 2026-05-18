package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID ticketId,
        UUID authorId,
        String content,
        Instant createdAt,
        Instant updatedAt
) {}
