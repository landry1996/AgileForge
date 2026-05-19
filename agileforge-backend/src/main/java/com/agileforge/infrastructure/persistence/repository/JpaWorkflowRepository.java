package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaWorkflowRepository extends JpaRepository<WorkflowEntity, UUID> {

    List<WorkflowEntity> findByProjectIdAndDeletedFalse(UUID projectId);

    Optional<WorkflowEntity> findByProjectIdAndTicketTypeAndDeletedFalse(UUID projectId, String ticketType);

    Optional<WorkflowEntity> findByIdAndDeletedFalse(UUID id);
}
