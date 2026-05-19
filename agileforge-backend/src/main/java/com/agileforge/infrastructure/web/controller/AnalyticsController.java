package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.response.*;
import com.agileforge.application.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Analytics", description = "Analytics and charts endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/projects/{projectId}/analytics")
    @Operation(summary = "Get project analytics dashboard data")
    public ResponseEntity<ProjectAnalyticsResponse> getProjectAnalytics(@PathVariable UUID projectId) {
        return ResponseEntity.ok(analyticsService.getProjectAnalytics(projectId));
    }

    @GetMapping("/sprints/{sprintId}/metrics")
    @Operation(summary = "Get sprint metrics with burndown and burnup chart data")
    public ResponseEntity<SprintMetricsResponse> getSprintMetrics(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(analyticsService.getSprintMetrics(sprintId));
    }

    @GetMapping("/projects/{projectId}/velocity")
    @Operation(summary = "Get velocity history for completed sprints")
    public ResponseEntity<List<VelocityDataPoint>> getVelocityHistory(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "5") int last) {
        return ResponseEntity.ok(analyticsService.getVelocityHistory(projectId, last));
    }

    @GetMapping("/projects/{projectId}/workload")
    @Operation(summary = "Get team workload distribution for a project")
    public ResponseEntity<List<TeamWorkloadResponse>> getTeamWorkload(@PathVariable UUID projectId) {
        return ResponseEntity.ok(analyticsService.getTeamWorkload(projectId));
    }
}
