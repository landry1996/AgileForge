package com.agileforge.application.dto.response;

import java.util.List;
import java.util.UUID;

public record PortfolioDashboardResponse(
        UUID portfolioId,
        String portfolioName,
        int totalProjects,
        int overallHealthScore,
        List<PortfolioProjectSummary> projectSummaries,
        List<RiskHeatMapEntry> riskHeatMap
) {}
