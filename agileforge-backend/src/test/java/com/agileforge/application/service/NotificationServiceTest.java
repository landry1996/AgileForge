package com.agileforge.application.service;

import com.agileforge.domain.model.Notification;
import com.agileforge.domain.model.NotificationType;
import com.agileforge.domain.port.out.NotificationRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepositoryPort notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Nested
    @DisplayName("Send Notification")
    class SendTests {

        @Test
        @DisplayName("Should send notification and persist it")
        void shouldSendNotification() {
            UUID userId = UUID.randomUUID();
            UUID ticketId = UUID.randomUUID();

            when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
                Notification n = i.getArgument(0);
                n.setId(UUID.randomUUID());
                n.setCreatedAt(Instant.now());
                return n;
            });

            Notification result = notificationService.send(userId, NotificationType.TICKET_ASSIGNED,
                    "Ticket assigned: PROJ-1", "You have been assigned", ticketId, "TICKET");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getType()).isEqualTo(NotificationType.TICKET_ASSIGNED);
            assertThat(result.getTitle()).isEqualTo("Ticket assigned: PROJ-1");
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should notify ticket assigned")
        void shouldNotifyTicketAssigned() {
            UUID assigneeId = UUID.randomUUID();
            UUID ticketId = UUID.randomUUID();

            when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
                Notification n = i.getArgument(0);
                n.setId(UUID.randomUUID());
                n.setCreatedAt(Instant.now());
                return n;
            });

            notificationService.notifyTicketAssigned(assigneeId, "PROJ-1", "Fix login bug", ticketId);

            verify(notificationRepository).save(argThat(n ->
                    n.getType() == NotificationType.TICKET_ASSIGNED &&
                    n.getUserId().equals(assigneeId) &&
                    n.getReferenceId().equals(ticketId)
            ));
        }

        @Test
        @DisplayName("Should notify ticket status changed")
        void shouldNotifyTicketStatusChanged() {
            UUID userId = UUID.randomUUID();
            UUID ticketId = UUID.randomUUID();

            when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
                Notification n = i.getArgument(0);
                n.setId(UUID.randomUUID());
                n.setCreatedAt(Instant.now());
                return n;
            });

            notificationService.notifyTicketStatusChanged(userId, "PROJ-5", "TODO", "IN_PROGRESS", ticketId);

            verify(notificationRepository).save(argThat(n ->
                    n.getType() == NotificationType.TICKET_STATUS_CHANGED &&
                    n.getTitle().contains("PROJ-5")
            ));
        }

        @Test
        @DisplayName("Should notify sprint started")
        void shouldNotifySprintStarted() {
            UUID userId = UUID.randomUUID();
            UUID sprintId = UUID.randomUUID();

            when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
                Notification n = i.getArgument(0);
                n.setId(UUID.randomUUID());
                n.setCreatedAt(Instant.now());
                return n;
            });

            notificationService.notifySprintStarted(userId, "Sprint 3", sprintId);

            verify(notificationRepository).save(argThat(n ->
                    n.getType() == NotificationType.SPRINT_STARTED &&
                    n.getReferenceType().equals("SPRINT")
            ));
        }
    }

    @Nested
    @DisplayName("Read Notifications")
    class ReadTests {

        @Test
        @DisplayName("Should get notifications by user id")
        void shouldGetByUserId() {
            UUID userId = UUID.randomUUID();
            Notification n1 = createNotification(userId, NotificationType.TICKET_ASSIGNED);
            Notification n2 = createNotification(userId, NotificationType.TICKET_COMMENTED);

            when(notificationRepository.findByUserId(userId, 0, 20)).thenReturn(List.of(n1, n2));

            List<Notification> result = notificationService.getByUserId(userId, 0, 20);

            assertThat(result).hasSize(2);
            verify(notificationRepository).findByUserId(userId, 0, 20);
        }

        @Test
        @DisplayName("Should get unread notifications")
        void shouldGetUnread() {
            UUID userId = UUID.randomUUID();
            Notification n1 = createNotification(userId, NotificationType.TICKET_ASSIGNED);

            when(notificationRepository.findUnreadByUserId(userId)).thenReturn(List.of(n1));

            List<Notification> result = notificationService.getUnread(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isRead()).isFalse();
        }

        @Test
        @DisplayName("Should count unread notifications")
        void shouldCountUnread() {
            UUID userId = UUID.randomUUID();
            when(notificationRepository.countUnreadByUserId(userId)).thenReturn(5L);

            long count = notificationService.countUnread(userId);

            assertThat(count).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Mark As Read")
    class MarkReadTests {

        @Test
        @DisplayName("Should mark single notification as read")
        void shouldMarkAsRead() {
            UUID notificationId = UUID.randomUUID();

            notificationService.markAsRead(notificationId);

            verify(notificationRepository).markAsRead(notificationId);
        }

        @Test
        @DisplayName("Should mark all notifications as read")
        void shouldMarkAllAsRead() {
            UUID userId = UUID.randomUUID();

            notificationService.markAllAsRead(userId);

            verify(notificationRepository).markAllAsReadByUserId(userId);
        }
    }

    private Notification createNotification(UUID userId, NotificationType type) {
        Notification n = new Notification(userId, type, "Test title", "Test message", UUID.randomUUID(), "TICKET");
        n.setId(UUID.randomUUID());
        n.setCreatedAt(Instant.now());
        return n;
    }
}
