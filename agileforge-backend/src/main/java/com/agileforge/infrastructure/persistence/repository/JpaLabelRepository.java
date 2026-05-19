package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.LabelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaLabelRepository extends JpaRepository<LabelEntity, UUID> {

    List<LabelEntity> findByProjectIdOrderByNameAsc(UUID projectId);

    Optional<LabelEntity> findByProjectIdAndName(UUID projectId, String name);
}
