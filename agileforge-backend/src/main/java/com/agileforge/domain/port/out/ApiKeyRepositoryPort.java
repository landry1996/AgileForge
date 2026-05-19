package com.agileforge.domain.port.out;

import com.agileforge.domain.model.ApiKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepositoryPort {

    ApiKey save(ApiKey apiKey);

    Optional<ApiKey> findById(UUID id);

    List<ApiKey> findByOrganizationId(UUID organizationId);

    Optional<ApiKey> findByKeyPrefix(String keyPrefix);

    void delete(UUID id);
}
