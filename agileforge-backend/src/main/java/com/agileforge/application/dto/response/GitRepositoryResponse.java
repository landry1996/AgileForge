package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GitRepositoryResponse(
        UUID id,
        UUID projectId,
        String provider,
        String owner,
        String repoName,
        String defaultBranch,
        boolean isActive,
        Instant createdAt
) {}
