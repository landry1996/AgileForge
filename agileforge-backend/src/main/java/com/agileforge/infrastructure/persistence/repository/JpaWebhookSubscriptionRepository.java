package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.WebhookSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaWebhookSubscriptionRepository extends JpaRepository<WebhookSubscriptionEntity, UUID> {

    List<WebhookSubscriptionEntity> findByProjectIdAndActiveTrue(UUID projectId);

    List<WebhookSubscriptionEntity> findByProjectId(UUID projectId);

    @Query("SELECT w FROM WebhookSubscriptionEntity w WHERE w.projectId = :projectId AND w.active = true AND w.events LIKE %:event%")
    List<WebhookSubscriptionEntity> findActiveByProjectIdAndEvent(@Param("projectId") UUID projectId, @Param("event") String event);
}
