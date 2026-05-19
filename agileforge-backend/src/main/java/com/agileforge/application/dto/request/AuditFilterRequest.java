package com.agileforge.application.dto.request;

import java.util.UUID;

public record AuditFilterRequest(
        UUID userId,
        String action,
        String entityType,
        String severity,
        String fromDate,
        String toDate,
        Integer page,
        Integer size
) {
    public int getPage() {
        return page != null ? page : 0;
    }

    public int getSize() {
        return size != null ? size : 20;
    }
}
