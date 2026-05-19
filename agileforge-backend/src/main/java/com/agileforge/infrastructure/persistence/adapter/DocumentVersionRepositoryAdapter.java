package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.DocumentVersion;
import com.agileforge.domain.port.out.DocumentVersionRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.DocumentVersionEntity;
import com.agileforge.infrastructure.persistence.repository.JpaDocumentVersionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DocumentVersionRepositoryAdapter implements DocumentVersionRepositoryPort {

    private final JpaDocumentVersionRepository versionRepository;

    public DocumentVersionRepositoryAdapter(JpaDocumentVersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    @Override
    public DocumentVersion save(DocumentVersion version) {
        DocumentVersionEntity entity = toEntity(version);
        DocumentVersionEntity saved = versionRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<DocumentVersion> findById(UUID id) {
        return versionRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<DocumentVersion> findByDocumentId(UUID documentId) {
        return versionRepository.findByDocumentIdOrderByVersionDesc(documentId).stream()
                .map(this::toDomain).toList();
    }

    private DocumentVersionEntity toEntity(DocumentVersion domain) {
        DocumentVersionEntity entity = new DocumentVersionEntity();
        entity.setId(domain.getId());
        entity.setDocumentId(domain.getDocumentId());
        entity.setTitle(domain.getTitle());
        entity.setContent(domain.getContent());
        entity.setVersion(domain.getVersion());
        entity.setEditedBy(domain.getEditedBy());
        entity.setChangeSummary(domain.getChangeSummary());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        return entity;
    }

    private DocumentVersion toDomain(DocumentVersionEntity entity) {
        DocumentVersion domain = new DocumentVersion();
        domain.setId(entity.getId());
        domain.setDocumentId(entity.getDocumentId());
        domain.setTitle(entity.getTitle());
        domain.setContent(entity.getContent());
        domain.setVersion(entity.getVersion());
        domain.setEditedBy(entity.getEditedBy());
        domain.setChangeSummary(entity.getChangeSummary());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
