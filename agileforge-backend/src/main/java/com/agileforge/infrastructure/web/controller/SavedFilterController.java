package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateSavedFilterRequest;
import com.agileforge.application.dto.request.UpdateSavedFilterRequest;
import com.agileforge.application.dto.response.SavedFilterResponse;
import com.agileforge.application.service.SavedFilterService;
import com.agileforge.domain.model.SavedFilter;
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
@Tag(name = "Saved Filters", description = "Saved filter management endpoints")
public class SavedFilterController {

    private final SavedFilterService savedFilterService;
    private final UserRepositoryPort userRepository;

    public SavedFilterController(SavedFilterService savedFilterService, UserRepositoryPort userRepository) {
        this.savedFilterService = savedFilterService;
        this.userRepository = userRepository;
    }

    @PostMapping("/projects/{projectId}/filters")
    @Operation(summary = "Create a saved filter for a project")
    public ResponseEntity<SavedFilterResponse> create(@PathVariable UUID projectId,
                                                      @Valid @RequestBody CreateSavedFilterRequest request,
                                                      Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        SavedFilter filter = savedFilterService.create(projectId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(filter));
    }

    @GetMapping("/projects/{projectId}/filters")
    @Operation(summary = "Get saved filters for current user (own + shared)")
    public ResponseEntity<List<SavedFilterResponse>> getFilters(@PathVariable UUID projectId,
                                                                Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        List<SavedFilterResponse> filters = savedFilterService.getMyFilters(projectId, userId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(filters);
    }

    @PutMapping("/filters/{filterId}")
    @Operation(summary = "Update a saved filter")
    public ResponseEntity<SavedFilterResponse> update(@PathVariable UUID filterId,
                                                      @Valid @RequestBody UpdateSavedFilterRequest request,
                                                      Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        SavedFilter filter = savedFilterService.update(filterId, userId, request);
        return ResponseEntity.ok(toResponse(filter));
    }

    @DeleteMapping("/filters/{filterId}")
    @Operation(summary = "Delete a saved filter")
    public ResponseEntity<Void> delete(@PathVariable UUID filterId, Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        savedFilterService.delete(filterId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private SavedFilterResponse toResponse(SavedFilter filter) {
        return new SavedFilterResponse(
                filter.getId(),
                filter.getProjectId(),
                filter.getUserId(),
                filter.getName(),
                savedFilterService.fromJson(filter.getFilterConfig()),
                filter.isShared(),
                filter.getCreatedAt(),
                filter.getUpdatedAt()
        );
    }
}
