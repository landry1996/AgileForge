package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.GitPullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaGitPullRequestRepository extends JpaRepository<GitPullRequestEntity, UUID> {

    List<GitPullRequestEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);

    List<GitPullRequestEntity> findByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);

    Optional<GitPullRequestEntity> findByRepositoryIdAndPrNumber(UUID repositoryId, int prNumber);
}
