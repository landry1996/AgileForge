package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.response.SearchResponse;
import com.agileforge.application.dto.response.TicketResponse;
import com.agileforge.application.service.SearchService;
import com.agileforge.domain.model.SearchResult;
import com.agileforge.domain.model.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/search")
@Tag(name = "Search", description = "Full-text search endpoints")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/tickets")
    @Operation(summary = "Search tickets with full-text search and filters")
    public ResponseEntity<SearchResponse<TicketResponse>> searchTickets(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SearchResult<Ticket> result = searchService.searchTickets(
                projectId, q, status, type, priority, assigneeId, page, size);

        List<TicketResponse> items = result.getItems().stream().map(this::toResponse).toList();
        SearchResponse<TicketResponse> response = new SearchResponse<>(
                items, result.getTotalCount(), result.getPage(),
                result.getPageSize(), result.getTotalPages());

        return ResponseEntity.ok(response);
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
}
