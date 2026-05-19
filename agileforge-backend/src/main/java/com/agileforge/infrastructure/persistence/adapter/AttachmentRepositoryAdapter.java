package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Attachment;
import com.agileforge.domain.port.out.AttachmentRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.AttachmentEntity;
import com.agileforge.infrastructure.persistence.repository.JpaAttachmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AttachmentRepositoryAdapter implements AttachmentRepositoryPort {

    private final JpaAttachmentRepository repository;

    public AttachmentRepositoryAdapter(JpaAttachmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Attachment save(Attachment attachment) {
        AttachmentEntity entity = toEntity(attachment);
        AttachmentEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Attachment> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Attachment> findByTicketId(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private AttachmentEntity toEntity(Attachment domain) {
        AttachmentEntity entity = new AttachmentEntity();
        entity.setId(domain.getId());
        entity.setTicketId(domain.getTicketId());
        entity.setFileName(domain.getFileName());
        entity.setFileSize(domain.getFileSize());
        entity.setContentType(domain.getContentType());
        entity.setStoragePath(domain.getStoragePath());
        entity.setUploadedBy(domain.getUploadedBy());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : java.time.Instant.now());
        return entity;
    }

    private Attachment toDomain(AttachmentEntity entity) {
        Attachment domain = new Attachment();
        domain.setId(entity.getId());
        domain.setTicketId(entity.getTicketId());
        domain.setFileName(entity.getFileName());
        domain.setFileSize(entity.getFileSize());
        domain.setContentType(entity.getContentType());
        domain.setStoragePath(entity.getStoragePath());
        domain.setUploadedBy(entity.getUploadedBy());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
