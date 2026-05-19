package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateApiKeyRequest;
import com.agileforge.application.dto.response.ApiKeyCreatedResponse;
import com.agileforge.application.dto.response.ApiKeyResponse;
import com.agileforge.application.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "API Keys", description = "API key management endpoints")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping("/organizations/{orgId}/api-keys")
    @Operation(summary = "Create a new API key")
    public ResponseEntity<ApiKeyCreatedResponse> create(@PathVariable UUID orgId,
                                                        @Valid @RequestBody CreateApiKeyRequest request,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ApiKeyCreatedResponse response = apiKeyService.createApiKey(orgId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/organizations/{orgId}/api-keys")
    @Operation(summary = "List API keys for an organization")
    public ResponseEntity<List<ApiKeyResponse>> getByOrganization(@PathVariable UUID orgId) {
        List<ApiKeyResponse> keys = apiKeyService.getByOrganization(orgId);
        return ResponseEntity.ok(keys);
    }

    @DeleteMapping("/api-keys/{keyId}")
    @Operation(summary = "Revoke an API key")
    public ResponseEntity<Void> revoke(@PathVariable UUID keyId) {
        apiKeyService.revokeKey(keyId);
        return ResponseEntity.noContent().build();
    }
}
