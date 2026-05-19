package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateReleaseRequest;
import com.agileforge.application.dto.request.UpdateReleaseRequest;
import com.agileforge.application.dto.response.ReleaseReadinessResponse;
import com.agileforge.application.dto.response.ReleaseResponse;
import com.agileforge.application.service.ReleaseService;
import com.agileforge.domain.model.Release;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Releases", description = "Release management endpoints")
public class ReleaseController {

    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @PostMapping("/projects/{projectId}/releases")
    @Operation(summary = "Create a new release")
    public ResponseEntity<ReleaseResponse> create(@PathVariable UUID projectId,
                                                  @Valid @RequestBody CreateReleaseRequest request) {
        Release release = releaseService.createRelease(projectId, request.name(), request.version(),
                request.description(), request.startDate(), request.releaseDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(release));
    }

    @GetMapping("/projects/{projectId}/releases")
    @Operation(summary = "Get all releases for a project")
    public ResponseEntity<List<ReleaseResponse>> getByProject(@PathVariable UUID projectId) {
        List<ReleaseResponse> releases = releaseService.getByProject(projectId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(releases);
    }

    @GetMapping("/releases/{releaseId}")
    @Operation(summary = "Get release by ID")
    public ResponseEntity<ReleaseResponse> getById(@PathVariable UUID releaseId) {
        Release release = releaseService.getById(releaseId);
        return ResponseEntity.ok(toResponse(release));
    }

    @PutMapping("/releases/{releaseId}")
    @Operation(summary = "Update a release")
    public ResponseEntity<ReleaseResponse> update(@PathVariable UUID releaseId,
                                                  @Valid @RequestBody UpdateReleaseRequest request) {
        Release release = releaseService.updateRelease(releaseId, request.name(), request.version(),
                request.description(), request.status(), request.startDate(), request.releaseDate());
        return ResponseEntity.ok(toResponse(release));
    }

    @DeleteMapping("/releases/{releaseId}")
    @Operation(summary = "Delete a release")
    public ResponseEntity<Void> delete(@PathVariable UUID releaseId) {
        releaseService.deleteRelease(releaseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/releases/{releaseId}/tickets/{ticketId}")
    @Operation(summary = "Add a ticket to a release")
    public ResponseEntity<Void> addTicket(@PathVariable UUID releaseId, @PathVariable UUID ticketId) {
        releaseService.addTicketToRelease(releaseId, ticketId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/releases/{releaseId}/tickets/{ticketId}")
    @Operation(summary = "Remove a ticket from a release")
    public ResponseEntity<Void> removeTicket(@PathVariable UUID releaseId, @PathVariable UUID ticketId) {
        releaseService.removeTicketFromRelease(releaseId, ticketId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/releases/{releaseId}/release")
    @Operation(summary = "Mark a release as released")
    public ResponseEntity<ReleaseResponse> releaseNow(@PathVariable UUID releaseId) {
        Release release = releaseService.releaseNow(releaseId);
        return ResponseEntity.ok(toResponse(release));
    }

    @GetMapping("/releases/{releaseId}/readiness")
    @Operation(summary = "Get release readiness report")
    public ResponseEntity<ReleaseReadinessResponse> getReadiness(@PathVariable UUID releaseId) {
        ReleaseReadinessResponse report = releaseService.getReadinessReport(releaseId);
        return ResponseEntity.ok(report);
    }

    private ReleaseResponse toResponse(Release r) {
        int ticketCount = releaseService.getTicketCount(r.getId());
        int completedCount = releaseService.getCompletedCount(r.getId());
        int progress = ticketCount > 0 ? (completedCount * 100) / ticketCount : 0;

        return new ReleaseResponse(
                r.getId(),
                r.getProjectId(),
                r.getName(),
                r.getVersion(),
                r.getDescription(),
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getStartDate(),
                r.getReleaseDate(),
                r.getReleasedAt(),
                ticketCount,
                completedCount,
                progress,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
