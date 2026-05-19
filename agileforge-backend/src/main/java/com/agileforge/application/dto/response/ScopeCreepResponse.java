package com.agileforge.application.dto.response;

import java.util.UUID;

public record ScopeCreepResponse(
        UUID sprintId,
        int originalScope,
        int currentScope,
        int addedCount,
        int removedCount,
        double scopeChangePercent,
        String warning
) {}
