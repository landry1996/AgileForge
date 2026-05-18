package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Project;
import com.agileforge.domain.port.out.ProjectRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.ProjectEntity;
import com.agileforge.infrastructure.persistence.repository.JpaProjectRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProjectRepositoryAdapter implements ProjectRepositoryPort {

    private final JpaProjectRepository repository;

    public ProjectRepositoryAdapter(JpaProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public Project save(Project project) {
        ProjectEntity entity = toEntity(project);
        ProjectEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return repository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public Optional<Project> findByOrganizationIdAndKey(UUID organizationId, String key) {
        return repository.findByOrganizationIdAndKeyAndDeletedFalse(organizationId, key).map(this::toDomain);
    }

    @Override
    public List<Project> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationIdAndDeletedFalse(organizationId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Project> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByOrganizationIdAndKey(UUID organizationId, String key) {
        return repository.existsByOrganizationIdAndKeyAndDeletedFalse(organizationId, key);
    }

    @Override
    public boolean existsByOrganizationIdAndName(UUID organizationId, String name) {
        return repository.existsByOrganizationIdAndNameAndDeletedFalse(organizationId, name);
    }

    private ProjectEntity toEntity(Project domain) {
        ProjectEntity entity = new ProjectEntity();
        entity.setId(domain.getId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setName(domain.getName());
        entity.setKey(domain.getKey());
        entity.setDescription(domain.getDescription());
        entity.setLogoUrl(domain.getLogoUrl());
        entity.setType(domain.getType() != null ? domain.getType().name() : "SOFTWARE");
        entity.setVisibility(domain.getVisibility() != null ? domain.getVisibility().name() : "PRIVATE");
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : "ACTIVE");
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setLeadId(domain.getLeadId());
        return entity;
    }

    private Project toDomain(ProjectEntity entity) {
        Project domain = new Project();
        domain.setId(entity.getId());
        domain.setOrganizationId(entity.getOrganizationId());
        domain.setName(entity.getName());
        domain.setKey(entity.getKey());
        domain.setDescription(entity.getDescription());
        domain.setLogoUrl(entity.getLogoUrl());
        domain.setType(Project.ProjectType.valueOf(entity.getType()));
        domain.setVisibility(Project.ProjectVisibility.valueOf(entity.getVisibility()));
        domain.setStatus(Project.ProjectStatus.valueOf(entity.getStatus()));
        domain.setStartDate(entity.getStartDate());
        domain.setEndDate(entity.getEndDate());
        domain.setLeadId(entity.getLeadId());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
