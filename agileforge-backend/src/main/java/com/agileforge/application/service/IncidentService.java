package com.agileforge.application.service;

import com.agileforge.application.dto.request.AddIncidentEventRequest;
import com.agileforge.application.dto.request.CreateIncidentRequest;
import com.agileforge.application.dto.request.UpdateIncidentRequest;
import com.agileforge.application.dto.response.IncidentEventResponse;
import com.agileforge.application.dto.response.IncidentResponse;
import com.agileforge.application.dto.response.IncidentTimelineResponse;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Incident;
import com.agileforge.domain.model.IncidentEvent;
import com.agileforge.domain.model.IncidentEventType;
import com.agileforge.domain.model.IncidentSeverity;
import com.agileforge.domain.model.IncidentStatus;
import com.agileforge.domain.port.out.IncidentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepositoryPort incidentRepository;

    public IncidentService(IncidentRepositoryPort incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public IncidentResponse createIncident(UUID projectId, UUID userId, CreateIncidentRequest request) {
        IncidentSeverity severity = request.severity() != null
                ? IncidentSeverity.valueOf(request.severity())
                : IncidentSeverity.HIGH;

        Incident incident = new Incident(projectId, request.title(), request.description(), severity);
        incident.setCommanderId(userId);
        Incident saved = incidentRepository.save(incident);

        // Add initial event
        IncidentEvent event = new IncidentEvent(saved.getId(), userId, IncidentEventType.STATUS_CHANGE,
                "Incident created with severity " + severity.name());
        incidentRepository.saveEvent(event);

        // Add creator as participant
        incidentRepository.addParticipant(saved.getId(), userId, "COMMANDER");

        log.info("Incident created: {} in project {}", saved.getId(), projectId);
        return toResponse(saved);
    }

    public IncidentResponse updateIncident(UUID incidentId, UUID userId, UpdateIncidentRequest request) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident", incidentId));

        IncidentStatus oldStatus = incident.getStatus();

        if (request.title() != null) incident.setTitle(request.title());
        if (request.description() != null) incident.setDescription(request.description());
        if (request.severity() != null) incident.setSeverity(IncidentSeverity.valueOf(request.severity()));
        if (request.status() != null) incident.setStatus(IncidentStatus.valueOf(request.status()));
        if (request.commanderId() != null) incident.setCommanderId(request.commanderId());
        if (request.rootCause() != null) incident.setRootCause(request.rootCause());
        if (request.resolution() != null) incident.setResolution(request.resolution());
        if (request.postMortem() != null) incident.setPostMortem(request.postMortem());

        Incident saved = incidentRepository.save(incident);

        // Add status change event if status changed
        if (request.status() != null && !oldStatus.name().equals(request.status())) {
            IncidentEvent event = new IncidentEvent(incidentId, userId, IncidentEventType.STATUS_CHANGE,
                    "Status changed from " + oldStatus.name() + " to " + request.status());
            incidentRepository.saveEvent(event);
        }

        log.info("Incident updated: {}", incidentId);
        return toResponse(saved);
    }

    public IncidentResponse resolveIncident(UUID incidentId, UUID userId, String resolution) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident", incidentId));

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(Instant.now());
        incident.setResolution(resolution);

        Incident saved = incidentRepository.save(incident);

        IncidentEvent event = new IncidentEvent(incidentId, userId, IncidentEventType.RESOLUTION,
                "Incident resolved: " + resolution);
        incidentRepository.saveEvent(event);

        log.info("Incident resolved: {}", incidentId);
        return toResponse(saved);
    }

    public IncidentEventResponse addEvent(UUID incidentId, UUID userId, AddIncidentEventRequest request) {
        incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident", incidentId));

        IncidentEvent event = new IncidentEvent(incidentId, userId,
                IncidentEventType.valueOf(request.eventType()), request.message());
        IncidentEvent saved = incidentRepository.saveEvent(event);

        log.info("Event added to incident {}: {}", incidentId, request.eventType());
        return toEventResponse(saved);
    }

    public void addParticipant(UUID incidentId, UUID userId, String role) {
        incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident", incidentId));

        incidentRepository.addParticipant(incidentId, userId, role != null ? role : "RESPONDER");
        log.info("Participant {} added to incident {} with role {}", userId, incidentId, role);
    }

    @Transactional(readOnly = true)
    public IncidentTimelineResponse getTimeline(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident", incidentId));

        List<IncidentEventResponse> events = incidentRepository.findEventsByIncidentId(incidentId).stream()
                .map(this::toEventResponse).toList();

        List<UUID> participants = incidentRepository.findParticipantIds(incidentId);

        long durationMinutes = calculateDurationMinutes(incident);

        return new IncidentTimelineResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getSeverity().name(),
                incident.getStatus().name(),
                events,
                participants,
                durationMinutes
        );
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getActiveIncidents(UUID projectId) {
        return incidentRepository.findActiveByProjectId(projectId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getByProject(UUID projectId) {
        return incidentRepository.findByProjectId(projectId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public IncidentResponse getById(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident", incidentId));
        return toResponse(incident);
    }

    private IncidentResponse toResponse(Incident incident) {
        List<UUID> participantIds = incidentRepository.findParticipantIds(incident.getId());
        Long durationMinutes = calculateDurationMinutes(incident);

        return new IncidentResponse(
                incident.getId(),
                incident.getProjectId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity().name(),
                incident.getStatus().name(),
                incident.getCommanderId(),
                incident.getStartedAt(),
                incident.getResolvedAt(),
                incident.getRootCause(),
                incident.getResolution(),
                incident.getPostMortem(),
                participantIds,
                durationMinutes,
                incident.getCreatedAt()
        );
    }

    private IncidentEventResponse toEventResponse(IncidentEvent event) {
        return new IncidentEventResponse(
                event.getId(),
                event.getIncidentId(),
                event.getUserId(),
                event.getEventType().name(),
                event.getMessage(),
                event.getCreatedAt()
        );
    }

    private long calculateDurationMinutes(Incident incident) {
        Instant end = incident.getResolvedAt() != null ? incident.getResolvedAt() : Instant.now();
        Instant start = incident.getStartedAt() != null ? incident.getStartedAt() : incident.getCreatedAt();
        if (start == null) return 0;
        return Duration.between(start, end).toMinutes();
    }
}
