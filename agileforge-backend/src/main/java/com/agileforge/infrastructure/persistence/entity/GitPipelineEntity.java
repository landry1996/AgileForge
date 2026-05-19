package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "git_pipelines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitPipelineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "pr_id")
    private UUID prId;

    @Column(name = "pipeline_id", length = 100)
    private String pipelineId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String url;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
