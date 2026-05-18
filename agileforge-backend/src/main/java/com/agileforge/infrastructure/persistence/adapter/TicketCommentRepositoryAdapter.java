package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.TicketComment;
import com.agileforge.domain.port.out.TicketCommentRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.TicketCommentEntity;
import com.agileforge.infrastructure.persistence.repository.JpaTicketCommentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TicketCommentRepositoryAdapter implements TicketCommentRepositoryPort {

    private final JpaTicketCommentRepository repository;

    public TicketCommentRepositoryAdapter(JpaTicketCommentRepository repository) {
        this.repository = repository;
    }

    @Override
    public TicketComment save(TicketComment comment) {
        TicketCommentEntity entity = toEntity(comment);
        TicketCommentEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TicketComment> findById(UUID id) {
        return repository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public List<TicketComment> findByTicketId(UUID ticketId) {
        return repository.findByTicketIdAndDeletedFalseOrderByCreatedAtAsc(ticketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            repository.save(entity);
        });
    }

    private TicketCommentEntity toEntity(TicketComment domain) {
        TicketCommentEntity entity = new TicketCommentEntity();
        entity.setId(domain.getId());
        entity.setTicketId(domain.getTicketId());
        entity.setAuthorId(domain.getAuthorId());
        entity.setContent(domain.getContent());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : java.time.Instant.now());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private TicketComment toDomain(TicketCommentEntity entity) {
        TicketComment domain = new TicketComment();
        domain.setId(entity.getId());
        domain.setTicketId(entity.getTicketId());
        domain.setAuthorId(entity.getAuthorId());
        domain.setContent(entity.getContent());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
