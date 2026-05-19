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
@Table(name = "git_branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitBranchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "branch_name", nullable = false, length = 255)
    private String branchName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
