package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateSprintRequest;
import com.agileforge.application.dto.response.SprintResponse;
import com.agileforge.application.dto.response.TicketResponse;
import com.agileforge.application.service.SprintService;
import com.agileforge.domain.model.Sprint;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sprints")
@Tag(name = "Sprints", description = "Sprint management endpoints")
public class SprintController {

    private final SprintService sprintService;
    private final TicketRepositoryPort ticketRepository;

    public SprintController(SprintService sprintService, TicketRepositoryPort ticketRepository) {
        this.sprintService = sprintService;
        this.ticketRepository = ticketRepository;
    }

    @PostMapping("/project/{projectId}")
    @Operation(summary = "Create a new sprint")
    public ResponseEntity<SprintResponse> create(@PathVariable UUID projectId,
                                                 @Valid @RequestBody CreateSprintRequest request) {
        Sprint sprint = sprintService.create(projectId, request.name(), request.goal(),
                request.startDate(), request.endDate(), request.capacity());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(sprint));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sprint by ID")
    public ResponseEntity<SprintResponse> getById(@PathVariable UUID id) {
        Sprint sprint = sprintService.getById(id);
        return ResponseEntity.ok(toResponse(sprint));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get all sprints for a project")
    public ResponseEntity<List<SprintResponse>> getByProject(@PathVariable UUID projectId) {
        List<SprintResponse> sprints = sprintService.getByProjectId(projectId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(sprints);
    }

    @GetMapping("/project/{projectId}/active")
    @Operation(summary = "Get active sprint for a project")
    public ResponseEntity<SprintResponse> getActiveSprint(@PathVariable UUID projectId) {
        Sprint sprint = sprintService.getActiveSprint(projectId);
        return ResponseEntity.ok(toResponse(sprint));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a sprint")
    public ResponseEntity<SprintResponse> start(@PathVariable UUID id) {
        Sprint sprint = sprintService.start(id);
        return ResponseEntity.ok(toResponse(sprint));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete a sprint")
    public ResponseEntity<SprintResponse> complete(@PathVariable UUID id) {
        Sprint sprint = sprintService.complete(id);
        return ResponseEntity.ok(toResponse(sprint));
    }

    @PostMapping("/{id}/tickets/{ticketId}")
    @Operation(summary = "Add a ticket to a sprint")
    public ResponseEntity<Void> addTicket(@PathVariable UUID id, @PathVariable UUID ticketId) {
        sprintService.addTicketToSprint(id, ticketId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/tickets/{ticketId}")
    @Operation(summary = "Remove a ticket from a sprint")
    public ResponseEntity<Void> removeTicket(@PathVariable UUID id, @PathVariable UUID ticketId) {
        sprintService.removeTicketFromSprint(id, ticketId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tickets")
    @Operation(summary = "Get tickets in a sprint")
    public ResponseEntity<List<TicketResponse>> getTickets(@PathVariable UUID id) {
        List<TicketResponse> tickets = ticketRepository.findBySprintId(id).stream()
                .map(this::toTicketResponse).toList();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}/metrics")
    @Operation(summary = "Get sprint metrics (velocity, burndown data)")
    public ResponseEntity<SprintService.SprintMetrics> getMetrics(@PathVariable UUID id) {
        return ResponseEntity.ok(sprintService.getMetrics(id));
    }

    private SprintResponse toResponse(Sprint s) {
        List<Ticket> tickets = ticketRepository.findBySprintId(s.getId());
        long doneTickets = tickets.stream().filter(Ticket::isDone).count();
        Integer totalPoints = tickets.stream()
                .map(Ticket::getStoryPoints)
                .filter(sp -> sp != null)
                .mapToInt(Integer::intValue)
                .sum();

        return new SprintResponse(
                s.getId(), s.getProjectId(), s.getName(), s.getGoal(),
                s.getStatus() != null ? s.getStatus().name() : null,
                s.getStartDate(), s.getEndDate(), s.getCapacity(),
                tickets.size(), doneTickets, totalPoints, s.getCreatedAt());
    }

    private TicketResponse toTicketResponse(Ticket t) {
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
}
