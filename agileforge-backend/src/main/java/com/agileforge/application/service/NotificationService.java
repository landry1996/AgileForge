package com.agileforge.application.service;

import com.agileforge.domain.model.Notification;
import com.agileforge.domain.model.NotificationType;
import com.agileforge.domain.port.out.NotificationRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    private final NotificationRepositoryPort notificationRepository;
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        try {
            long unreadCount = notificationRepository.countUnreadByUserId(userId);
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("unreadCount", unreadCount)));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }

        log.debug("SSE subscription for user {}", userId);
        return emitter;
    }

    public Notification send(UUID userId, NotificationType type, String title, String message,
                             UUID referenceId, String referenceType) {
        Notification notification = new Notification(userId, type, title, message, referenceId, referenceType);
        Notification saved = notificationRepository.save(notification);

        pushToUser(userId, saved);
        log.debug("Notification sent to user {}: {}", userId, title);
        return saved;
    }

    public void notifyTicketAssigned(UUID assigneeId, String ticketKey, String ticketTitle, UUID ticketId) {
        send(assigneeId, NotificationType.TICKET_ASSIGNED,
                "Ticket assigned: " + ticketKey,
                "You have been assigned to \"" + ticketTitle + "\"",
                ticketId, "TICKET");
    }

    public void notifyTicketStatusChanged(UUID userId, String ticketKey, String oldStatus,
                                          String newStatus, UUID ticketId) {
        send(userId, NotificationType.TICKET_STATUS_CHANGED,
                ticketKey + " status changed",
                "Status changed from " + oldStatus + " to " + newStatus,
                ticketId, "TICKET");
    }

    public void notifyTicketCommented(UUID userId, String ticketKey, String commenterName, UUID ticketId) {
        send(userId, NotificationType.TICKET_COMMENTED,
                "New comment on " + ticketKey,
                commenterName + " commented on this ticket",
                ticketId, "TICKET");
    }

    public void notifySprintStarted(UUID userId, String sprintName, UUID sprintId) {
        send(userId, NotificationType.SPRINT_STARTED,
                "Sprint started: " + sprintName,
                "The sprint \"" + sprintName + "\" has been started",
                sprintId, "SPRINT");
    }

    public void notifySprintCompleted(UUID userId, String sprintName, UUID sprintId) {
        send(userId, NotificationType.SPRINT_COMPLETED,
                "Sprint completed: " + sprintName,
                "The sprint \"" + sprintName + "\" has been completed",
                sprintId, "SPRINT");
    }

    @Transactional(readOnly = true)
    public List<Notification> getByUserId(UUID userId, int page, int size) {
        return notificationRepository.findByUserId(userId, page, size);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnread(UUID userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markAsRead(UUID notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        pushCountUpdate(userId);
    }

    private void pushToUser(UUID userId, Notification notification) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) return;

        Map<String, Object> data = Map.of(
                "id", notification.getId(),
                "type", notification.getType().name(),
                "title", notification.getTitle(),
                "message", notification.getMessage() != null ? notification.getMessage() : "",
                "referenceId", notification.getReferenceId() != null ? notification.getReferenceId().toString() : "",
                "referenceType", notification.getReferenceType() != null ? notification.getReferenceType() : "",
                "createdAt", notification.getCreatedAt().toString()
        );

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(data));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    private void pushCountUpdate(UUID userId) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) return;

        long count = notificationRepository.countUnreadByUserId(userId);
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().name("count").data(Map.of("unreadCount", count)));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }
}
