package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.SavedFilterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaSavedFilterRepository extends JpaRepository<SavedFilterEntity, UUID> {

    List<SavedFilterEntity> findByProjectIdAndUserId(UUID projectId, UUID userId);

    List<SavedFilterEntity> findByProjectIdAndIsSharedTrue(UUID projectId);
}
