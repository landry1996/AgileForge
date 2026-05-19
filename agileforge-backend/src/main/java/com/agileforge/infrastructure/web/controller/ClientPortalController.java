package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.ConfigurePortalRequest;
import com.agileforge.application.dto.request.CreateClientFeedbackRequest;
import com.agileforge.application.dto.request.CreateClientUserRequest;
import com.agileforge.application.dto.response.ClientFeedbackResponse;
import com.agileforge.application.dto.response.ClientPortalResponse;
import com.agileforge.application.dto.response.ClientPortalViewResponse;
import com.agileforge.application.dto.response.ClientUserResponse;
import com.agileforge.application.service.ClientPortalService;
import com.agileforge.domain.model.ClientFeedback;
import com.agileforge.domain.model.ClientPortal;
import com.agileforge.domain.model.ClientUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Client Portal", description = "Client portal management endpoints")
public class ClientPortalController {

    private final ClientPortalService clientPortalService;

    public ClientPortalController(ClientPortalService clientPortalService) {
        this.clientPortalService = clientPortalService;
    }

    @PutMapping("/projects/{projectId}/portal")
    @Operation(summary = "Configure client portal for a project")
    public ResponseEntity<ClientPortalResponse> configurePortal(
            @PathVariable UUID projectId,
            @Valid @RequestBody ConfigurePortalRequest request) {
        ClientPortal portal = clientPortalService.configurePortal(projectId, request.isEnabled(),
                request.welcomeMessage(), request.allowedTicketTypes(),
                request.showRoadmap(), request.showReleases(), request.showChangelog());
        return ResponseEntity.ok(toPortalResponse(portal, projectId));
    }

    @GetMapping("/projects/{projectId}/portal")
    @Operation(summary = "Get client portal configuration")
    public ResponseEntity<ClientPortalResponse> getPortalConfig(@PathVariable UUID projectId) {
        ClientPortal portal = clientPortalService.getPortalConfig(projectId);
        return ResponseEntity.ok(toPortalResponse(portal, projectId));
    }

    @PostMapping("/projects/{projectId}/portal/users")
    @Operation(summary = "Add a client user to the portal")
    public ResponseEntity<ClientUserResponse> addClientUser(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateClientUserRequest request) {
        ClientUser user = clientPortalService.addClientUser(projectId, request.email(),
                request.name(), request.company());
        return ResponseEntity.status(HttpStatus.CREATED).body(toClientUserResponse(user));
    }

    @GetMapping("/projects/{projectId}/portal/users")
    @Operation(summary = "List client users for a portal")
    public ResponseEntity<List<ClientUserResponse>> getClientUsers(@PathVariable UUID projectId) {
        List<ClientUserResponse> users = clientPortalService.getClientUsers(projectId).stream()
                .map(this::toClientUserResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/portal/users/{clientUserId}")
    @Operation(summary = "Remove a client user")
    public ResponseEntity<Void> removeClientUser(@PathVariable UUID clientUserId) {
        clientPortalService.removeClientUser(clientUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/portal/{portalId}/feedback")
    @Operation(summary = "Submit client feedback")
    public ResponseEntity<ClientFeedbackResponse> submitFeedback(
            @PathVariable UUID portalId,
            @RequestParam UUID clientUserId,
            @Valid @RequestBody CreateClientFeedbackRequest request) {
        ClientFeedback feedback = clientPortalService.submitFeedback(portalId, clientUserId,
                request.ticketId(), request.type(), request.content(), request.rating());
        return ResponseEntity.status(HttpStatus.CREATED).body(toFeedbackResponse(feedback));
    }

    @GetMapping("/projects/{projectId}/portal/feedback")
    @Operation(summary = "Get all feedback for a project portal")
    public ResponseEntity<List<ClientFeedbackResponse>> getFeedback(@PathVariable UUID projectId) {
        List<ClientFeedbackResponse> feedbacks = clientPortalService.getFeedbackByProject(projectId).stream()
                .map(this::toFeedbackResponse)
                .toList();
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/projects/{projectId}/portal/view")
    @Operation(summary = "Get public client portal view")
    public ResponseEntity<ClientPortalViewResponse> getPortalView(@PathVariable UUID projectId) {
        ClientPortalViewResponse view = clientPortalService.getPortalView(projectId);
        return ResponseEntity.ok(view);
    }

    private ClientPortalResponse toPortalResponse(ClientPortal portal, UUID projectId) {
        List<ClientUser> users = clientPortalService.getClientUsers(projectId);
        return new ClientPortalResponse(
                portal.getId(),
                portal.getProjectId(),
                portal.isEnabled(),
                portal.getWelcomeMessage(),
                portal.getAllowedTicketTypes(),
                portal.isShowRoadmap(),
                portal.isShowReleases(),
                portal.isShowChangelog(),
                portal.getCustomBranding(),
                users.size(),
                portal.getCreatedAt(),
                portal.getUpdatedAt()
        );
    }

    private ClientUserResponse toClientUserResponse(ClientUser user) {
        return new ClientUserResponse(
                user.getId(),
                user.getPortalId(),
                user.getEmail(),
                user.getName(),
                user.getCompany(),
                user.isActive(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    private ClientFeedbackResponse toFeedbackResponse(ClientFeedback feedback) {
        return new ClientFeedbackResponse(
                feedback.getId(),
                feedback.getPortalId(),
                feedback.getTicketId(),
                feedback.getClientUserId(),
                null, // clientName would require additional lookup
                feedback.getType() != null ? feedback.getType().name() : null,
                feedback.getContent(),
                feedback.getRating(),
                feedback.getCreatedAt()
        );
    }
}
