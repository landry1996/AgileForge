package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.AcceptInvitationRequest;
import com.agileforge.application.dto.request.InviteRequest;
import com.agileforge.application.dto.response.InvitationResponse;
import com.agileforge.application.service.InvitationService;
import com.agileforge.domain.model.Invitation;
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
@Tag(name = "Invitations", description = "Invitation management endpoints")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserRepositoryPort userRepository;

    public InvitationController(InvitationService invitationService, UserRepositoryPort userRepository) {
        this.invitationService = invitationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/organizations/{orgId}/invitations")
    @Operation(summary = "Invite a member to the organization")
    public ResponseEntity<InvitationResponse> invite(@PathVariable UUID orgId,
                                                     @Valid @RequestBody InviteRequest request,
                                                     Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Invitation invitation = invitationService.invite(orgId, request.email(), request.role(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(invitation));
    }

    @GetMapping("/organizations/{orgId}/invitations")
    @Operation(summary = "List pending invitations for an organization")
    public ResponseEntity<List<InvitationResponse>> getPendingInvitations(@PathVariable UUID orgId,
                                                                          Authentication auth) {
        List<InvitationResponse> invitations = invitationService.getPendingInvitations(orgId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(invitations);
    }

    @PostMapping("/invitations/accept")
    @Operation(summary = "Accept an invitation by token")
    public ResponseEntity<InvitationResponse> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request,
                                                               Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Invitation invitation = invitationService.acceptInvitation(request.token(), userId);
        return ResponseEntity.ok(toResponse(invitation));
    }

    @DeleteMapping("/invitations/{invitationId}")
    @Operation(summary = "Cancel an invitation")
    public ResponseEntity<Void> cancelInvitation(@PathVariable UUID invitationId,
                                                 Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        invitationService.cancelInvitation(invitationId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/{invitationId}/resend")
    @Operation(summary = "Resend an invitation")
    public ResponseEntity<InvitationResponse> resendInvitation(@PathVariable UUID invitationId,
                                                               Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Invitation invitation = invitationService.resendInvitation(invitationId, userId);
        return ResponseEntity.ok(toResponse(invitation));
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private InvitationResponse toResponse(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getOrganizationId(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus().name(),
                invitation.getInvitedBy(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt()
        );
    }
}
