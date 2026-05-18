package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Notification;
import com.agileforge.domain.model.NotificationType;
import com.agileforge.domain.port.out.NotificationRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.NotificationEntity;
import com.agileforge.infrastructure.persistence.repository.JpaNotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final JpaNotificationRepository repository;

    public NotificationRepositoryAdapter(JpaNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = toEntity(notification);
        NotificationEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Notification> findByUserId(UUID userId, int page, int size) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Notification> findUnreadByUserId(UUID userId) {
        return repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void markAsRead(UUID id) {
        repository.markAsRead(id);
    }

    @Override
    public void markAllAsReadByUserId(UUID userId) {
        repository.markAllAsReadByUserId(userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        repository.deleteByUserId(userId);
    }

    private NotificationEntity toEntity(Notification n) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(n.getId());
        entity.setUserId(n.getUserId());
        entity.setType(n.getType().name());
        entity.setTitle(n.getTitle());
        entity.setMessage(n.getMessage());
        entity.setReferenceId(n.getReferenceId());
        entity.setReferenceType(n.getReferenceType());
        entity.setRead(n.isRead());
        entity.setCreatedAt(n.getCreatedAt() != null ? n.getCreatedAt() : Instant.now());
        return entity;
    }

    private Notification toDomain(NotificationEntity e) {
        Notification n = new Notification();
        n.setId(e.getId());
        n.setUserId(e.getUserId());
        n.setType(NotificationType.valueOf(e.getType()));
        n.setTitle(e.getTitle());
        n.setMessage(e.getMessage());
        n.setReferenceId(e.getReferenceId());
        n.setReferenceType(e.getReferenceType());
        n.setRead(e.isRead());
        n.setCreatedAt(e.getCreatedAt());
        return n;
    }
}
