package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record ProjectContextResponse(
        UUID projectId,
        String projectName,
        List<String> techStack,
        String architecture,
        List<String> conventions,
        List<String> decisions,
        List<String> knownIssues
) {}
