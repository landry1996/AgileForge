package com.agileforge.domain.port.out;

import com.agileforge.domain.model.WebhookSubscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookRepositoryPort {

    WebhookSubscription save(WebhookSubscription webhook);

    Optional<WebhookSubscription> findById(UUID id);

    List<WebhookSubscription> findByProjectId(UUID projectId);

    List<WebhookSubscription> findActiveByProjectIdAndEvent(UUID projectId, String event);

    void delete(UUID id);
}
