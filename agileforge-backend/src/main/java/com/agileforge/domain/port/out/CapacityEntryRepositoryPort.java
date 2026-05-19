package com.agileforge.domain.port.out;

import com.agileforge.domain.model.CapacityEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CapacityEntryRepositoryPort {

    CapacityEntry save(CapacityEntry entry);

    Optional<CapacityEntry> findById(UUID id);

    List<CapacityEntry> findByProjectIdAndSprintId(UUID projectId, UUID sprintId);

    List<CapacityEntry> findByUserId(UUID userId);

    List<CapacityEntry> findByProjectId(UUID projectId);

    void delete(UUID id);

    double sumAvailableHoursByProjectAndSprint(UUID projectId, UUID sprintId);
}
