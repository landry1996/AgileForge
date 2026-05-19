package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreateApiKeyRequest;
import com.agileforge.application.dto.response.ApiKeyCreatedResponse;
import com.agileforge.application.dto.response.ApiKeyResponse;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.ApiKey;
import com.agileforge.domain.port.out.ApiKeyRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);
    private static final String KEY_PREFIX = "af_";
    private static final int KEY_LENGTH = 32;

    private final ApiKeyRepositoryPort apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(ApiKeyRepositoryPort apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public ApiKeyCreatedResponse createApiKey(UUID organizationId, UUID userId, CreateApiKeyRequest request) {
        // Generate a random API key
        String rawKey = generateRandomKey();
        String keyPrefix = rawKey.substring(0, 8);
        String keyHash = hashKey(rawKey);

        String permissions = request.permissions() != null ? String.join(",", request.permissions()) : "";

        ApiKey apiKey = new ApiKey(organizationId, request.name(), keyHash, keyPrefix, permissions, userId);
        if (request.expiresAt() != null) {
            apiKey.setExpiresAt(request.expiresAt());
        }

        ApiKey saved = apiKeyRepository.save(apiKey);
        log.info("API key created: {} (prefix: {}) for organization {}", saved.getId(), keyPrefix, organizationId);

        // Return full key only on creation
        return new ApiKeyCreatedResponse(saved.getId(), saved.getName(), rawKey, keyPrefix);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getByOrganization(UUID organizationId) {
        return apiKeyRepository.findByOrganizationId(organizationId).stream()
                .map(this::toResponse).toList();
    }

    public void revokeKey(UUID keyId) {
        apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new EntityNotFoundException("ApiKey", keyId));
        apiKeyRepository.delete(keyId);
        log.info("API key revoked: {}", keyId);
    }

    @Transactional(readOnly = true)
    public ApiKey validateKey(String keyString) {
        if (keyString == null || keyString.length() < 8) {
            return null;
        }

        String prefix = keyString.substring(0, 8);
        String keyHash = hashKey(keyString);

        return apiKeyRepository.findByKeyPrefix(prefix)
                .filter(key -> key.getKeyHash().equals(keyHash))
                .filter(ApiKey::isActive)
                .filter(key -> key.getExpiresAt() == null || key.getExpiresAt().isAfter(java.time.Instant.now()))
                .orElse(null);
    }

    private String generateRandomKey() {
        byte[] bytes = new byte[KEY_LENGTH];
        secureRandom.nextBytes(bytes);
        return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private ApiKeyResponse toResponse(ApiKey apiKey) {
        List<String> permissionList = apiKey.getPermissions() != null && !apiKey.getPermissions().isBlank()
                ? Arrays.asList(apiKey.getPermissions().split(","))
                : List.of();

        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getKeyPrefix(),
                permissionList,
                apiKey.getExpiresAt(),
                apiKey.getLastUsedAt(),
                apiKey.isActive(),
                apiKey.getCreatedAt()
        );
    }
}
