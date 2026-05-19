package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.WorkflowStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaWorkflowStatusRepository extends JpaRepository<WorkflowStatusEntity, UUID> {

    List<WorkflowStatusEntity> findByWorkflowIdOrderByPositionAsc(UUID workflowId);

    void deleteByWorkflowId(UUID workflowId);
}
