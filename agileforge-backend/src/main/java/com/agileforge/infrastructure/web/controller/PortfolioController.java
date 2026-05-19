package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreatePortfolioRequest;
import com.agileforge.application.dto.response.PortfolioDashboardResponse;
import com.agileforge.application.dto.response.PortfolioProjectSummary;
import com.agileforge.application.dto.response.PortfolioResponse;
import com.agileforge.application.dto.response.RiskHeatMapEntry;
import com.agileforge.application.service.PortfolioService;
import com.agileforge.domain.model.Portfolio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Portfolio Management", description = "Portfolio management and consolidated project views")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping("/organizations/{orgId}/portfolios")
    @Operation(summary = "Create a new portfolio")
    public ResponseEntity<PortfolioResponse> create(@PathVariable UUID orgId,
                                                    @Valid @RequestBody CreatePortfolioRequest request) {
        Portfolio portfolio = portfolioService.createPortfolio(orgId, request.name(),
                request.description(), null, request.projectIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(portfolio));
    }

    @GetMapping("/organizations/{orgId}/portfolios")
    @Operation(summary = "Get all portfolios for an organization")
    public ResponseEntity<List<PortfolioResponse>> getByOrganization(@PathVariable UUID orgId) {
        List<PortfolioResponse> portfolios = portfolioService.getByOrganization(orgId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(portfolios);
    }

    @GetMapping("/portfolios/{portfolioId}")
    @Operation(summary = "Get portfolio by ID")
    public ResponseEntity<PortfolioResponse> getById(@PathVariable UUID portfolioId) {
        Portfolio portfolio = portfolioService.getById(portfolioId);
        return ResponseEntity.ok(toResponseWithProjects(portfolio, portfolioId));
    }

    @PutMapping("/portfolios/{portfolioId}")
    @Operation(summary = "Update a portfolio")
    public ResponseEntity<PortfolioResponse> update(@PathVariable UUID portfolioId,
                                                    @Valid @RequestBody CreatePortfolioRequest request) {
        Portfolio portfolio = portfolioService.updatePortfolio(portfolioId, request.name(), request.description());
        return ResponseEntity.ok(toResponse(portfolio));
    }

    @DeleteMapping("/portfolios/{portfolioId}")
    @Operation(summary = "Delete a portfolio")
    public ResponseEntity<Void> delete(@PathVariable UUID portfolioId) {
        portfolioService.deletePortfolio(portfolioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/portfolios/{portfolioId}/projects/{projectId}")
    @Operation(summary = "Add a project to a portfolio")
    public ResponseEntity<Void> addProject(@PathVariable UUID portfolioId, @PathVariable UUID projectId) {
        portfolioService.addProject(portfolioId, projectId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/portfolios/{portfolioId}/projects/{projectId}")
    @Operation(summary = "Remove a project from a portfolio")
    public ResponseEntity<Void> removeProject(@PathVariable UUID portfolioId, @PathVariable UUID projectId) {
        portfolioService.removeProject(portfolioId, projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/portfolios/{portfolioId}/dashboard")
    @Operation(summary = "Get portfolio dashboard with aggregated metrics")
    public ResponseEntity<PortfolioDashboardResponse> getDashboard(@PathVariable UUID portfolioId) {
        PortfolioDashboardResponse dashboard = portfolioService.getDashboard(portfolioId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/portfolios/{portfolioId}/risk-heatmap")
    @Operation(summary = "Get risk heat map across portfolio projects")
    public ResponseEntity<List<RiskHeatMapEntry>> getRiskHeatMap(@PathVariable UUID portfolioId) {
        List<RiskHeatMapEntry> heatMap = portfolioService.getRiskHeatMap(portfolioId);
        return ResponseEntity.ok(heatMap);
    }

    private PortfolioResponse toResponse(Portfolio p) {
        return new PortfolioResponse(
                p.getId(),
                p.getOrganizationId(),
                p.getName(),
                p.getDescription(),
                p.getOwnerId(),
                List.of(),
                p.getCreatedAt()
        );
    }

    private PortfolioResponse toResponseWithProjects(Portfolio p, UUID portfolioId) {
        List<PortfolioProjectSummary> projects = portfolioService.getProjectSummaries(portfolioId);
        return new PortfolioResponse(
                p.getId(),
                p.getOrganizationId(),
                p.getName(),
                p.getDescription(),
                p.getOwnerId(),
                projects,
                p.getCreatedAt()
        );
    }
}
