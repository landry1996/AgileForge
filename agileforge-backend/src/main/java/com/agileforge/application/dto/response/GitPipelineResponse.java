package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GitPipelineResponse(
        UUID id,
        UUID repositoryId,
        UUID ticketId,
        UUID prId,
        String pipelineId,
        String status,
        String url,
        Instant startedAt,
        Instant finishedAt,
        Instant createdAt
) {}
