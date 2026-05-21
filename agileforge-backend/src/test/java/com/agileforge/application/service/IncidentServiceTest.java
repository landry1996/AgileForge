package com.agileforge.application.service;

import com.agileforge.application.dto.request.AddIncidentEventRequest;
import com.agileforge.application.dto.request.CreateIncidentRequest;
import com.agileforge.application.dto.request.UpdateIncidentRequest;
import com.agileforge.application.dto.response.IncidentResponse;
import com.agileforge.application.dto.response.IncidentTimelineResponse;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.IncidentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepositoryPort incidentRepository;

    @InjectMocks
    private IncidentService incidentService;

    @Test
    void shouldCreateIncident() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Incident incident = new Incident(projectId, "Outage", "DB down", IncidentSeverity.CRITICAL);
        incident.setId(UUID.randomUUID());
        incident.setStatus(IncidentStatus.DETECTED);
        incident.setStartedAt(Instant.now());
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentRepository.saveEvent(any())).thenReturn(new IncidentEvent(incident.getId(), userId, IncidentEventType.STATUS_CHANGE, "created"));
        when(incidentRepository.findParticipantIds(any())).thenReturn(List.of(userId));

        IncidentResponse result = incidentService.createIncident(projectId, userId, new CreateIncidentRequest("Outage", "DB down", "CRITICAL"));

        assertNotNull(result);
        assertEquals("CRITICAL", result.severity());
        verify(incidentRepository).addParticipant(any(), eq(userId), eq("COMMANDER"));
    }

    @Test
    void shouldUpdateIncident() {
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Incident incident = new Incident(UUID.randomUUID(), "Outage", "desc", IncidentSeverity.HIGH);
        incident.setId(incidentId);
        incident.setStatus(IncidentStatus.DETECTED);
        incident.setStartedAt(Instant.now());
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(incidentRepository.findParticipantIds(any())).thenReturn(List.of());
        when(incidentRepository.saveEvent(any())).thenReturn(new IncidentEvent());

        IncidentResponse result = incidentService.updateIncident(incidentId, userId,
                new UpdateIncidentRequest("Updated", null, null, "INVESTIGATING", null, null, null, null));

        assertEquals("Updated", result.title());
    }

    @Test
    void shouldResolveIncident() {
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Incident incident = new Incident(UUID.randomUUID(), "Outage", "desc", IncidentSeverity.HIGH);
        incident.setId(incidentId);
        incident.setStatus(IncidentStatus.INVESTIGATING);
        incident.setStartedAt(Instant.now());
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(incidentRepository.saveEvent(any())).thenReturn(new IncidentEvent());
        when(incidentRepository.findParticipantIds(any())).thenReturn(List.of());

        IncidentResponse result = incidentService.resolveIncident(incidentId, userId, "Fixed the DB");

        assertEquals("RESOLVED", result.status());
        assertNotNull(result.resolvedAt());
    }

    @Test
    void shouldAddEvent() {
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Incident incident = new Incident(UUID.randomUUID(), "Test", null, IncidentSeverity.LOW);
        incident.setId(incidentId);
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        IncidentEvent event = new IncidentEvent(incidentId, userId, IncidentEventType.UPDATE, "progress");
        event.setId(UUID.randomUUID());
        event.setCreatedAt(Instant.now());
        when(incidentRepository.saveEvent(any())).thenReturn(event);

        var result = incidentService.addEvent(incidentId, userId, new AddIncidentEventRequest("UPDATE", "progress"));

        assertNotNull(result);
    }

    @Test
    void shouldAddParticipant() {
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Incident incident = new Incident(UUID.randomUUID(), "Test", null, IncidentSeverity.LOW);
        incident.setId(incidentId);
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));

        incidentService.addParticipant(incidentId, userId, "RESPONDER");

        verify(incidentRepository).addParticipant(incidentId, userId, "RESPONDER");
    }

    @Test
    void shouldGetTimeline() {
        UUID incidentId = UUID.randomUUID();
        Incident incident = new Incident(UUID.randomUUID(), "Outage", null, IncidentSeverity.HIGH);
        incident.setId(incidentId);
        incident.setStatus(IncidentStatus.INVESTIGATING);
        incident.setStartedAt(Instant.now());
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        when(incidentRepository.findEventsByIncidentId(incidentId)).thenReturn(List.of());
        when(incidentRepository.findParticipantIds(incidentId)).thenReturn(List.of(UUID.randomUUID()));

        IncidentTimelineResponse result = incidentService.getTimeline(incidentId);

        assertNotNull(result);
        assertEquals("Outage", result.title());
    }

    @Test
    void shouldGetActiveIncidents() {
        UUID projectId = UUID.randomUUID();
        when(incidentRepository.findActiveByProjectId(projectId)).thenReturn(List.of());

        List<IncidentResponse> result = incidentService.getActiveIncidents(projectId);

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(incidentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> incidentService.getById(id));
    }

    @Test
    void shouldGetById() {
        UUID id = UUID.randomUUID();
        Incident incident = new Incident(UUID.randomUUID(), "Test", null, IncidentSeverity.LOW);
        incident.setId(id);
        incident.setStatus(IncidentStatus.DETECTED);
        incident.setStartedAt(Instant.now());
        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));
        when(incidentRepository.findParticipantIds(id)).thenReturn(List.of());

        IncidentResponse result = incidentService.getById(id);

        assertEquals(id, result.id());
    }
}
