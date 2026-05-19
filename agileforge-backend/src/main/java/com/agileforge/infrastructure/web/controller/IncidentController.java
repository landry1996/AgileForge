package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.AddIncidentEventRequest;
import com.agileforge.application.dto.request.CreateIncidentRequest;
import com.agileforge.application.dto.request.UpdateIncidentRequest;
import com.agileforge.application.dto.response.IncidentEventResponse;
import com.agileforge.application.dto.response.IncidentResponse;
import com.agileforge.application.dto.response.IncidentTimelineResponse;
import com.agileforge.application.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Tag(name = "Incident Management", description = "Incident management (War Room) endpoints")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping("/projects/{projectId}/incidents")
    @Operation(summary = "Create a new incident")
    public ResponseEntity<IncidentResponse> create(@PathVariable UUID projectId,
                                                   @Valid @RequestBody CreateIncidentRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        IncidentResponse response = incidentService.createIncident(projectId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/incidents")
    @Operation(summary = "Get all incidents for a project")
    public ResponseEntity<List<IncidentResponse>> getByProject(@PathVariable UUID projectId) {
        List<IncidentResponse> incidents = incidentService.getByProject(projectId);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/projects/{projectId}/incidents/active")
    @Operation(summary = "Get active incidents for a project")
    public ResponseEntity<List<IncidentResponse>> getActiveByProject(@PathVariable UUID projectId) {
        List<IncidentResponse> incidents = incidentService.getActiveIncidents(projectId);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/incidents/{incidentId}")
    @Operation(summary = "Get incident by ID")
    public ResponseEntity<IncidentResponse> getById(@PathVariable UUID incidentId) {
        IncidentResponse response = incidentService.getById(incidentId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/incidents/{incidentId}")
    @Operation(summary = "Update an incident")
    public ResponseEntity<IncidentResponse> update(@PathVariable UUID incidentId,
                                                   @Valid @RequestBody UpdateIncidentRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        IncidentResponse response = incidentService.updateIncident(incidentId, userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/incidents/{incidentId}/resolve")
    @Operation(summary = "Resolve an incident")
    public ResponseEntity<IncidentResponse> resolve(@PathVariable UUID incidentId,
                                                    @RequestBody Map<String, String> body,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        String resolution = body.getOrDefault("resolution", "Resolved");
        IncidentResponse response = incidentService.resolveIncident(incidentId, userId, resolution);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/incidents/{incidentId}/events")
    @Operation(summary = "Add an event to incident timeline")
    public ResponseEntity<IncidentEventResponse> addEvent(@PathVariable UUID incidentId,
                                                          @Valid @RequestBody AddIncidentEventRequest request,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        IncidentEventResponse response = incidentService.addEvent(incidentId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/incidents/{incidentId}/participants/{userId}")
    @Operation(summary = "Add a participant to an incident")
    public ResponseEntity<Void> addParticipant(@PathVariable UUID incidentId,
                                               @PathVariable UUID userId,
                                               @RequestParam(defaultValue = "RESPONDER") String role) {
        incidentService.addParticipant(incidentId, userId, role);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/incidents/{incidentId}/timeline")
    @Operation(summary = "Get full incident timeline")
    public ResponseEntity<IncidentTimelineResponse> getTimeline(@PathVariable UUID incidentId) {
        IncidentTimelineResponse response = incidentService.getTimeline(incidentId);
        return ResponseEntity.ok(response);
    }
}
