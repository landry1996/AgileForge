package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.exception.ForbiddenException;
import com.agileforge.domain.model.Organization;
import com.agileforge.domain.model.OrganizationMember;
import com.agileforge.domain.port.out.OrganizationMemberRepositoryPort;
import com.agileforge.domain.port.out.OrganizationRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.RoleEntity;
import com.agileforge.infrastructure.persistence.repository.JpaRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationService.class);

    private final OrganizationRepositoryPort organizationRepository;
    private final OrganizationMemberRepositoryPort memberRepository;
    private final JpaRoleRepository roleRepository;

    public OrganizationService(OrganizationRepositoryPort organizationRepository,
                               OrganizationMemberRepositoryPort memberRepository,
                               JpaRoleRepository roleRepository) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
    }

    public Organization create(String name, String slug, String description, String website, UUID creatorId) {
        if (organizationRepository.existsBySlug(slug)) {
            throw new BusinessException("Organization slug already taken: " + slug);
        }

        Organization org = new Organization(name, slug, description);
        org.setWebsite(website);
        Organization saved = organizationRepository.save(org);

        RoleEntity adminRole = roleRepository.findByCode("ORG_ADMIN")
                .orElseThrow(() -> new BusinessException("Default role ORG_ADMIN not found"));

        OrganizationMember member = new OrganizationMember(saved.getId(), creatorId, adminRole.getId());
        memberRepository.save(member);

        log.info("Organization created: {} ({})", name, slug);
        return saved;
    }

    @Transactional(readOnly = true)
    public Organization getById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization", id));
    }

    @Transactional(readOnly = true)
    public Organization getBySlug(String slug) {
        return organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + slug));
    }

    @Transactional(readOnly = true)
    public List<Organization> getByUserId(UUID userId) {
        return organizationRepository.findByUserId(userId);
    }

    public Organization update(UUID id, String name, String description, String website, String logoUrl, UUID requesterId) {
        Organization org = getById(id);
        verifyMembership(id, requesterId);

        if (name != null) org.setName(name);
        if (description != null) org.setDescription(description);
        if (website != null) org.setWebsite(website);
        if (logoUrl != null) org.setLogoUrl(logoUrl);

        return organizationRepository.save(org);
    }

    public void addMember(UUID organizationId, UUID userId, String roleCode, UUID requesterId) {
        Organization org = getById(organizationId);
        verifyMembership(organizationId, requesterId);

        if (memberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new BusinessException("User is already a member of this organization");
        }

        int currentMembers = organizationRepository.countMembersByOrganizationId(organizationId);
        if (!org.canAddUser(currentMembers)) {
            throw new BusinessException("Organization has reached maximum members limit (" + org.getMaxUsers() + ")");
        }

        RoleEntity role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleCode));

        OrganizationMember member = new OrganizationMember(organizationId, userId, role.getId());
        memberRepository.save(member);

        log.info("Member added to organization {}: user={}, role={}", organizationId, userId, roleCode);
    }

    public void removeMember(UUID organizationId, UUID userId, UUID requesterId) {
        verifyMembership(organizationId, requesterId);

        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found in organization"));

        memberRepository.delete(member.getId());
        log.info("Member removed from organization {}: user={}", organizationId, userId);
    }

    @Transactional(readOnly = true)
    public List<OrganizationMember> getMembers(UUID organizationId) {
        return memberRepository.findByOrganizationId(organizationId);
    }

    private void verifyMembership(UUID organizationId, UUID userId) {
        if (!memberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new ForbiddenException("You are not a member of this organization");
        }
    }
}
