package com.agileforge.domain.port.out;

import com.agileforge.domain.model.GitPullRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GitPullRequestPort {

    GitPullRequest save(GitPullRequest gitPullRequest);

    List<GitPullRequest> findByTicketId(UUID ticketId);

    List<GitPullRequest> findByRepositoryId(UUID repositoryId);

    Optional<GitPullRequest> findByRepositoryIdAndPrNumber(UUID repositoryId, int prNumber);
}
