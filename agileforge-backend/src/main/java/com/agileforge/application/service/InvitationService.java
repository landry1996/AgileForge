package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Invitation;
import com.agileforge.domain.model.InvitationStatus;
import com.agileforge.domain.model.Organization;
import com.agileforge.domain.model.OrganizationMember;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.out.EmailPort;
import com.agileforge.domain.port.out.InvitationRepositoryPort;
import com.agileforge.domain.port.out.OrganizationMemberRepositoryPort;
import com.agileforge.domain.port.out.OrganizationRepositoryPort;
import com.agileforge.domain.port.out.UserRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.RoleEntity;
import com.agileforge.infrastructure.persistence.repository.JpaRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InvitationService {

    private static final Logger log = LoggerFactory.getLogger(InvitationService.class);

    private final InvitationRepositoryPort invitationRepository;
    private final OrganizationRepositoryPort organizationRepository;
    private final OrganizationMemberRepositoryPort memberRepository;
    private final UserRepositoryPort userRepository;
    private final EmailPort emailPort;
    private final JpaRoleRepository roleRepository;

    @Value("${agileforge.app.base-url:http://localhost:3000}")
    private String appBaseUrl;

    public InvitationService(InvitationRepositoryPort invitationRepository,
                             OrganizationRepositoryPort organizationRepository,
                             OrganizationMemberRepositoryPort memberRepository,
                             UserRepositoryPort userRepository,
                             EmailPort emailPort,
                             JpaRoleRepository roleRepository) {
        this.invitationRepository = invitationRepository;
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.emailPort = emailPort;
        this.roleRepository = roleRepository;
    }

    public Invitation invite(UUID orgId, String email, String role, UUID invitedByUserId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organization", orgId));

        User inviter = userRepository.findById(invitedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User", invitedByUserId));

        // Check if there's already a pending invitation for this email in this org
        invitationRepository.findPendingByOrganizationIdAndEmail(orgId, email)
                .ifPresent(existing -> {
                    throw new BusinessException("A pending invitation already exists for " + email + " in this organization");
                });

        // Check if user is already a member
        userRepository.findByEmail(email).ifPresent(user -> {
            if (memberRepository.existsByOrganizationIdAndUserId(orgId, user.getId())) {
                throw new BusinessException("User " + email + " is already a member of this organization");
            }
        });

        String effectiveRole = (role != null && !role.isBlank()) ? role : "DEVELOPER";
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        Invitation invitation = new Invitation(orgId, email, effectiveRole, token, invitedByUserId, expiresAt);
        Invitation saved = invitationRepository.save(invitation);

        String acceptUrl = appBaseUrl + "/invitations/accept?token=" + token;
        emailPort.sendInvitationEmail(email, org.getName(), inviter.getDisplayName(), acceptUrl);

        log.info("Invitation sent to {} for organization {} by {}", email, orgId, invitedByUserId);
        return saved;
    }

    public Invitation acceptInvitation(String token, UUID userId) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found with the provided token"));

        if (!invitation.isPending()) {
            throw new BusinessException("Invitation is no longer pending (status: " + invitation.getStatus() + ")");
        }

        if (invitation.isExpired()) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BusinessException("Invitation has expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        // Check if user is already a member
        if (memberRepository.existsByOrganizationIdAndUserId(invitation.getOrganizationId(), userId)) {
            throw new BusinessException("User is already a member of this organization");
        }

        // Add user to organization
        RoleEntity role = roleRepository.findByCode(invitation.getRole())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + invitation.getRole()));

        OrganizationMember member = new OrganizationMember(invitation.getOrganizationId(), userId, role.getId());
        memberRepository.save(member);

        // Mark invitation as accepted
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        Invitation updated = invitationRepository.save(invitation);

        log.info("Invitation accepted: user={} joined organization={}", userId, invitation.getOrganizationId());
        return updated;
    }

    public Invitation cancelInvitation(UUID invitationId, UUID userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation", invitationId));

        if (!invitation.isPending()) {
            throw new BusinessException("Only pending invitations can be cancelled");
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        Invitation updated = invitationRepository.save(invitation);

        log.info("Invitation {} cancelled by user {}", invitationId, userId);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<Invitation> getPendingInvitations(UUID orgId) {
        return invitationRepository.findByOrganizationId(orgId).stream()
                .filter(Invitation::isPending)
                .filter(inv -> !inv.isExpired())
                .toList();
    }

    public Invitation resendInvitation(UUID invitationId, UUID userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation", invitationId));

        if (!invitation.isPending()) {
            throw new BusinessException("Only pending invitations can be resent");
        }

        Organization org = organizationRepository.findById(invitation.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organization", invitation.getOrganizationId()));

        User inviter = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        // Regenerate token and reset expiry
        String newToken = UUID.randomUUID().toString();
        invitation.setToken(newToken);
        invitation.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        Invitation updated = invitationRepository.save(invitation);

        String acceptUrl = appBaseUrl + "/invitations/accept?token=" + newToken;
        emailPort.sendInvitationEmail(invitation.getEmail(), org.getName(), inviter.getDisplayName(), acceptUrl);

        log.info("Invitation {} resent to {} by user {}", invitationId, invitation.getEmail(), userId);
        return updated;
    }
}
