package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.KeyResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaKeyResultRepository extends JpaRepository<KeyResultEntity, UUID> {

    List<KeyResultEntity> findByObjectiveIdOrderByCreatedAtAsc(UUID objectiveId);
}
