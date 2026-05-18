package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record BoardResponse(
        UUID projectId,
        String projectName,
        String projectKey,
        UUID activeSprintId,
        String activeSprintName,
        List<BoardColumnResponse> columns
) {
    public record BoardColumnResponse(
            UUID id,
            String name,
            String mappedStatus,
            int position,
            Integer wipLimit,
            List<TicketResponse> tickets
    ) {}
}
