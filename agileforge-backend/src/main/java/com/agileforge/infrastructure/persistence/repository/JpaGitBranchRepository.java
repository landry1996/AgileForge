package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.GitBranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaGitBranchRepository extends JpaRepository<GitBranchEntity, UUID> {

    List<GitBranchEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);

    List<GitBranchEntity> findByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);
}
