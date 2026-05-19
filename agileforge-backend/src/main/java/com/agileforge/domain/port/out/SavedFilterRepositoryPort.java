package com.agileforge.domain.port.out;

import com.agileforge.domain.model.SavedFilter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedFilterRepositoryPort {

    SavedFilter save(SavedFilter savedFilter);

    Optional<SavedFilter> findById(UUID id);

    List<SavedFilter> findByProjectIdAndUserId(UUID projectId, UUID userId);

    List<SavedFilter> findSharedByProjectId(UUID projectId);

    void delete(UUID id);
}
