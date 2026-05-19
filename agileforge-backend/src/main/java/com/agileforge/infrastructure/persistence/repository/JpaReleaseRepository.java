package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.ReleaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaReleaseRepository extends JpaRepository<ReleaseEntity, UUID> {

    Optional<ReleaseEntity> findByIdAndDeletedFalse(UUID id);

    List<ReleaseEntity> findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(UUID projectId);

    List<ReleaseEntity> findByProjectIdAndStatusAndDeletedFalse(UUID projectId, String status);
}
