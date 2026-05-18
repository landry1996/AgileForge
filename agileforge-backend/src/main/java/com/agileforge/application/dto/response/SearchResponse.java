package com.agileforge.application.dto.response;

import java.util.List;

public record SearchResponse<T>(
        List<T> items,
        long totalCount,
        int page,
        int pageSize,
        int totalPages
) {}
