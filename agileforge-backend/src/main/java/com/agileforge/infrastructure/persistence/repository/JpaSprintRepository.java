package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.SprintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaSprintRepository extends JpaRepository<SprintEntity, UUID> {

    Optional<SprintEntity> findByIdAndDeletedFalse(UUID id);

    List<SprintEntity> findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(UUID projectId);

    Optional<SprintEntity> findByProjectIdAndStatusAndDeletedFalse(UUID projectId, String status);

    long countByProjectIdAndDeletedFalse(UUID projectId);

    List<SprintEntity> findByProjectIdAndStatusAndDeletedFalseOrderByEndDateDesc(UUID projectId, String status);
}
