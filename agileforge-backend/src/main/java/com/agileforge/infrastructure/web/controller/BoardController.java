package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.response.BoardResponse;
import com.agileforge.application.dto.response.TicketResponse;
import com.agileforge.application.service.BoardService;
import com.agileforge.domain.model.BoardColumn;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/board")
@Tag(name = "Board", description = "Kanban board management endpoints")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get board view for a project")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable UUID projectId) {
        BoardService.BoardData data = boardService.getBoard(projectId);

        List<BoardResponse.BoardColumnResponse> columnResponses = data.columns().stream()
                .map(col -> {
                    List<TicketResponse> colTickets = data.tickets().stream()
                            .filter(t -> t.getStatus() == col.getMappedStatus())
                            .map(this::toTicketResponse)
                            .toList();
                    return new BoardResponse.BoardColumnResponse(
                            col.getId(), col.getName(),
                            col.getMappedStatus().name(), col.getPosition(),
                            col.getWipLimit(), colTickets);
                })
                .toList();

        BoardResponse response = new BoardResponse(
                data.project().getId(),
                data.project().getName(),
                data.project().getKey(),
                data.activeSprint() != null ? data.activeSprint().getId() : null,
                data.activeSprint() != null ? data.activeSprint().getName() : null,
                columnResponses);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/project/{projectId}/columns")
    @Operation(summary = "Add a column to the board")
    public ResponseEntity<BoardColumnResponse> addColumn(@PathVariable UUID projectId,
                                                         @RequestBody AddColumnRequest request) {
        BoardColumn column = boardService.addColumn(projectId, request.name(),
                TicketStatus.valueOf(request.mappedStatus()), request.position(), request.wipLimit());
        return ResponseEntity.status(HttpStatus.CREATED).body(toColumnResponse(column));
    }

    @DeleteMapping("/columns/{columnId}")
    @Operation(summary = "Remove a column from the board")
    public ResponseEntity<Void> removeColumn(@PathVariable UUID columnId) {
        boardService.removeColumn(columnId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tickets/{ticketId}/move")
    @Operation(summary = "Move a ticket to a new status (drag & drop)")
    public ResponseEntity<Void> moveTicket(@PathVariable UUID ticketId,
                                           @RequestParam String status) {
        TicketStatus newStatus = TicketStatus.valueOf(status.toUpperCase());
        boardService.moveTicket(ticketId, newStatus);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/project/{projectId}/backlog")
    @Operation(summary = "Get backlog tickets (not in board)")
    public ResponseEntity<List<TicketResponse>> getBacklog(@PathVariable UUID projectId) {
        List<TicketResponse> tickets = boardService.getBacklog(projectId).stream()
                .map(this::toTicketResponse).toList();
        return ResponseEntity.ok(tickets);
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

    private BoardColumnResponse toColumnResponse(BoardColumn col) {
        return new BoardColumnResponse(col.getId(), col.getName(),
                col.getMappedStatus().name(), col.getPosition(), col.getWipLimit());
    }

    public record AddColumnRequest(String name, String mappedStatus, int position, Integer wipLimit) {}
    public record BoardColumnResponse(UUID id, String name, String mappedStatus, int position, Integer wipLimit) {}
}
