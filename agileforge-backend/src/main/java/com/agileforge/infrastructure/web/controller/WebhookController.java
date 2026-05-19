package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateWebhookRequest;
import com.agileforge.application.dto.response.WebhookResponse;
import com.agileforge.application.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Webhooks", description = "Webhook subscription management endpoints")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/projects/{projectId}/webhooks")
    @Operation(summary = "Create a webhook subscription")
    public ResponseEntity<WebhookResponse> create(@PathVariable UUID projectId,
                                                  @Valid @RequestBody CreateWebhookRequest request) {
        WebhookResponse response = webhookService.createWebhook(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/webhooks")
    @Operation(summary = "Get all webhooks for a project")
    public ResponseEntity<List<WebhookResponse>> getByProject(@PathVariable UUID projectId) {
        List<WebhookResponse> webhooks = webhookService.getByProject(projectId);
        return ResponseEntity.ok(webhooks);
    }

    @PutMapping("/webhooks/{webhookId}")
    @Operation(summary = "Update a webhook subscription")
    public ResponseEntity<WebhookResponse> update(@PathVariable UUID webhookId,
                                                  @Valid @RequestBody CreateWebhookRequest request) {
        WebhookResponse response = webhookService.updateWebhook(webhookId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/webhooks/{webhookId}")
    @Operation(summary = "Delete a webhook subscription")
    public ResponseEntity<Void> delete(@PathVariable UUID webhookId) {
        webhookService.deleteWebhook(webhookId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/webhooks/{webhookId}/test")
    @Operation(summary = "Send a test ping to a webhook")
    public ResponseEntity<Void> test(@PathVariable UUID webhookId) {
        webhookService.testWebhook(webhookId);
        return ResponseEntity.ok().build();
    }
}
