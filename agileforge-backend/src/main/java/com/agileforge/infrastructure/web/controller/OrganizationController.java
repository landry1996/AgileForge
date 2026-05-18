package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.AddMemberRequest;
import com.agileforge.application.dto.request.CreateOrganizationRequest;
import com.agileforge.application.dto.request.UpdateOrganizationRequest;
import com.agileforge.application.dto.response.OrganizationResponse;
import com.agileforge.application.service.OrganizationService;
import com.agileforge.domain.model.Organization;
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
@RequestMapping("/organizations")
@Tag(name = "Organizations", description = "Organization management endpoints")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final UserRepositoryPort userRepository;

    public OrganizationController(OrganizationService organizationService, UserRepositoryPort userRepository) {
        this.organizationService = organizationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new organization")
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest request,
                                                       Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Organization org = organizationService.create(
                request.name(), request.slug(), request.description(), request.website(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(org));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable UUID id) {
        Organization org = organizationService.getById(id);
        return ResponseEntity.ok(toResponse(org));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get organization by slug")
    public ResponseEntity<OrganizationResponse> getBySlug(@PathVariable String slug) {
        Organization org = organizationService.getBySlug(slug);
        return ResponseEntity.ok(toResponse(org));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my organizations")
    public ResponseEntity<List<OrganizationResponse>> getMyOrganizations(Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        List<OrganizationResponse> orgs = organizationService.getByUserId(userId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(orgs);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization")
    public ResponseEntity<OrganizationResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody UpdateOrganizationRequest request,
                                                       Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Organization org = organizationService.update(id, request.name(), request.description(),
                request.website(), request.logoUrl(), userId);
        return ResponseEntity.ok(toResponse(org));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to organization")
    public ResponseEntity<Void> addMember(@PathVariable UUID id,
                                          @Valid @RequestBody AddMemberRequest request,
                                          Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        organizationService.addMember(id, request.userId(), request.roleCode(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "Remove member from organization")
    public ResponseEntity<Void> removeMember(@PathVariable UUID id,
                                             @PathVariable UUID memberId,
                                             Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        organizationService.removeMember(id, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private OrganizationResponse toResponse(Organization org) {
        return new OrganizationResponse(
                org.getId(), org.getName(), org.getSlug(), org.getDescription(),
                org.getLogoUrl(), org.getWebsite(), org.getPlan(),
                org.getMaxUsers(), org.getMaxProjects(), org.isActive(), org.getCreatedAt());
    }
}
