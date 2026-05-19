package com.agileforge.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkflowResponse(
        UUID id,
        UUID projectId,
        String name,
        String ticketType,
        boolean isDefault,
        List<WorkflowStatusResponse> statuses,
        List<WorkflowTransitionResponse> transitions,
        Instant createdAt
) {

    public record WorkflowStatusResponse(
            UUID id,
            String name,
            String category,
            int position,
            String color
    ) {}

    public record WorkflowTransitionResponse(
            UUID id,
            String fromStatus,
            String toStatus
    ) {}
}
