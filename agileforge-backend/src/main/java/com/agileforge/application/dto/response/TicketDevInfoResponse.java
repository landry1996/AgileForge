package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record TicketDevInfoResponse(
        UUID ticketId,
        List<GitBranchResponse> branches,
        List<GitPullRequestResponse> pullRequests,
        List<GitPipelineResponse> pipelines
) {}
