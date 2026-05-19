package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.GitPipelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaGitPipelineRepository extends JpaRepository<GitPipelineEntity, UUID> {

    List<GitPipelineEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);

    List<GitPipelineEntity> findByPrIdOrderByCreatedAtDesc(UUID prId);
}
