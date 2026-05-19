package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GitPullRequestResponse(
        UUID id,
        UUID repositoryId,
        UUID ticketId,
        int prNumber,
        String title,
        String status,
        String author,
        String sourceBranch,
        String targetBranch,
        String url,
        Instant createdAt,
        Instant mergedAt,
        Instant closedAt
) {}
