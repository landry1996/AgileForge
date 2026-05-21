package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.CollaborationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/collaboration")
public class CollaborationController {

    private final CollaborationService collaborationService;

    public CollaborationController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @PostMapping("/sessions/join")
    public ResponseEntity<?> joinSession(@RequestBody Map<String, String> request) {
        String resourceType = request.get("resourceType");
        UUID resourceId = UUID.fromString(request.get("resourceId"));
        UUID userId = UUID.fromString(request.get("userId"));
        return ResponseEntity.ok(collaborationService.joinSession(resourceType, resourceId, userId));
    }

    @PostMapping("/sessions/{sessionId}/leave")
    public ResponseEntity<Void> leaveSession(@PathVariable UUID sessionId, @RequestBody Map<String, String> request) {
        UUID userId = UUID.fromString(request.get("userId"));
        collaborationService.leaveSession(sessionId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions/{sessionId}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(collaborationService.getSessionParticipants(sessionId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getActiveSessions(@RequestParam String resourceType, @RequestParam UUID resourceId) {
        return ResponseEntity.ok(collaborationService.getActiveSessions(resourceType, resourceId));
    }

    @PostMapping("/presence/heartbeat")
    public ResponseEntity<Void> heartbeat(@RequestBody Map<String, String> request) {
        UUID userId = UUID.fromString(request.get("userId"));
        collaborationService.heartbeat(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/presence/update")
    public ResponseEntity<Void> updatePresence(@RequestBody Map<String, String> request) {
        UUID userId = UUID.fromString(request.get("userId"));
        String status = request.get("status");
        String currentPage = request.get("currentPage");
        UUID resourceId = request.containsKey("currentResourceId") ?
                UUID.fromString(request.get("currentResourceId")) : null;
        collaborationService.updatePresence(userId, status, currentPage, resourceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/presence/online/{organizationId}")
    public ResponseEntity<?> getOnlineUsers(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(collaborationService.getOnlineUsers(organizationId));
    }

    @GetMapping("/operations/{sessionId}")
    public ResponseEntity<?> getOperations(@PathVariable UUID sessionId,
                                           @RequestParam(required = false) String since) {
        Instant sinceInstant = since != null ? Instant.parse(since) : Instant.now().minusSeconds(3600);
        return ResponseEntity.ok(collaborationService.getOperationHistory(sessionId, sinceInstant));
    }
}
