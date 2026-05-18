package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.response.NotificationResponse;
import com.agileforge.application.service.NotificationService;
import com.agileforge.domain.model.Notification;
import com.agileforge.domain.port.out.UserRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Real-time notification endpoints")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepositoryPort userRepository;

    public NotificationController(NotificationService notificationService, UserRepositoryPort userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to real-time notifications (SSE)")
    public SseEmitter subscribe(Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        return notificationService.subscribe(userId);
    }

    @GetMapping
    @Operation(summary = "Get all notifications (paginated)")
    public ResponseEntity<List<NotificationResponse>> getAll(Authentication auth,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        UUID userId = getCurrentUserId(auth);
        List<NotificationResponse> notifications = notificationService.getByUserId(userId, page, size)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<List<NotificationResponse>> getUnread(Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        List<NotificationResponse> notifications = notificationService.getUnread(userId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType().name(), n.getTitle(), n.getMessage(),
                n.getReferenceId(), n.getReferenceType(), n.isRead(), n.getCreatedAt()
        );
    }
}
