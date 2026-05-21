package com.agileforge.infrastructure.web.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/collaboration/{sessionId}/cursor")
    @SendTo("/topic/collaboration/{sessionId}/cursors")
    public Map<String, Object> updateCursor(@DestinationVariable String sessionId, Map<String, Object> payload) {
        payload.put("timestamp", Instant.now().toString());
        return payload;
    }

    @MessageMapping("/collaboration/{sessionId}/operation")
    @SendTo("/topic/collaboration/{sessionId}/operations")
    public Map<String, Object> broadcastOperation(@DestinationVariable String sessionId, Map<String, Object> payload) {
        payload.put("timestamp", Instant.now().toString());
        return payload;
    }

    @MessageMapping("/presence/update")
    public void handlePresenceUpdate(Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        messagingTemplate.convertAndSend("/topic/presence/" + payload.get("organizationId"), payload);
    }

    @MessageMapping("/ticket/{ticketId}/typing")
    @SendTo("/topic/ticket/{ticketId}/typing")
    public Map<String, Object> typingIndicator(@DestinationVariable String ticketId, Map<String, Object> payload) {
        payload.put("timestamp", Instant.now().toString());
        return payload;
    }

    @MessageMapping("/board/{projectId}/update")
    @SendTo("/topic/board/{projectId}/updates")
    public Map<String, Object> boardUpdate(@DestinationVariable String projectId, Map<String, Object> payload) {
        payload.put("timestamp", Instant.now().toString());
        return payload;
    }

    public void notifyUser(String userId, String type, Object payload) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications",
                Map.of("type", type, "payload", payload, "timestamp", Instant.now().toString()));
    }

    public void broadcastToProject(String projectId, String event, Object data) {
        messagingTemplate.convertAndSend("/topic/project/" + projectId + "/" + event,
                Map.of("data", data, "timestamp", Instant.now().toString()));
    }
}
