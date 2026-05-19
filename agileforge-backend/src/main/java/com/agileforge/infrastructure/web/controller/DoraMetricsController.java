package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.response.DoraMetricsResponse;
import com.agileforge.application.service.DoraMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "DORA Metrics", description = "DevOps Research and Assessment metrics endpoints")
public class DoraMetricsController {

    private final DoraMetricsService doraMetricsService;

    public DoraMetricsController(DoraMetricsService doraMetricsService) {
        this.doraMetricsService = doraMetricsService;
    }

    @GetMapping("/projects/{projectId}/dora-metrics")
    @Operation(summary = "Get DORA metrics for a project")
    public ResponseEntity<DoraMetricsResponse> getDoraMetrics(@PathVariable UUID projectId) {
        DoraMetricsResponse metrics = doraMetricsService.calculateDoraMetrics(projectId);
        return ResponseEntity.ok(metrics);
    }
}
