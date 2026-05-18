package com.agileforge.application.dto.response;

public record GeneratedTicketResponse(
        String title,
        String description,
        String type,
        String priority,
        Integer storyPoints,
        String acceptanceCriteria
) {}
