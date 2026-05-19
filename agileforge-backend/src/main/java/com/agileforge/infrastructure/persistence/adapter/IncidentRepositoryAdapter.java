package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Incident;
import com.agileforge.domain.model.IncidentEvent;
import com.agileforge.domain.model.IncidentEventType;
import com.agileforge.domain.model.IncidentSeverity;
import com.agileforge.domain.model.IncidentStatus;
import com.agileforge.domain.port.out.IncidentRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.IncidentEntity;
import com.agileforge.infrastructure.persistence.entity.IncidentEventEntity;
import com.agileforge.infrastructure.persistence.entity.IncidentParticipantEntity;
import com.agileforge.infrastructure.persistence.repository.JpaIncidentEventRepository;
import com.agileforge.infrastructure.persistence.repository.JpaIncidentParticipantRepository;
import com.agileforge.infrastructure.persistence.repository.JpaIncidentRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class IncidentRepositoryAdapter implements IncidentRepositoryPort {

    private final JpaIncidentRepository incidentRepository;
    private final JpaIncidentEventRepository eventRepository;
    private final JpaIncidentParticipantRepository participantRepository;

    public IncidentRepositoryAdapter(JpaIncidentRepository incidentRepository,
                                     JpaIncidentEventRepository eventRepository,
                                     JpaIncidentParticipantRepository participantRepository) {
        this.incidentRepository = incidentRepository;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
    }

    @Override
    public Incident save(Incident incident) {
        IncidentEntity entity = toEntity(incident);
        IncidentEntity saved = incidentRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Incident> findById(UUID id) {
        return incidentRepository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public List<Incident> findByProjectId(UUID projectId) {
        return incidentRepository.findByProjectIdAndDeletedFalse(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Incident> findActiveByProjectId(UUID projectId) {
        return incidentRepository.findByProjectIdAndStatusNotAndDeletedFalse(projectId, "RESOLVED").stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        incidentRepository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            incidentRepository.save(entity);
        });
    }

    @Override
    public IncidentEvent saveEvent(IncidentEvent event) {
        IncidentEventEntity entity = toEventEntity(event);
        IncidentEventEntity saved = eventRepository.save(entity);
        return toEventDomain(saved);
    }

    @Override
    public List<IncidentEvent> findEventsByIncidentId(UUID incidentId) {
        return eventRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId).stream()
                .map(this::toEventDomain).toList();
    }

    @Override
    public void addParticipant(UUID incidentId, UUID userId, String role) {
        IncidentParticipantEntity entity = new IncidentParticipantEntity();
        entity.setIncidentId(incidentId);
        entity.setUserId(userId);
        entity.setRole(role);
        entity.setJoinedAt(Instant.now());
        participantRepository.save(entity);
    }

    @Override
    public List<UUID> findParticipantIds(UUID incidentId) {
        return participantRepository.findByIncidentId(incidentId).stream()
                .map(IncidentParticipantEntity::getUserId).toList();
    }

    private IncidentEntity toEntity(Incident domain) {
        IncidentEntity entity = new IncidentEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setSeverity(domain.getSeverity() != null ? domain.getSeverity().name() : "HIGH");
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : "DETECTED");
        entity.setCommanderId(domain.getCommanderId());
        entity.setStartedAt(domain.getStartedAt() != null ? domain.getStartedAt() : Instant.now());
        entity.setResolvedAt(domain.getResolvedAt());
        entity.setRootCause(domain.getRootCause());
        entity.setResolution(domain.getResolution());
        entity.setPostMortem(domain.getPostMortem());
        return entity;
    }

    private Incident toDomain(IncidentEntity entity) {
        Incident domain = new Incident();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setTitle(entity.getTitle());
        domain.setDescription(entity.getDescription());
        domain.setSeverity(IncidentSeverity.valueOf(entity.getSeverity()));
        domain.setStatus(IncidentStatus.valueOf(entity.getStatus()));
        domain.setCommanderId(entity.getCommanderId());
        domain.setStartedAt(entity.getStartedAt());
        domain.setResolvedAt(entity.getResolvedAt());
        domain.setRootCause(entity.getRootCause());
        domain.setResolution(entity.getResolution());
        domain.setPostMortem(entity.getPostMortem());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    private IncidentEventEntity toEventEntity(IncidentEvent domain) {
        IncidentEventEntity entity = new IncidentEventEntity();
        entity.setId(domain.getId());
        entity.setIncidentId(domain.getIncidentId());
        entity.setUserId(domain.getUserId());
        entity.setEventType(domain.getEventType() != null ? domain.getEventType().name() : "UPDATE");
        entity.setMessage(domain.getMessage());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        return entity;
    }

    private IncidentEvent toEventDomain(IncidentEventEntity entity) {
        IncidentEvent domain = new IncidentEvent();
        domain.setId(entity.getId());
        domain.setIncidentId(entity.getIncidentId());
        domain.setUserId(entity.getUserId());
        domain.setEventType(IncidentEventType.valueOf(entity.getEventType()));
        domain.setMessage(entity.getMessage());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
