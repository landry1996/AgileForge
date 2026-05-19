package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class GitPipeline {

    private UUID id;
    private UUID repositoryId;
    private UUID ticketId;
    private UUID prId;
    private String pipelineId;
    private PipelineStatus status;
    private String url;
    private Instant startedAt;
    private Instant finishedAt;
    private Instant createdAt;

    public GitPipeline() {}

    public GitPipeline(UUID repositoryId, UUID ticketId, UUID prId, String pipelineId,
                       PipelineStatus status, String url) {
        this.repositoryId = repositoryId;
        this.ticketId = ticketId;
        this.prId = prId;
        this.pipelineId = pipelineId;
        this.status = status;
        this.url = url;
    }

    public boolean isCompleted() {
        return status == PipelineStatus.SUCCESS || status == PipelineStatus.FAILED || status == PipelineStatus.CANCELLED;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRepositoryId() { return repositoryId; }
    public void setRepositoryId(UUID repositoryId) { this.repositoryId = repositoryId; }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public UUID getPrId() { return prId; }
    public void setPrId(UUID prId) { this.prId = prId; }

    public String getPipelineId() { return pipelineId; }
    public void setPipelineId(String pipelineId) { this.pipelineId = pipelineId; }

    public PipelineStatus getStatus() { return status; }
    public void setStatus(PipelineStatus status) { this.status = status; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
