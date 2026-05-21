package com.agileforge.infrastructure.webhook;

import com.agileforge.domain.model.WebhookSubscription;
import com.agileforge.domain.port.out.WebhookRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

@Service
public class WebhookDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryService.class);
    private static final int MAX_RETRIES = 3;
    private static final int MAX_FAILURES_BEFORE_DISABLE = 10;

    private final WebhookRepositoryPort webhookRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WebhookDeliveryService(WebhookRepositoryPort webhookRepository, ObjectMapper objectMapper) {
        this.webhookRepository = webhookRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Async("webhookExecutor")
    public void deliver(WebhookSubscription subscription, String event, Map<String, Object> payload) {
        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Failed to serialize webhook payload for {}: {}", subscription.getId(), e.getMessage());
            return;
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(subscription.getUrl()))
                        .timeout(Duration.ofSeconds(30))
                        .header("Content-Type", "application/json")
                        .header("X-Webhook-Event", event)
                        .header("X-Webhook-Delivery", subscription.getId().toString())
                        .POST(HttpRequest.BodyPublishers.ofString(body));

                if (subscription.getSecret() != null && !subscription.getSecret().isBlank()) {
                    String signature = computeHmacSha256(body, subscription.getSecret());
                    requestBuilder.header("X-Webhook-Signature", "sha256=" + signature);
                }

                HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    subscription.setLastTriggeredAt(Instant.now());
                    subscription.setFailureCount(0);
                    webhookRepository.save(subscription);
                    log.info("Webhook delivered: {} -> {} (event: {}, attempt: {})",
                            subscription.getId(), subscription.getUrl(), event, attempt);
                    return;
                }

                log.warn("Webhook delivery failed (HTTP {}): {} -> {} (attempt {}/{})",
                        response.statusCode(), subscription.getId(), subscription.getUrl(), attempt, MAX_RETRIES);

            } catch (Exception e) {
                log.warn("Webhook delivery error: {} -> {} (attempt {}/{}): {}",
                        subscription.getId(), subscription.getUrl(), attempt, MAX_RETRIES, e.getMessage());
            }

            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        // All retries exhausted
        int newFailureCount = subscription.getFailureCount() + 1;
        subscription.setFailureCount(newFailureCount);
        subscription.setLastTriggeredAt(Instant.now());

        if (newFailureCount >= MAX_FAILURES_BEFORE_DISABLE) {
            subscription.setActive(false);
            log.error("Webhook disabled after {} consecutive failures: {} ({})",
                    newFailureCount, subscription.getId(), subscription.getUrl());
        }

        webhookRepository.save(subscription);
    }

    private String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
}
