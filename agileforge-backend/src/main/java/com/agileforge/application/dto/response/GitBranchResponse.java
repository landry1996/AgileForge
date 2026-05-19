package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GitBranchResponse(
        UUID id,
        UUID repositoryId,
        UUID ticketId,
        String branchName,
        Instant createdAt
) {}
