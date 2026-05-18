package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Organization;
import com.agileforge.domain.port.out.OrganizationRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.OrganizationEntity;
import com.agileforge.infrastructure.persistence.repository.JpaOrganizationMemberRepository;
import com.agileforge.infrastructure.persistence.repository.JpaOrganizationRepository;
import com.agileforge.infrastructure.persistence.repository.JpaProjectRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrganizationRepositoryAdapter implements OrganizationRepositoryPort {

    private final JpaOrganizationRepository repository;
    private final JpaOrganizationMemberRepository memberRepository;
    private final JpaProjectRepository projectRepository;

    public OrganizationRepositoryAdapter(JpaOrganizationRepository repository,
                                         JpaOrganizationMemberRepository memberRepository,
                                         JpaProjectRepository projectRepository) {
        this.repository = repository;
        this.memberRepository = memberRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public Organization save(Organization organization) {
        OrganizationEntity entity = toEntity(organization);
        OrganizationEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return repository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public Optional<Organization> findBySlug(String slug) {
        return repository.findBySlugAndDeletedFalse(slug).map(this::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlugAndDeletedFalse(slug);
    }

    @Override
    public List<Organization> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public int countMembersByOrganizationId(UUID organizationId) {
        return memberRepository.countByOrganizationIdAndActiveTrue(organizationId);
    }

    @Override
    public int countProjectsByOrganizationId(UUID organizationId) {
        return projectRepository.countByOrganizationIdAndDeletedFalse(organizationId);
    }

    private OrganizationEntity toEntity(Organization domain) {
        OrganizationEntity entity = new OrganizationEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setSlug(domain.getSlug());
        entity.setDescription(domain.getDescription());
        entity.setLogoUrl(domain.getLogoUrl());
        entity.setWebsite(domain.getWebsite());
        entity.setPlan(domain.getPlan());
        entity.setMaxUsers(domain.getMaxUsers());
        entity.setMaxProjects(domain.getMaxProjects());
        entity.setActive(domain.isActive());
        return entity;
    }

    private Organization toDomain(OrganizationEntity entity) {
        Organization domain = new Organization();
        domain.setId(entity.getId());
        domain.setName(entity.getName());
        domain.setSlug(entity.getSlug());
        domain.setDescription(entity.getDescription());
        domain.setLogoUrl(entity.getLogoUrl());
        domain.setWebsite(entity.getWebsite());
        domain.setPlan(entity.getPlan());
        domain.setMaxUsers(entity.getMaxUsers());
        domain.setMaxProjects(entity.getMaxProjects());
        domain.setActive(entity.isActive());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
