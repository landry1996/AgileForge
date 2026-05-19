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
@Table(name = "git_pull_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitPullRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "pr_number", nullable = false)
    private int prNumber;

    @Column(length = 500)
    private String title;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 100)
    private String author;

    @Column(name = "source_branch", length = 255)
    private String sourceBranch;

    @Column(name = "target_branch", length = 255)
    private String targetBranch;

    @Column(length = 500)
    private String url;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "merged_at")
    private Instant mergedAt;

    @Column(name = "closed_at")
    private Instant closedAt;
}
