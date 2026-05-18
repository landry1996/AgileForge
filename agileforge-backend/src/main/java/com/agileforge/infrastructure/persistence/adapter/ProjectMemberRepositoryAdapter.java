package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.ProjectMember;
import com.agileforge.domain.port.out.ProjectMemberRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.ProjectMemberEntity;
import com.agileforge.infrastructure.persistence.repository.JpaProjectMemberRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProjectMemberRepositoryAdapter implements ProjectMemberRepositoryPort {

    private final JpaProjectMemberRepository repository;

    public ProjectMemberRepositoryAdapter(JpaProjectMemberRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProjectMember save(ProjectMember member) {
        ProjectMemberEntity entity = toEntity(member);
        ProjectMemberEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId) {
        return repository.findByProjectIdAndUserIdAndActiveTrue(projectId, userId)
                .map(this::toDomain);
    }

    @Override
    public List<ProjectMember> findByProjectId(UUID projectId) {
        return repository.findByProjectIdAndActiveTrue(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public boolean existsByProjectIdAndUserId(UUID projectId, UUID userId) {
        return repository.existsByProjectIdAndUserIdAndActiveTrue(projectId, userId);
    }

    @Override
    public void delete(UUID id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setActive(false);
            repository.save(entity);
        });
    }

    private ProjectMemberEntity toEntity(ProjectMember domain) {
        ProjectMemberEntity entity = new ProjectMemberEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setUserId(domain.getUserId());
        entity.setRoleId(domain.getRoleId());
        entity.setJoinedAt(domain.getJoinedAt());
        entity.setActive(domain.isActive());
        return entity;
    }

    private ProjectMember toDomain(ProjectMemberEntity entity) {
        ProjectMember domain = new ProjectMember();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setUserId(entity.getUserId());
        domain.setRoleId(entity.getRoleId());
        domain.setJoinedAt(entity.getJoinedAt());
        domain.setActive(entity.isActive());
        return domain;
    }
}
