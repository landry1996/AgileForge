package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.response.*;
import com.agileforge.application.service.BlockingDetectionService;
import com.agileforge.application.service.PredictionService;
import com.agileforge.application.service.ProjectHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Predictions & Health", description = "Predictive analytics, risk detection, and project health")
public class PredictionController {

    private final PredictionService predictionService;
    private final BlockingDetectionService blockingDetectionService;
    private final ProjectHealthService projectHealthService;

    public PredictionController(PredictionService predictionService,
                                BlockingDetectionService blockingDetectionService,
                                ProjectHealthService projectHealthService) {
        this.predictionService = predictionService;
        this.blockingDetectionService = blockingDetectionService;
        this.projectHealthService = projectHealthService;
    }

    @GetMapping("/projects/{projectId}/predictions/sprint/{sprintId}")
    @Operation(summary = "Predict sprint completion probability")
    public ResponseEntity<SprintPredictionResponse> predictSprintCompletion(
            @PathVariable UUID projectId,
            @PathVariable UUID sprintId) {
        return ResponseEntity.ok(predictionService.predictSprintCompletion(sprintId));
    }

    @GetMapping("/tickets/{ticketId}/predictions/delay")
    @Operation(summary = "Predict ticket delay risk")
    public ResponseEntity<TicketDelayPredictionResponse> predictTicketDelay(
            @PathVariable UUID ticketId) {
        return ResponseEntity.ok(predictionService.predictTicketDelay(ticketId));
    }

    @GetMapping("/sprints/{sprintId}/predictions/scope-creep")
    @Operation(summary = "Detect scope creep in a sprint")
    public ResponseEntity<ScopeCreepResponse> detectScopeCreep(
            @PathVariable UUID sprintId) {
        return ResponseEntity.ok(predictionService.detectScopeCreep(sprintId));
    }

    @GetMapping("/projects/{projectId}/predictions/capacity")
    @Operation(summary = "Suggest sprint capacity based on historical velocity")
    public ResponseEntity<SprintCapacitySuggestionResponse> suggestSprintCapacity(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(predictionService.suggestSprintCapacity(projectId));
    }

    @GetMapping("/projects/{projectId}/predictions/blocked")
    @Operation(summary = "Detect blocked and at-risk tickets")
    public ResponseEntity<List<BlockedTicketAlert>> detectBlockedTickets(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(blockingDetectionService.detectBlockedTickets(projectId));
    }

    @GetMapping("/projects/{projectId}/predictions/risks")
    @Operation(summary = "Get project-level risk alerts")
    public ResponseEntity<List<RiskAlert>> getProjectRisks(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(blockingDetectionService.getProjectRisks(projectId));
    }

    @GetMapping("/projects/{projectId}/health")
    @Operation(summary = "Calculate project health score")
    public ResponseEntity<ProjectHealthResponse> getProjectHealth(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(projectHealthService.calculateHealthScore(projectId));
    }
}
