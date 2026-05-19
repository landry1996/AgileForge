package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateCapacityEntryRequest;
import com.agileforge.application.dto.response.CapacityEntryResponse;
import com.agileforge.application.dto.response.CapacityForecastResponse;
import com.agileforge.application.dto.response.TeamCapacityResponse;
import com.agileforge.application.service.CapacityPlanningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Capacity Planning", description = "Team capacity and workload planning")
public class CapacityController {

    private final CapacityPlanningService capacityPlanningService;

    public CapacityController(CapacityPlanningService capacityPlanningService) {
        this.capacityPlanningService = capacityPlanningService;
    }

    @PostMapping("/projects/{projectId}/capacity")
    @Operation(summary = "Add a capacity entry for a team member")
    public ResponseEntity<CapacityEntryResponse> addCapacityEntry(@PathVariable UUID projectId,
                                                                   @Valid @RequestBody CreateCapacityEntryRequest request) {
        CapacityEntryResponse response = capacityPlanningService.addCapacityEntry(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/capacity/sprint/{sprintId}")
    @Operation(summary = "Get team capacity for a specific sprint")
    public ResponseEntity<TeamCapacityResponse> getTeamCapacity(@PathVariable UUID projectId,
                                                                 @PathVariable UUID sprintId) {
        TeamCapacityResponse response = capacityPlanningService.getTeamCapacity(projectId, sprintId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/capacity/forecast")
    @Operation(summary = "Get capacity forecast for a project")
    public ResponseEntity<CapacityForecastResponse> getCapacityForecast(@PathVariable UUID projectId) {
        CapacityForecastResponse response = capacityPlanningService.getCapacityForecast(projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/capacity/{entryId}")
    @Operation(summary = "Update a capacity entry")
    public ResponseEntity<CapacityEntryResponse> updateEntry(@PathVariable UUID entryId,
                                                              @Valid @RequestBody CreateCapacityEntryRequest request) {
        CapacityEntryResponse response = capacityPlanningService.updateEntry(entryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/capacity/{entryId}")
    @Operation(summary = "Delete a capacity entry")
    public ResponseEntity<Void> deleteEntry(@PathVariable UUID entryId) {
        capacityPlanningService.deleteEntry(entryId);
        return ResponseEntity.noContent().build();
    }
}
