package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.WorkflowTransitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaWorkflowTransitionRepository extends JpaRepository<WorkflowTransitionEntity, UUID> {

    List<WorkflowTransitionEntity> findByWorkflowId(UUID workflowId);

    void deleteByWorkflowId(UUID workflowId);
}
