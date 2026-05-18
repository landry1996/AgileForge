package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.AddMemberRequest;
import com.agileforge.application.dto.request.CreateProjectRequest;
import com.agileforge.application.dto.request.UpdateProjectRequest;
import com.agileforge.application.dto.response.ProjectResponse;
import com.agileforge.application.service.ProjectService;
import com.agileforge.domain.model.Project;
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
@RequestMapping("/projects")
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepositoryPort userRepository;

    public ProjectController(ProjectService projectService, UserRepositoryPort userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @PostMapping("/organization/{orgId}")
    @Operation(summary = "Create a new project in an organization")
    public ResponseEntity<ProjectResponse> create(@PathVariable UUID orgId,
                                                  @Valid @RequestBody CreateProjectRequest request,
                                                  Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Project project = projectService.create(orgId, request.name(), request.key(),
                request.description(), request.type(), request.visibility(),
                request.startDate(), request.endDate(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(project));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectResponse> getById(@PathVariable UUID id) {
        Project project = projectService.getById(id);
        return ResponseEntity.ok(toResponse(project));
    }

    @GetMapping("/organization/{orgId}")
    @Operation(summary = "Get all projects in an organization")
    public ResponseEntity<List<ProjectResponse>> getByOrganization(@PathVariable UUID orgId) {
        List<ProjectResponse> projects = projectService.getByOrganizationId(orgId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my projects")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        List<ProjectResponse> projects = projectService.getByUserId(userId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public ResponseEntity<ProjectResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateProjectRequest request,
                                                  Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Project project = projectService.update(id, request.name(), request.description(),
                request.type(), request.visibility(), request.status(),
                request.startDate(), request.endDate(), request.logoUrl(), userId);
        return ResponseEntity.ok(toResponse(project));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to project")
    public ResponseEntity<Void> addMember(@PathVariable UUID id,
                                          @Valid @RequestBody AddMemberRequest request,
                                          Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        projectService.addMember(id, request.userId(), request.roleCode(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "Remove member from project")
    public ResponseEntity<Void> removeMember(@PathVariable UUID id,
                                             @PathVariable UUID memberId,
                                             Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        projectService.removeMember(id, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(), project.getOrganizationId(), project.getName(), project.getKey(),
                project.getDescription(), project.getLogoUrl(),
                project.getType() != null ? project.getType().name() : null,
                project.getVisibility() != null ? project.getVisibility().name() : null,
                project.getStatus() != null ? project.getStatus().name() : null,
                project.getStartDate(), project.getEndDate(),
                project.getLeadId(), project.getCreatedAt());
    }
}
