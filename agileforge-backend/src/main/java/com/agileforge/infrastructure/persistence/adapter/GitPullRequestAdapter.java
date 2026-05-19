package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.GitPullRequest;
import com.agileforge.domain.model.PRStatus;
import com.agileforge.domain.port.out.GitPullRequestPort;
import com.agileforge.infrastructure.persistence.entity.GitPullRequestEntity;
import com.agileforge.infrastructure.persistence.repository.JpaGitPullRequestRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GitPullRequestAdapter implements GitPullRequestPort {

    private final JpaGitPullRequestRepository repository;

    public GitPullRequestAdapter(JpaGitPullRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public GitPullRequest save(GitPullRequest gitPullRequest) {
        GitPullRequestEntity entity = toEntity(gitPullRequest);
        GitPullRequestEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<GitPullRequest> findByTicketId(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<GitPullRequest> findByRepositoryId(UUID repositoryId) {
        return repository.findByRepositoryIdOrderByCreatedAtDesc(repositoryId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<GitPullRequest> findByRepositoryIdAndPrNumber(UUID repositoryId, int prNumber) {
        return repository.findByRepositoryIdAndPrNumber(repositoryId, prNumber).map(this::toDomain);
    }

    private GitPullRequestEntity toEntity(GitPullRequest domain) {
        GitPullRequestEntity entity = new GitPullRequestEntity();
        entity.setId(domain.getId());
        entity.setRepositoryId(domain.getRepositoryId());
        entity.setTicketId(domain.getTicketId());
        entity.setPrNumber(domain.getPrNumber());
        entity.setTitle(domain.getTitle());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : PRStatus.OPEN.name());
        entity.setAuthor(domain.getAuthor());
        entity.setSourceBranch(domain.getSourceBranch());
        entity.setTargetBranch(domain.getTargetBranch());
        entity.setUrl(domain.getUrl());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        entity.setMergedAt(domain.getMergedAt());
        entity.setClosedAt(domain.getClosedAt());
        return entity;
    }

    private GitPullRequest toDomain(GitPullRequestEntity entity) {
        GitPullRequest domain = new GitPullRequest();
        domain.setId(entity.getId());
        domain.setRepositoryId(entity.getRepositoryId());
        domain.setTicketId(entity.getTicketId());
        domain.setPrNumber(entity.getPrNumber());
        domain.setTitle(entity.getTitle());
        domain.setStatus(PRStatus.valueOf(entity.getStatus()));
        domain.setAuthor(entity.getAuthor());
        domain.setSourceBranch(entity.getSourceBranch());
        domain.setTargetBranch(entity.getTargetBranch());
        domain.setUrl(entity.getUrl());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setMergedAt(entity.getMergedAt());
        domain.setClosedAt(entity.getClosedAt());
        return domain;
    }
}
