package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.WebhookSubscription;
import com.agileforge.domain.port.out.WebhookRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.WebhookSubscriptionEntity;
import com.agileforge.infrastructure.persistence.repository.JpaWebhookSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WebhookRepositoryAdapter implements WebhookRepositoryPort {

    private final JpaWebhookSubscriptionRepository repository;

    public WebhookRepositoryAdapter(JpaWebhookSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public WebhookSubscription save(WebhookSubscription webhook) {
        WebhookSubscriptionEntity entity = toEntity(webhook);
        WebhookSubscriptionEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<WebhookSubscription> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<WebhookSubscription> findByProjectId(UUID projectId) {
        return repository.findByProjectId(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<WebhookSubscription> findActiveByProjectIdAndEvent(UUID projectId, String event) {
        return repository.findActiveByProjectIdAndEvent(projectId, event).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private WebhookSubscriptionEntity toEntity(WebhookSubscription domain) {
        WebhookSubscriptionEntity entity = new WebhookSubscriptionEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setUrl(domain.getUrl());
        entity.setSecret(domain.getSecret());
        entity.setEvents(domain.getEvents());
        entity.setActive(domain.isActive());
        entity.setLastTriggeredAt(domain.getLastTriggeredAt());
        entity.setFailureCount(domain.getFailureCount());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        return entity;
    }

    private WebhookSubscription toDomain(WebhookSubscriptionEntity entity) {
        WebhookSubscription domain = new WebhookSubscription();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setUrl(entity.getUrl());
        domain.setSecret(entity.getSecret());
        domain.setEvents(entity.getEvents());
        domain.setActive(entity.isActive());
        domain.setLastTriggeredAt(entity.getLastTriggeredAt());
        domain.setFailureCount(entity.getFailureCount());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
