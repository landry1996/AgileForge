package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateTimeEntryRequest;
import com.agileforge.application.dto.response.TimeEntryResponse;
import com.agileforge.application.dto.response.TimeTrackingSummaryResponse;
import com.agileforge.application.service.TimeTrackingService;
import com.agileforge.domain.model.TimeEntry;
import com.agileforge.domain.port.out.UserRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Tag(name = "Time Tracking", description = "Time tracking and time entry management endpoints")
public class TimeTrackingController {

    private final TimeTrackingService timeTrackingService;
    private final UserRepositoryPort userRepository;

    public TimeTrackingController(TimeTrackingService timeTrackingService, UserRepositoryPort userRepository) {
        this.timeTrackingService = timeTrackingService;
        this.userRepository = userRepository;
    }

    @PostMapping("/tickets/{ticketId}/time-entries")
    @Operation(summary = "Log a time entry on a ticket")
    public ResponseEntity<TimeEntryResponse> logTime(@PathVariable UUID ticketId,
                                                     @Valid @RequestBody CreateTimeEntryRequest request,
                                                     Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        TimeEntry entry = timeTrackingService.logTime(ticketId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entry));
    }

    @GetMapping("/tickets/{ticketId}/time-entries")
    @Operation(summary = "Get all time entries for a ticket")
    public ResponseEntity<List<TimeEntryResponse>> getEntriesByTicket(@PathVariable UUID ticketId) {
        List<TimeEntryResponse> entries = timeTrackingService.getEntriesByTicket(ticketId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/tickets/{ticketId}/time-summary")
    @Operation(summary = "Get time tracking summary for a ticket")
    public ResponseEntity<TimeTrackingSummaryResponse> getTicketSummary(@PathVariable UUID ticketId) {
        TimeTrackingService.TimeTrackingSummary summary = timeTrackingService.getTicketSummary(ticketId);
        List<TimeEntryResponse> entries = summary.entries().stream().map(this::toResponse).toList();
        TimeTrackingSummaryResponse response = new TimeTrackingSummaryResponse(
                summary.ticketId(), summary.totalLogged(), summary.estimatedHours(), entries);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/time-entries/my")
    @Operation(summary = "Get current user's time entries within a date range")
    public ResponseEntity<List<TimeEntryResponse>> getMyEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        List<TimeEntryResponse> entries = timeTrackingService.getEntriesByUser(userId, from, to).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/time-entries/my/weekly-total")
    @Operation(summary = "Get current user's total hours logged this week")
    public ResponseEntity<Map<String, Double>> getMyWeeklyTotal(Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        double total = timeTrackingService.getUserWeeklyTotal(userId);
        return ResponseEntity.ok(Map.of("weeklyTotal", total));
    }

    @DeleteMapping("/time-entries/{entryId}")
    @Operation(summary = "Delete a time entry (only the author can delete)")
    public ResponseEntity<Void> deleteEntry(@PathVariable UUID entryId, Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        timeTrackingService.deleteEntry(entryId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private TimeEntryResponse toResponse(TimeEntry entry) {
        return new TimeEntryResponse(
                entry.getId(),
                entry.getTicketId(),
                entry.getUserId(),
                entry.getHours(),
                entry.getDescription(),
                entry.getWorkDate(),
                entry.getCreatedAt()
        );
    }
}
