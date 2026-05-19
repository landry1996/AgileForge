package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateKeyResultRequest;
import com.agileforge.application.dto.request.CreateObjectiveRequest;
import com.agileforge.application.dto.request.UpdateKeyResultProgressRequest;
import com.agileforge.application.dto.response.KeyResultResponse;
import com.agileforge.application.dto.response.ObjectiveResponse;
import com.agileforge.application.service.OkrService;
import com.agileforge.domain.model.KeyResult;
import com.agileforge.domain.model.Objective;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "OKR Management", description = "Objectives and Key Results management endpoints")
public class OkrController {

    private final OkrService okrService;

    public OkrController(OkrService okrService) {
        this.okrService = okrService;
    }

    @PostMapping("/projects/{projectId}/objectives")
    @Operation(summary = "Create a new objective")
    public ResponseEntity<ObjectiveResponse> createObjective(@PathVariable UUID projectId,
                                                             @Valid @RequestBody CreateObjectiveRequest request) {
        Objective objective = okrService.createObjective(projectId, request.title(), request.description(),
                request.period(), request.startDate(), request.endDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(toObjectiveResponse(objective));
    }

    @GetMapping("/projects/{projectId}/objectives")
    @Operation(summary = "Get all objectives for a project")
    public ResponseEntity<List<ObjectiveResponse>> getByProject(@PathVariable UUID projectId) {
        List<ObjectiveResponse> objectives = okrService.getByProject(projectId).stream()
                .map(this::toObjectiveResponse).toList();
        return ResponseEntity.ok(objectives);
    }

    @GetMapping("/objectives/{objectiveId}")
    @Operation(summary = "Get objective by ID")
    public ResponseEntity<ObjectiveResponse> getById(@PathVariable UUID objectiveId) {
        Objective objective = okrService.getById(objectiveId);
        return ResponseEntity.ok(toObjectiveResponse(objective));
    }

    @PutMapping("/objectives/{objectiveId}")
    @Operation(summary = "Update an objective")
    public ResponseEntity<ObjectiveResponse> updateObjective(@PathVariable UUID objectiveId,
                                                             @Valid @RequestBody CreateObjectiveRequest request) {
        Objective objective = okrService.updateObjective(objectiveId, request.title(), request.description(),
                request.period(), null, request.startDate(), request.endDate());
        return ResponseEntity.ok(toObjectiveResponse(objective));
    }

    @DeleteMapping("/objectives/{objectiveId}")
    @Operation(summary = "Delete an objective")
    public ResponseEntity<Void> deleteObjective(@PathVariable UUID objectiveId) {
        okrService.deleteObjective(objectiveId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/objectives/{objectiveId}/key-results")
    @Operation(summary = "Add a key result to an objective")
    public ResponseEntity<KeyResultResponse> addKeyResult(@PathVariable UUID objectiveId,
                                                          @Valid @RequestBody CreateKeyResultRequest request) {
        KeyResult keyResult = okrService.addKeyResult(objectiveId, request.title(),
                request.targetValue(), request.unit(), request.startValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(toKeyResultResponse(keyResult));
    }

    @PutMapping("/key-results/{keyResultId}/progress")
    @Operation(summary = "Update key result progress")
    public ResponseEntity<KeyResultResponse> updateKeyResultProgress(@PathVariable UUID keyResultId,
                                                                     @Valid @RequestBody UpdateKeyResultProgressRequest request) {
        KeyResult keyResult = okrService.updateKeyResultProgress(keyResultId, request.currentValue());
        return ResponseEntity.ok(toKeyResultResponse(keyResult));
    }

    @DeleteMapping("/key-results/{keyResultId}")
    @Operation(summary = "Delete a key result")
    public ResponseEntity<Void> deleteKeyResult(@PathVariable UUID keyResultId) {
        okrService.deleteKeyResult(keyResultId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/key-results/{keyResultId}/tickets/{ticketId}")
    @Operation(summary = "Link a ticket to a key result")
    public ResponseEntity<Void> linkTicket(@PathVariable UUID keyResultId, @PathVariable UUID ticketId) {
        okrService.linkTicket(keyResultId, ticketId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/key-results/{keyResultId}/tickets/{ticketId}")
    @Operation(summary = "Unlink a ticket from a key result")
    public ResponseEntity<Void> unlinkTicket(@PathVariable UUID keyResultId, @PathVariable UUID ticketId) {
        okrService.unlinkTicket(keyResultId, ticketId);
        return ResponseEntity.noContent().build();
    }

    private ObjectiveResponse toObjectiveResponse(Objective o) {
        List<KeyResultResponse> keyResultResponses = o.getKeyResults() != null
                ? o.getKeyResults().stream().map(this::toKeyResultResponse).toList()
                : List.of();

        return new ObjectiveResponse(
                o.getId(),
                o.getProjectId(),
                o.getTitle(),
                o.getDescription(),
                o.getPeriod(),
                o.getStartDate(),
                o.getEndDate(),
                o.getStatus() != null ? o.getStatus().name() : null,
                o.getProgress(),
                keyResultResponses,
                o.getCreatedAt()
        );
    }

    private KeyResultResponse toKeyResultResponse(KeyResult kr) {
        return new KeyResultResponse(
                kr.getId(),
                kr.getObjectiveId(),
                kr.getTitle(),
                kr.getTargetValue(),
                kr.getCurrentValue(),
                kr.getUnit(),
                kr.getStartValue(),
                kr.getProgress(),
                kr.getCreatedAt()
        );
    }
}
