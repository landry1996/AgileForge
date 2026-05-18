package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.NotificationService;
import com.agileforge.domain.model.Notification;
import com.agileforge.domain.model.NotificationType;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.out.UserRepositoryPort;
import com.agileforge.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController Integration Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserRepositoryPort userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /notifications - should return notifications list")
    void shouldReturnNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        mockUser(userId);

        Notification n1 = createNotification(userId, NotificationType.TICKET_ASSIGNED, "Ticket assigned");
        when(notificationService.getByUserId(userId, 0, 20)).thenReturn(List.of(n1));

        mockMvc.perform(get("/notifications").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Ticket assigned"))
                .andExpect(jsonPath("$[0].type").value("TICKET_ASSIGNED"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /notifications/unread - should return unread notifications")
    void shouldReturnUnreadNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        mockUser(userId);

        Notification n1 = createNotification(userId, NotificationType.TICKET_COMMENTED, "New comment");
        when(notificationService.getUnread(userId)).thenReturn(List.of(n1));

        mockMvc.perform(get("/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /notifications/unread/count - should return count")
    void shouldReturnUnreadCount() throws Exception {
        UUID userId = UUID.randomUUID();
        mockUser(userId);

        when(notificationService.countUnread(userId)).thenReturn(7L);

        mockMvc.perform(get("/notifications/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(7));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("PATCH /notifications/{id}/read - should mark as read")
    void shouldMarkAsRead() throws Exception {
        UUID notificationId = UUID.randomUUID();

        mockMvc.perform(patch("/notifications/{id}/read", notificationId).with(csrf()))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead(notificationId);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("PATCH /notifications/read-all - should mark all as read")
    void shouldMarkAllAsRead() throws Exception {
        UUID userId = UUID.randomUUID();
        mockUser(userId);

        mockMvc.perform(patch("/notifications/read-all").with(csrf()))
                .andExpect(status().isOk());

        verify(notificationService).markAllAsRead(userId);
    }

    @Test
    @DisplayName("GET /notifications - should return 401 when unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isUnauthorized());
    }

    private void mockUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    }

    private Notification createNotification(UUID userId, NotificationType type, String title) {
        Notification n = new Notification(userId, type, title, "Test message", UUID.randomUUID(), "TICKET");
        n.setId(UUID.randomUUID());
        n.setCreatedAt(Instant.now());
        return n;
    }
}
