package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreateWebhookRequest;
import com.agileforge.application.dto.response.WebhookResponse;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.WebhookSubscription;
import com.agileforge.domain.port.out.WebhookRepositoryPort;
import com.agileforge.infrastructure.webhook.WebhookDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookRepositoryPort webhookRepository;
    private final WebhookDeliveryService deliveryService;

    public WebhookService(WebhookRepositoryPort webhookRepository, WebhookDeliveryService deliveryService) {
        this.webhookRepository = webhookRepository;
        this.deliveryService = deliveryService;
    }

    public WebhookResponse createWebhook(UUID projectId, CreateWebhookRequest request) {
        String events = request.events() != null ? String.join(",", request.events()) : "";

        WebhookSubscription webhook = new WebhookSubscription(projectId, request.url(), request.secret(), events);
        WebhookSubscription saved = webhookRepository.save(webhook);

        log.info("Webhook created: {} for project {}", saved.getId(), projectId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WebhookResponse> getByProject(UUID projectId) {
        return webhookRepository.findByProjectId(projectId).stream()
                .map(this::toResponse).toList();
    }

    public WebhookResponse updateWebhook(UUID webhookId, CreateWebhookRequest request) {
        WebhookSubscription webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new EntityNotFoundException("Webhook", webhookId));

        if (request.url() != null) webhook.setUrl(request.url());
        if (request.secret() != null) webhook.setSecret(request.secret());
        if (request.events() != null) webhook.setEvents(String.join(",", request.events()));

        WebhookSubscription saved = webhookRepository.save(webhook);
        log.info("Webhook updated: {}", webhookId);
        return toResponse(saved);
    }

    public void deleteWebhook(UUID webhookId) {
        webhookRepository.findById(webhookId)
                .orElseThrow(() -> new EntityNotFoundException("Webhook", webhookId));
        webhookRepository.delete(webhookId);
        log.info("Webhook deleted: {}", webhookId);
    }

    public void triggerWebhook(UUID projectId, String event, Map<String, Object> payload) {
        List<WebhookSubscription> subscriptions = webhookRepository.findActiveByProjectIdAndEvent(projectId, event);

        for (WebhookSubscription subscription : subscriptions) {
            log.info("Webhook triggered: {} -> {} (event: {})", subscription.getId(), subscription.getUrl(), event);
            deliveryService.deliver(subscription, event, payload);
        }
    }

    public void testWebhook(UUID webhookId) {
        WebhookSubscription webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new EntityNotFoundException("Webhook", webhookId));

        // Log test ping (actual HTTP delivery would be implemented)
        log.info("Test webhook ping sent to: {} (webhook: {})", webhook.getUrl(), webhookId);
        webhook.setLastTriggeredAt(Instant.now());
        webhookRepository.save(webhook);
    }

    private WebhookResponse toResponse(WebhookSubscription webhook) {
        List<String> eventList = webhook.getEvents() != null && !webhook.getEvents().isBlank()
                ? Arrays.asList(webhook.getEvents().split(","))
                : List.of();

        return new WebhookResponse(
                webhook.getId(),
                webhook.getProjectId(),
                webhook.getUrl(),
                eventList,
                webhook.isActive(),
                webhook.getLastTriggeredAt(),
                webhook.getFailureCount(),
                webhook.getCreatedAt()
        );
    }
}
