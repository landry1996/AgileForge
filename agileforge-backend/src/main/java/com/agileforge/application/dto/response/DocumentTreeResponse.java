package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record DocumentTreeResponse(
        UUID id,
        String title,
        String docType,
        int position,
        List<DocumentTreeResponse> children
) {}
