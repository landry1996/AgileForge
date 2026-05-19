package com.agileforge.application.dto.response;

import java.util.List;

public record AcceptanceCriteriaResponse(
        List<String> criteria,
        List<String> testSuggestions
) {}
