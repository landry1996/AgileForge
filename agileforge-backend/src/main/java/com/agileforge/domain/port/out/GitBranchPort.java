package com.agileforge.domain.port.out;

import com.agileforge.domain.model.GitBranch;

import java.util.List;
import java.util.UUID;

public interface GitBranchPort {

    GitBranch save(GitBranch gitBranch);

    List<GitBranch> findByTicketId(UUID ticketId);

    List<GitBranch> findByRepositoryId(UUID repositoryId);
}
