package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findByUserId(UUID userId, int page, int size);

    List<Notification> findUnreadByUserId(UUID userId);

    long countUnreadByUserId(UUID userId);

    void markAsRead(UUID id);

    void markAllAsReadByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
