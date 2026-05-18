package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateCommentRequest;
import com.agileforge.application.dto.request.CreateTicketRequest;
import com.agileforge.application.dto.request.UpdateTicketRequest;
import com.agileforge.application.dto.response.CommentResponse;
import com.agileforge.application.dto.response.TicketHistoryResponse;
import com.agileforge.application.dto.response.TicketResponse;
import com.agileforge.application.service.TicketService;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketComment;
import com.agileforge.domain.model.TicketHistory;
import com.agileforge.domain.model.TicketStatus;
import com.agileforge.domain.port.out.UserRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets", description = "Ticket management endpoints")
public class TicketController {

    private final TicketService ticketService;
    private final UserRepositoryPort userRepository;

    public TicketController(TicketService ticketService, UserRepositoryPort userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    @PostMapping("/project/{projectId}")
    @Operation(summary = "Create a new ticket")
    public ResponseEntity<TicketResponse> create(@PathVariable UUID projectId,
                                                 @Valid @RequestBody CreateTicketRequest request,
                                                 Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Ticket ticket = ticketService.create(projectId, request.title(), request.description(),
                request.type(), request.priority(), request.assigneeId(), request.epicId(),
                request.parentId(), request.storyPoints(), request.estimatedHours(),
                request.dueDate(), request.environment(), request.component(), request.labels(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ticket));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<TicketResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(ticketService.getById(id)));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get all tickets in a project")
    public ResponseEntity<List<TicketResponse>> getByProject(@PathVariable UUID projectId) {
        List<TicketResponse> tickets = ticketService.getByProjectId(projectId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/project/{projectId}/status/{status}")
    @Operation(summary = "Get tickets by status")
    public ResponseEntity<List<TicketResponse>> getByStatus(@PathVariable UUID projectId,
                                                            @PathVariable String status) {
        TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
        List<TicketResponse> tickets = ticketService.getByProjectIdAndStatus(projectId, ticketStatus).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my assigned tickets")
    public ResponseEntity<List<TicketResponse>> getMyTickets(Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        List<TicketResponse> tickets = ticketService.getByAssigneeId(userId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/sprint/{sprintId}")
    @Operation(summary = "Get tickets in a sprint")
    public ResponseEntity<List<TicketResponse>> getBySprint(@PathVariable UUID sprintId) {
        List<TicketResponse> tickets = ticketService.getBySprintId(sprintId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/epic/{epicId}")
    @Operation(summary = "Get tickets in an epic")
    public ResponseEntity<List<TicketResponse>> getByEpic(@PathVariable UUID epicId) {
        List<TicketResponse> tickets = ticketService.getByEpicId(epicId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a ticket")
    public ResponseEntity<TicketResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateTicketRequest request,
                                                 Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Ticket ticket = ticketService.update(id, request.title(), request.description(),
                request.type(), request.status(), request.priority(), request.assigneeId(),
                request.epicId(), request.parentId(), request.sprintId(), request.storyPoints(),
                request.estimatedHours(), request.dueDate(), request.environment(),
                request.component(), request.labels(), request.affectedVersion(),
                request.fixVersion(), userId);
        return ResponseEntity.ok(toResponse(ticket));
    }

    @PatchMapping("/{id}/transition/{status}")
    @Operation(summary = "Transition ticket to a new status")
    public ResponseEntity<TicketResponse> transition(@PathVariable UUID id,
                                                     @PathVariable String status,
                                                     Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        TicketStatus newStatus = TicketStatus.valueOf(status.toUpperCase());
        Ticket ticket = ticketService.transition(id, newStatus, userId);
        return ResponseEntity.ok(toResponse(ticket));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment to a ticket")
    public ResponseEntity<CommentResponse> addComment(@PathVariable UUID id,
                                                      @Valid @RequestBody CreateCommentRequest request,
                                                      Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        TicketComment comment = ticketService.addComment(id, userId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(toCommentResponse(comment));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get comments on a ticket")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID id) {
        List<CommentResponse> comments = ticketService.getComments(id).stream()
                .map(this::toCommentResponse).toList();
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId, Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        ticketService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get ticket history")
    public ResponseEntity<List<TicketHistoryResponse>> getHistory(@PathVariable UUID id) {
        List<TicketHistoryResponse> history = ticketService.getHistory(id).stream()
                .map(this::toHistoryResponse).toList();
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{id}/log-time")
    @Operation(summary = "Log time on a ticket")
    public ResponseEntity<Void> logTime(@PathVariable UUID id,
                                        @RequestParam double hours,
                                        Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        ticketService.logTime(id, hours, userId);
        return ResponseEntity.ok().build();
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(), t.getProjectId(), t.getFullKey(), t.getKey(), t.getNumber(),
                t.getTitle(), t.getDescription(),
                t.getType() != null ? t.getType().name() : null,
                t.getStatus() != null ? t.getStatus().name() : null,
                t.getPriority() != null ? t.getPriority().name() : null,
                t.getAssigneeId(), t.getReporterId(), t.getEpicId(), t.getParentId(),
                t.getSprintId(), t.getStoryPoints(), t.getEstimatedHours(), t.getLoggedHours(),
                t.getDueDate(), t.getEnvironment(), t.getComponent(), t.getLabels(),
                t.getAffectedVersion(), t.getFixVersion(), t.getQualityScore(),
                t.getCreatedAt(), t.getUpdatedAt());
    }

    private CommentResponse toCommentResponse(TicketComment c) {
        return new CommentResponse(c.getId(), c.getTicketId(), c.getAuthorId(),
                c.getContent(), c.getCreatedAt(), c.getUpdatedAt());
    }

    private TicketHistoryResponse toHistoryResponse(TicketHistory h) {
        return new TicketHistoryResponse(h.getId(), h.getTicketId(), h.getUserId(),
                h.getField(), h.getOldValue(), h.getNewValue(), h.getCreatedAt());
    }
}
