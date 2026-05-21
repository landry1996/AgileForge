package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreateWebhookRequest;
import com.agileforge.application.dto.response.WebhookResponse;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.WebhookSubscription;
import com.agileforge.domain.port.out.WebhookRepositoryPort;
import com.agileforge.infrastructure.webhook.WebhookDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock private WebhookRepositoryPort webhookRepository;
    @Mock private WebhookDeliveryService deliveryService;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void shouldCreateWebhook() {
        UUID projectId = UUID.randomUUID();
        WebhookSubscription webhook = new WebhookSubscription(projectId, "https://example.com/hook", "secret", "ticket.created");
        webhook.setId(UUID.randomUUID());
        webhook.setActive(true);
        webhook.setCreatedAt(Instant.now());
        when(webhookRepository.save(any())).thenReturn(webhook);

        WebhookResponse result = webhookService.createWebhook(projectId,
                new CreateWebhookRequest("https://example.com/hook", "secret", List.of("ticket.created")));

        assertNotNull(result);
        assertEquals("https://example.com/hook", result.url());
    }

    @Test
    void shouldGetByProject() {
        UUID projectId = UUID.randomUUID();
        when(webhookRepository.findByProjectId(projectId)).thenReturn(List.of());

        List<WebhookResponse> result = webhookService.getByProject(projectId);

        assertNotNull(result);
    }

    @Test
    void shouldUpdateWebhook() {
        UUID webhookId = UUID.randomUUID();
        WebhookSubscription webhook = new WebhookSubscription(UUID.randomUUID(), "https://old.com", null, "");
        webhook.setId(webhookId);
        webhook.setActive(true);
        webhook.setCreatedAt(Instant.now());
        when(webhookRepository.findById(webhookId)).thenReturn(Optional.of(webhook));
        when(webhookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WebhookResponse result = webhookService.updateWebhook(webhookId,
                new CreateWebhookRequest("https://new.com", null, List.of("sprint.completed")));

        assertEquals("https://new.com", result.url());
    }

    @Test
    void shouldDeleteWebhook() {
        UUID webhookId = UUID.randomUUID();
        WebhookSubscription webhook = new WebhookSubscription(UUID.randomUUID(), "https://test.com", null, "");
        webhook.setId(webhookId);
        when(webhookRepository.findById(webhookId)).thenReturn(Optional.of(webhook));

        webhookService.deleteWebhook(webhookId);

        verify(webhookRepository).delete(webhookId);
    }

    @Test
    void shouldThrowWhenDeletingNonExistent() {
        UUID webhookId = UUID.randomUUID();
        when(webhookRepository.findById(webhookId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> webhookService.deleteWebhook(webhookId));
    }

    @Test
    void shouldTriggerWebhookWithDeliveryService() {
        UUID projectId = UUID.randomUUID();
        WebhookSubscription sub = new WebhookSubscription(projectId, "https://test.com", "secret", "ticket.created");
        sub.setId(UUID.randomUUID());
        when(webhookRepository.findActiveByProjectIdAndEvent(projectId, "ticket.created")).thenReturn(List.of(sub));

        webhookService.triggerWebhook(projectId, "ticket.created", Map.of("ticketId", "123"));

        verify(deliveryService).deliver(eq(sub), eq("ticket.created"), any());
    }

    @Test
    void shouldTestWebhook() {
        UUID webhookId = UUID.randomUUID();
        WebhookSubscription webhook = new WebhookSubscription(UUID.randomUUID(), "https://test.com", null, "");
        webhook.setId(webhookId);
        webhook.setCreatedAt(Instant.now());
        when(webhookRepository.findById(webhookId)).thenReturn(Optional.of(webhook));
        when(webhookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        webhookService.testWebhook(webhookId);

        verify(webhookRepository).save(any());
    }
}
