package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.GitRepository;
import com.agileforge.domain.port.out.GitRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.GitRepositoryEntity;
import com.agileforge.infrastructure.persistence.repository.JpaGitRepositoryRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GitRepositoryAdapter implements GitRepositoryPort {

    private final JpaGitRepositoryRepository repository;

    public GitRepositoryAdapter(JpaGitRepositoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public GitRepository save(GitRepository gitRepository) {
        GitRepositoryEntity entity = toEntity(gitRepository);
        GitRepositoryEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<GitRepository> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<GitRepository> findByProjectId(UUID projectId) {
        return repository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private GitRepositoryEntity toEntity(GitRepository domain) {
        GitRepositoryEntity entity = new GitRepositoryEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setProvider(domain.getProvider());
        entity.setOwner(domain.getOwner());
        entity.setRepoName(domain.getRepoName());
        entity.setDefaultBranch(domain.getDefaultBranch());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        return entity;
    }

    private GitRepository toDomain(GitRepositoryEntity entity) {
        GitRepository domain = new GitRepository();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setProvider(entity.getProvider());
        domain.setOwner(entity.getOwner());
        domain.setRepoName(entity.getRepoName());
        domain.setDefaultBranch(entity.getDefaultBranch());
        domain.setActive(entity.isActive());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
