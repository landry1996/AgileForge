package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Incident;
import com.agileforge.domain.model.IncidentEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepositoryPort {

    Incident save(Incident incident);

    Optional<Incident> findById(UUID id);

    List<Incident> findByProjectId(UUID projectId);

    List<Incident> findActiveByProjectId(UUID projectId);

    void delete(UUID id);

    IncidentEvent saveEvent(IncidentEvent event);

    List<IncidentEvent> findEventsByIncidentId(UUID incidentId);

    void addParticipant(UUID incidentId, UUID userId, String role);

    List<UUID> findParticipantIds(UUID incidentId);
}
