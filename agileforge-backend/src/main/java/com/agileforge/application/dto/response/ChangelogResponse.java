package com.agileforge.application.dto.response;

import java.util.List;

public record ChangelogResponse(
        String version,
        String date,
        List<String> features,
        List<String> bugFixes,
        List<String> improvements,
        List<String> breakingChanges
) {}
