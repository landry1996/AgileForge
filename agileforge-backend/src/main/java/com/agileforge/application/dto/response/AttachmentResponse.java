package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        UUID ticketId,
        String fileName,
        long fileSize,
        String contentType,
        UUID uploadedBy,
        Instant createdAt,
        String downloadUrl
) {}
