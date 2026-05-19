package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.GitBranch;
import com.agileforge.domain.port.out.GitBranchPort;
import com.agileforge.infrastructure.persistence.entity.GitBranchEntity;
import com.agileforge.infrastructure.persistence.repository.JpaGitBranchRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class GitBranchAdapter implements GitBranchPort {

    private final JpaGitBranchRepository repository;

    public GitBranchAdapter(JpaGitBranchRepository repository) {
        this.repository = repository;
    }

    @Override
    public GitBranch save(GitBranch gitBranch) {
        GitBranchEntity entity = toEntity(gitBranch);
        GitBranchEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<GitBranch> findByTicketId(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<GitBranch> findByRepositoryId(UUID repositoryId) {
        return repository.findByRepositoryIdOrderByCreatedAtDesc(repositoryId).stream()
                .map(this::toDomain).toList();
    }

    private GitBranchEntity toEntity(GitBranch domain) {
        GitBranchEntity entity = new GitBranchEntity();
        entity.setId(domain.getId());
        entity.setRepositoryId(domain.getRepositoryId());
        entity.setTicketId(domain.getTicketId());
        entity.setBranchName(domain.getBranchName());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        return entity;
    }

    private GitBranch toDomain(GitBranchEntity entity) {
        GitBranch domain = new GitBranch();
        domain.setId(entity.getId());
        domain.setRepositoryId(entity.getRepositoryId());
        domain.setTicketId(entity.getTicketId());
        domain.setBranchName(entity.getBranchName());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
