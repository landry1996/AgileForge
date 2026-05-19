package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateLabelRequest;
import com.agileforge.application.dto.response.LabelResponse;
import com.agileforge.application.service.LabelService;
import com.agileforge.domain.model.Label;
import com.agileforge.domain.port.out.UserRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Labels", description = "Label management endpoints")
public class LabelController {

    private final LabelService labelService;
    private final UserRepositoryPort userRepository;

    public LabelController(LabelService labelService, UserRepositoryPort userRepository) {
        this.labelService = labelService;
        this.userRepository = userRepository;
    }

    @PostMapping("/projects/{projectId}/labels")
    @Operation(summary = "Create a label for a project")
    public ResponseEntity<LabelResponse> create(@PathVariable UUID projectId,
                                                @Valid @RequestBody CreateLabelRequest request) {
        Label label = labelService.createLabel(projectId, request.name(), request.color(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(label));
    }

    @GetMapping("/projects/{projectId}/labels")
    @Operation(summary = "Get all labels for a project")
    public ResponseEntity<List<LabelResponse>> getProjectLabels(@PathVariable UUID projectId) {
        List<LabelResponse> labels = labelService.getProjectLabels(projectId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(labels);
    }

    @PutMapping("/labels/{labelId}")
    @Operation(summary = "Update a label")
    public ResponseEntity<LabelResponse> update(@PathVariable UUID labelId,
                                                @Valid @RequestBody CreateLabelRequest request) {
        Label label = labelService.updateLabel(labelId, request.name(), request.color(), request.description());
        return ResponseEntity.ok(toResponse(label));
    }

    @DeleteMapping("/labels/{labelId}")
    @Operation(summary = "Delete a label")
    public ResponseEntity<Void> delete(@PathVariable UUID labelId) {
        labelService.deleteLabel(labelId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tickets/{ticketId}/labels/{labelId}")
    @Operation(summary = "Add a label to a ticket")
    public ResponseEntity<Void> addLabelToTicket(@PathVariable UUID ticketId,
                                                  @PathVariable UUID labelId) {
        labelService.addLabelToTicket(ticketId, labelId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tickets/{ticketId}/labels/{labelId}")
    @Operation(summary = "Remove a label from a ticket")
    public ResponseEntity<Void> removeLabelFromTicket(@PathVariable UUID ticketId,
                                                       @PathVariable UUID labelId) {
        labelService.removeLabelFromTicket(ticketId, labelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tickets/{ticketId}/labels")
    @Operation(summary = "Get labels for a ticket")
    public ResponseEntity<List<LabelResponse>> getTicketLabels(@PathVariable UUID ticketId) {
        List<LabelResponse> labels = labelService.getTicketLabels(ticketId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(labels);
    }

    private LabelResponse toResponse(Label label) {
        return new LabelResponse(
                label.getId(),
                label.getProjectId(),
                label.getName(),
                label.getColor(),
                label.getDescription(),
                label.getCreatedAt()
        );
    }
}
