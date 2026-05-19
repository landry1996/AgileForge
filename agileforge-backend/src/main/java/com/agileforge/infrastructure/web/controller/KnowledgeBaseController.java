package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateKnowledgeEntryRequest;
import com.agileforge.application.dto.request.UpdateKnowledgeEntryRequest;
import com.agileforge.application.dto.response.KnowledgeEntryResponse;
import com.agileforge.application.dto.response.ProjectContextResponse;
import com.agileforge.application.service.KnowledgeBaseService;
import com.agileforge.domain.model.KnowledgeCategory;
import com.agileforge.domain.model.KnowledgeEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Knowledge Base", description = "Project knowledge base and memory management")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping("/projects/{projectId}/knowledge")
    @Operation(summary = "Create a knowledge entry for a project")
    public ResponseEntity<KnowledgeEntryResponse> create(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID userId,
            @Valid @RequestBody CreateKnowledgeEntryRequest request) {
        KnowledgeEntry entry = knowledgeBaseService.create(projectId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entry));
    }

    @GetMapping("/projects/{projectId}/knowledge")
    @Operation(summary = "Get all knowledge entries for a project")
    public ResponseEntity<List<KnowledgeEntryResponse>> getByProject(@PathVariable UUID projectId) {
        List<KnowledgeEntryResponse> entries = knowledgeBaseService.getByProject(projectId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/projects/{projectId}/knowledge/category/{category}")
    @Operation(summary = "Get knowledge entries by category for a project")
    public ResponseEntity<List<KnowledgeEntryResponse>> getByCategory(
            @PathVariable UUID projectId,
            @PathVariable String category) {
        KnowledgeCategory knowledgeCategory = KnowledgeCategory.valueOf(category.toUpperCase());
        List<KnowledgeEntryResponse> entries = knowledgeBaseService.getByCategory(projectId, knowledgeCategory).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/projects/{projectId}/knowledge/context")
    @Operation(summary = "Get aggregated project context from knowledge base")
    public ResponseEntity<ProjectContextResponse> getProjectContext(@PathVariable UUID projectId) {
        ProjectContextResponse context = knowledgeBaseService.getProjectContext(projectId);
        return ResponseEntity.ok(context);
    }

    @PutMapping("/knowledge/{entryId}")
    @Operation(summary = "Update a knowledge entry")
    public ResponseEntity<KnowledgeEntryResponse> update(
            @PathVariable UUID entryId,
            @Valid @RequestBody UpdateKnowledgeEntryRequest request) {
        KnowledgeEntry entry = knowledgeBaseService.update(entryId, request);
        return ResponseEntity.ok(toResponse(entry));
    }

    @DeleteMapping("/knowledge/{entryId}")
    @Operation(summary = "Delete a knowledge entry")
    public ResponseEntity<Void> delete(@PathVariable UUID entryId) {
        knowledgeBaseService.delete(entryId);
        return ResponseEntity.noContent().build();
    }

    private KnowledgeEntryResponse toResponse(KnowledgeEntry entry) {
        return new KnowledgeEntryResponse(
                entry.getId(),
                entry.getProjectId(),
                entry.getCategory().name(),
                entry.getTitle(),
                entry.getContent(),
                entry.getTags(),
                entry.isActive(),
                entry.getCreatedBy(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
