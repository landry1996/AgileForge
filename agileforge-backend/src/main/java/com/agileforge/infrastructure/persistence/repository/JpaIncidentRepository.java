package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaIncidentRepository extends JpaRepository<IncidentEntity, UUID> {

    Optional<IncidentEntity> findByIdAndDeletedFalse(UUID id);

    List<IncidentEntity> findByProjectIdAndDeletedFalse(UUID projectId);

    List<IncidentEntity> findByProjectIdAndStatusNotAndDeletedFalse(UUID projectId, String status);
}
