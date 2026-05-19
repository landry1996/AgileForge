package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateRoadmapItemRequest;
import com.agileforge.application.dto.request.UpdateRoadmapItemRequest;
import com.agileforge.application.dto.response.RoadmapItemResponse;
import com.agileforge.application.service.RoadmapService;
import com.agileforge.domain.model.RoadmapItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Roadmap", description = "Roadmap management endpoints")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @PostMapping("/projects/{projectId}/roadmap")
    @Operation(summary = "Create a new roadmap item")
    public ResponseEntity<RoadmapItemResponse> create(@PathVariable UUID projectId,
                                                      @Valid @RequestBody CreateRoadmapItemRequest request) {
        RoadmapItem item = roadmapService.create(projectId, request.title(), request.description(),
                request.category(), request.startDate(), request.endDate(), request.color(),
                request.position(), request.releaseId(), request.epicId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(item));
    }

    @GetMapping("/projects/{projectId}/roadmap")
    @Operation(summary = "Get all roadmap items for a project")
    public ResponseEntity<List<RoadmapItemResponse>> getByProject(@PathVariable UUID projectId) {
        List<RoadmapItemResponse> items = roadmapService.getByProject(projectId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(items);
    }

    @PutMapping("/roadmap/{itemId}")
    @Operation(summary = "Update a roadmap item")
    public ResponseEntity<RoadmapItemResponse> update(@PathVariable UUID itemId,
                                                      @Valid @RequestBody UpdateRoadmapItemRequest request) {
        RoadmapItem item = roadmapService.update(itemId, request.title(), request.description(),
                request.category(), request.status(), request.startDate(), request.endDate(),
                request.color(), request.position(), request.releaseId(), request.epicId());
        return ResponseEntity.ok(toResponse(item));
    }

    @DeleteMapping("/roadmap/{itemId}")
    @Operation(summary = "Delete a roadmap item")
    public ResponseEntity<Void> delete(@PathVariable UUID itemId) {
        roadmapService.delete(itemId);
        return ResponseEntity.noContent().build();
    }

    private RoadmapItemResponse toResponse(RoadmapItem item) {
        return new RoadmapItemResponse(
                item.getId(),
                item.getProjectId(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getStatus() != null ? item.getStatus().name() : null,
                item.getStartDate(),
                item.getEndDate(),
                item.getColor(),
                item.getPosition(),
                item.getReleaseId(),
                item.getEpicId(),
                item.getCreatedAt()
        );
    }
}
