package com.agileforge.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CollaborationService Tests")
class CollaborationServiceTest {

    private final CollaborationService service = new CollaborationService();

    @Test
    @DisplayName("Should join session")
    void shouldJoinSession() {
        UUID resourceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var session = service.joinSession("TICKET", resourceId, userId);

        assertThat(session).isNotNull();
        assertThat(session.id()).isNotNull();
        assertThat(session.resourceType()).isEqualTo("TICKET");
        assertThat(session.resourceId()).isEqualTo(resourceId);
    }

    @Test
    @DisplayName("Should leave session without error")
    void shouldLeaveSession() {
        service.leaveSession(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    @DisplayName("Should update cursor without error")
    void shouldUpdateCursor() {
        service.updateCursor(UUID.randomUUID(), UUID.randomUUID(), "{\"line\":5,\"col\":10}");
    }

    @Test
    @DisplayName("Should broadcast operation without error")
    void shouldBroadcastOperation() {
        service.broadcastOperation(UUID.randomUUID(), UUID.randomUUID(), "INSERT", "{\"text\":\"hello\"}");
    }

    @Test
    @DisplayName("Should return empty participants")
    void shouldReturnEmptyParticipants() {
        assertThat(service.getSessionParticipants(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Should return empty active sessions")
    void shouldReturnEmptyActiveSessions() {
        assertThat(service.getActiveSessions("TICKET", UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Should update presence without error")
    void shouldUpdatePresence() {
        service.updatePresence(UUID.randomUUID(), "ONLINE", "/board", UUID.randomUUID());
    }

    @Test
    @DisplayName("Should return empty online users")
    void shouldReturnEmptyOnlineUsers() {
        assertThat(service.getOnlineUsers(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Should heartbeat without error")
    void shouldHeartbeat() {
        service.heartbeat(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should cleanup stale presences without error")
    void shouldCleanupStalePresences() {
        service.cleanupStalePresences();
    }

    @Test
    @DisplayName("Should return empty operation history")
    void shouldReturnEmptyOperationHistory() {
        assertThat(service.getOperationHistory(UUID.randomUUID(), Instant.now().minusSeconds(3600))).isEmpty();
    }
}
