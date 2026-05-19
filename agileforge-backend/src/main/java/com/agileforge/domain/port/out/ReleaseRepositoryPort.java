package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Release;
import com.agileforge.domain.model.ReleaseStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReleaseRepositoryPort {

    Release save(Release release);

    Optional<Release> findById(UUID id);

    List<Release> findByProjectId(UUID projectId);

    List<Release> findByProjectIdAndStatus(UUID projectId, ReleaseStatus status);

    void delete(UUID id);

    void addTicketToRelease(UUID releaseId, UUID ticketId);

    void removeTicketFromRelease(UUID releaseId, UUID ticketId);

    List<UUID> findTicketIdsByReleaseId(UUID releaseId);
}
