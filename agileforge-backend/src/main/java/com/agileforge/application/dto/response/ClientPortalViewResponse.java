package com.agileforge.application.dto.response;

import java.util.List;
import java.util.Map;

public record ClientPortalViewResponse(
        String projectName,
        String welcomeMessage,
        List<Map<String, Object>> publicTickets,
        List<Map<String, Object>> roadmapItems,
        List<Map<String, Object>> releases
) {}
