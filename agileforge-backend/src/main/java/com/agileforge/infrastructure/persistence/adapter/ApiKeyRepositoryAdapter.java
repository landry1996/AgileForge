package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.ApiKey;
import com.agileforge.domain.port.out.ApiKeyRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.ApiKeyEntity;
import com.agileforge.infrastructure.persistence.repository.JpaApiKeyRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ApiKeyRepositoryAdapter implements ApiKeyRepositoryPort {

    private final JpaApiKeyRepository repository;

    public ApiKeyRepositoryAdapter(JpaApiKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApiKey save(ApiKey apiKey) {
        ApiKeyEntity entity = toEntity(apiKey);
        ApiKeyEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ApiKey> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ApiKey> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<ApiKey> findByKeyPrefix(String keyPrefix) {
        return repository.findByKeyPrefixAndActiveTrue(keyPrefix).map(this::toDomain);
    }

    @Override
    public void delete(UUID id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setActive(false);
            repository.save(entity);
        });
    }

    private ApiKeyEntity toEntity(ApiKey domain) {
        ApiKeyEntity entity = new ApiKeyEntity();
        entity.setId(domain.getId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setName(domain.getName());
        entity.setKeyHash(domain.getKeyHash());
        entity.setKeyPrefix(domain.getKeyPrefix());
        entity.setPermissions(domain.getPermissions());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setLastUsedAt(domain.getLastUsedAt());
        entity.setActive(domain.isActive());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        return entity;
    }

    private ApiKey toDomain(ApiKeyEntity entity) {
        ApiKey domain = new ApiKey();
        domain.setId(entity.getId());
        domain.setOrganizationId(entity.getOrganizationId());
        domain.setName(entity.getName());
        domain.setKeyHash(entity.getKeyHash());
        domain.setKeyPrefix(entity.getKeyPrefix());
        domain.setPermissions(entity.getPermissions());
        domain.setExpiresAt(entity.getExpiresAt());
        domain.setLastUsedAt(entity.getLastUsedAt());
        domain.setActive(entity.isActive());
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
