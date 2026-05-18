package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.response.ActivityResponse;
import com.agileforge.application.service.TicketService;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketHistory;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import com.agileforge.domain.port.out.UserRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/activity")
@Tag(name = "Activity", description = "Activity feed endpoints")
public class ActivityController {

    private final TicketService ticketService;
    private final TicketRepositoryPort ticketRepository;
    private final UserRepositoryPort userRepository;

    public ActivityController(TicketService ticketService, TicketRepositoryPort ticketRepository,
                              UserRepositoryPort userRepository) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get recent activity for a project")
    public ResponseEntity<List<ActivityResponse>> getProjectActivity(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "20") int limit) {

        List<Ticket> tickets = ticketRepository.findByProjectId(projectId);
        List<ActivityResponse> activities = new ArrayList<>();

        for (Ticket ticket : tickets) {
            List<TicketHistory> history = ticketService.getHistory(ticket.getId());
            for (TicketHistory h : history) {
                String userName = resolveUserName(h.getUserId());
                activities.add(new ActivityResponse(
                        h.getId(), ticket.getId(), ticket.getFullKey(),
                        h.getUserId(), userName, "updated", h.getField(),
                        h.getOldValue(), h.getNewValue(), h.getCreatedAt()
                ));
            }
        }

        activities.sort(Comparator.comparing(ActivityResponse::createdAt).reversed());
        List<ActivityResponse> limited = activities.stream().limit(limit).toList();

        return ResponseEntity.ok(limited);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my recent activity")
    public ResponseEntity<List<ActivityResponse>> getMyActivity(
            Authentication auth,
            @RequestParam(defaultValue = "20") int limit) {

        UUID userId = getCurrentUserId(auth);
        List<Ticket> tickets = ticketRepository.findByAssigneeId(userId);
        List<ActivityResponse> activities = new ArrayList<>();

        for (Ticket ticket : tickets) {
            List<TicketHistory> history = ticketService.getHistory(ticket.getId());
            for (TicketHistory h : history) {
                String userName = resolveUserName(h.getUserId());
                activities.add(new ActivityResponse(
                        h.getId(), ticket.getId(), ticket.getFullKey(),
                        h.getUserId(), userName, "updated", h.getField(),
                        h.getOldValue(), h.getNewValue(), h.getCreatedAt()
                ));
            }
        }

        activities.sort(Comparator.comparing(ActivityResponse::createdAt).reversed());
        List<ActivityResponse> limited = activities.stream().limit(limit).toList();

        return ResponseEntity.ok(limited);
    }

    private String resolveUserName(UUID userId) {
        if (userId == null) return "System";
        return userRepository.findById(userId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Unknown");
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
