package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.GitPipeline;
import com.agileforge.domain.model.PipelineStatus;
import com.agileforge.domain.port.out.GitPipelinePort;
import com.agileforge.infrastructure.persistence.entity.GitPipelineEntity;
import com.agileforge.infrastructure.persistence.repository.JpaGitPipelineRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class GitPipelineAdapter implements GitPipelinePort {

    private final JpaGitPipelineRepository repository;

    public GitPipelineAdapter(JpaGitPipelineRepository repository) {
        this.repository = repository;
    }

    @Override
    public GitPipeline save(GitPipeline gitPipeline) {
        GitPipelineEntity entity = toEntity(gitPipeline);
        GitPipelineEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<GitPipeline> findByTicketId(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<GitPipeline> findByPrId(UUID prId) {
        return repository.findByPrIdOrderByCreatedAtDesc(prId).stream()
                .map(this::toDomain).toList();
    }

    private GitPipelineEntity toEntity(GitPipeline domain) {
        GitPipelineEntity entity = new GitPipelineEntity();
        entity.setId(domain.getId());
        entity.setRepositoryId(domain.getRepositoryId());
        entity.setTicketId(domain.getTicketId());
        entity.setPrId(domain.getPrId());
        entity.setPipelineId(domain.getPipelineId());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : PipelineStatus.PENDING.name());
        entity.setUrl(domain.getUrl());
        entity.setStartedAt(domain.getStartedAt());
        entity.setFinishedAt(domain.getFinishedAt());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        return entity;
    }

    private GitPipeline toDomain(GitPipelineEntity entity) {
        GitPipeline domain = new GitPipeline();
        domain.setId(entity.getId());
        domain.setRepositoryId(entity.getRepositoryId());
        domain.setTicketId(entity.getTicketId());
        domain.setPrId(entity.getPrId());
        domain.setPipelineId(entity.getPipelineId());
        domain.setStatus(PipelineStatus.valueOf(entity.getStatus()));
        domain.setUrl(entity.getUrl());
        domain.setStartedAt(entity.getStartedAt());
        domain.setFinishedAt(entity.getFinishedAt());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
