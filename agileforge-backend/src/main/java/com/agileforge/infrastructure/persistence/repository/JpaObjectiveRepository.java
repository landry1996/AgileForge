package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.ObjectiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaObjectiveRepository extends JpaRepository<ObjectiveEntity, UUID> {

    Optional<ObjectiveEntity> findByIdAndDeletedFalse(UUID id);

    List<ObjectiveEntity> findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(UUID projectId);
}
