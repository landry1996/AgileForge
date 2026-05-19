package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateWorkflowRequest;
import com.agileforge.application.dto.response.WorkflowResponse;
import com.agileforge.application.service.WorkflowService;
import com.agileforge.domain.model.Workflow;
import com.agileforge.domain.model.WorkflowStatus;
import com.agileforge.domain.model.WorkflowTransition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Workflows", description = "Workflow engine management endpoints")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/projects/{projectId}/workflows")
    @Operation(summary = "Create a new workflow for a project")
    public ResponseEntity<WorkflowResponse> create(@PathVariable UUID projectId,
                                                   @Valid @RequestBody CreateWorkflowRequest request) {
        Workflow workflow = workflowService.createWorkflow(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(workflow));
    }

    @GetMapping("/projects/{projectId}/workflows")
    @Operation(summary = "Get all workflows for a project")
    public ResponseEntity<List<WorkflowResponse>> getByProject(@PathVariable UUID projectId) {
        List<WorkflowResponse> workflows = workflowService.getWorkflowsByProject(projectId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(workflows);
    }

    @GetMapping("/projects/{projectId}/workflows/{ticketType}")
    @Operation(summary = "Get workflow for a specific ticket type in a project")
    public ResponseEntity<WorkflowResponse> getByTicketType(@PathVariable UUID projectId,
                                                            @PathVariable String ticketType) {
        Workflow workflow = workflowService.getWorkflowForTicketType(projectId, ticketType)
                .orElseGet(() -> workflowService.getDefaultWorkflow(ticketType));
        return ResponseEntity.ok(toResponse(workflow));
    }

    @PutMapping("/workflows/{workflowId}")
    @Operation(summary = "Update an existing workflow")
    public ResponseEntity<WorkflowResponse> update(@PathVariable UUID workflowId,
                                                   @Valid @RequestBody CreateWorkflowRequest request) {
        Workflow workflow = workflowService.updateWorkflow(workflowId, request);
        return ResponseEntity.ok(toResponse(workflow));
    }

    @DeleteMapping("/workflows/{workflowId}")
    @Operation(summary = "Delete a workflow (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID workflowId) {
        workflowService.deleteWorkflow(workflowId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/workflows/{workflowId}/validate-transition")
    @Operation(summary = "Validate if a status transition is allowed")
    public ResponseEntity<Boolean> validateTransition(@PathVariable UUID workflowId,
                                                      @RequestParam String from,
                                                      @RequestParam String to) {
        boolean valid = workflowService.validateTransitionByWorkflowId(workflowId, from, to);
        return ResponseEntity.ok(valid);
    }

    private WorkflowResponse toResponse(Workflow workflow) {
        List<WorkflowResponse.WorkflowStatusResponse> statuses = workflow.getStatuses() != null
                ? workflow.getStatuses().stream().map(this::toStatusResponse).toList()
                : List.of();

        List<WorkflowResponse.WorkflowTransitionResponse> transitions = workflow.getTransitions() != null
                ? workflow.getTransitions().stream().map(this::toTransitionResponse).toList()
                : List.of();

        return new WorkflowResponse(
                workflow.getId(),
                workflow.getProjectId(),
                workflow.getName(),
                workflow.getTicketType(),
                workflow.isDefault(),
                statuses,
                transitions,
                workflow.getCreatedAt()
        );
    }

    private WorkflowResponse.WorkflowStatusResponse toStatusResponse(WorkflowStatus status) {
        return new WorkflowResponse.WorkflowStatusResponse(
                status.getId(),
                status.getName(),
                status.getCategory() != null ? status.getCategory().name() : null,
                status.getPosition(),
                status.getColor()
        );
    }

    private WorkflowResponse.WorkflowTransitionResponse toTransitionResponse(WorkflowTransition transition) {
        return new WorkflowResponse.WorkflowTransitionResponse(
                transition.getId(),
                transition.getFromStatus(),
                transition.getToStatus()
        );
    }
}
