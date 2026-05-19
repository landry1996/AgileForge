package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTicketRepository extends JpaRepository<TicketEntity, UUID> {

    Optional<TicketEntity> findByIdAndDeletedFalse(UUID id);

    Optional<TicketEntity> findByProjectIdAndNumberAndDeletedFalse(UUID projectId, long number);

    List<TicketEntity> findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(UUID projectId);

    List<TicketEntity> findByProjectIdAndStatusAndDeletedFalse(UUID projectId, String status);

    List<TicketEntity> findByProjectIdAndTypeAndDeletedFalse(UUID projectId, String type);

    List<TicketEntity> findByAssigneeIdAndDeletedFalseOrderByPriorityAscCreatedAtDesc(UUID assigneeId);

    List<TicketEntity> findBySprintIdAndDeletedFalse(UUID sprintId);

    List<TicketEntity> findByEpicIdAndDeletedFalse(UUID epicId);

    @Query("SELECT COALESCE(MAX(t.number), 0) + 1 FROM TicketEntity t WHERE t.projectId = :projectId")
    long getNextNumber(@Param("projectId") UUID projectId);

    long countByProjectIdAndDeletedFalse(UUID projectId);

    long countByProjectIdAndStatusAndDeletedFalse(UUID projectId, String status);

    List<TicketEntity> findByProjectIdAndStatusInAndDeletedFalse(UUID projectId, List<String> statuses);
}
