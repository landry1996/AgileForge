package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Sprint;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SprintRepositoryPort {

    Sprint save(Sprint sprint);

    Optional<Sprint> findById(UUID id);

    List<Sprint> findByProjectId(UUID projectId);

    Optional<Sprint> findActiveByProjectId(UUID projectId);

    long countByProjectId(UUID projectId);

    List<Sprint> findCompletedByProjectId(UUID projectId);
}
