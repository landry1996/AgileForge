package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.CapacityEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaCapacityEntryRepository extends JpaRepository<CapacityEntryEntity, UUID> {

    List<CapacityEntryEntity> findByProjectIdAndSprintId(UUID projectId, UUID sprintId);

    List<CapacityEntryEntity> findByUserId(UUID userId);

    List<CapacityEntryEntity> findByProjectId(UUID projectId);

    @Query("SELECT COALESCE(SUM(ce.availableHours), 0) FROM CapacityEntryEntity ce WHERE ce.projectId = :projectId AND ce.sprintId = :sprintId")
    double sumAvailableHoursByProjectIdAndSprintId(@Param("projectId") UUID projectId, @Param("sprintId") UUID sprintId);
}
