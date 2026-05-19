package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Document;
import com.agileforge.domain.model.DocumentStatus;
import com.agileforge.domain.model.DocumentType;
import com.agileforge.domain.port.out.DocumentRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.DocumentEntity;
import com.agileforge.infrastructure.persistence.entity.DocumentTicketLinkEntity;
import com.agileforge.infrastructure.persistence.entity.DocumentTicketLinkId;
import com.agileforge.infrastructure.persistence.repository.JpaDocumentRepository;
import com.agileforge.infrastructure.persistence.repository.JpaDocumentTicketLinkRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DocumentRepositoryAdapter implements DocumentRepositoryPort {

    private final JpaDocumentRepository documentRepository;
    private final JpaDocumentTicketLinkRepository ticketLinkRepository;

    public DocumentRepositoryAdapter(JpaDocumentRepository documentRepository,
                                     JpaDocumentTicketLinkRepository ticketLinkRepository) {
        this.documentRepository = documentRepository;
        this.ticketLinkRepository = ticketLinkRepository;
    }

    @Override
    public Document save(Document document) {
        DocumentEntity entity = toEntity(document);
        DocumentEntity saved = documentRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Document> findById(UUID id) {
        return documentRepository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public List<Document> findByProjectId(UUID projectId) {
        return documentRepository.findByProjectIdAndDeletedFalseOrderByPositionAsc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Document> findByParentId(UUID parentId) {
        return documentRepository.findByParentIdAndDeletedFalseOrderByPositionAsc(parentId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Document> findByProjectIdAndType(UUID projectId, DocumentType docType) {
        return documentRepository.findByProjectIdAndDocTypeAndDeletedFalseOrderByPositionAsc(projectId, docType.name()).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        documentRepository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            documentRepository.save(entity);
        });
    }

    @Override
    public void linkToTicket(UUID documentId, UUID ticketId) {
        DocumentTicketLinkId id = new DocumentTicketLinkId(documentId, ticketId);
        if (!ticketLinkRepository.existsById(id)) {
            ticketLinkRepository.save(new DocumentTicketLinkEntity(id));
        }
    }

    @Override
    public void unlinkFromTicket(UUID documentId, UUID ticketId) {
        DocumentTicketLinkId id = new DocumentTicketLinkId(documentId, ticketId);
        ticketLinkRepository.deleteById(id);
    }

    @Override
    public List<UUID> findLinkedTicketIds(UUID documentId) {
        return ticketLinkRepository.findTicketIdsByDocumentId(documentId);
    }

    private DocumentEntity toEntity(Document domain) {
        DocumentEntity entity = new DocumentEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setParentId(domain.getParentId());
        entity.setTitle(domain.getTitle());
        entity.setContent(domain.getContent());
        entity.setDocType(domain.getDocType().name());
        entity.setStatus(domain.getStatus().name());
        entity.setPosition(domain.getPosition());
        entity.setAuthorId(domain.getAuthorId());
        entity.setLastEditedBy(domain.getLastEditedBy());
        entity.setVersion(domain.getVersion());
        return entity;
    }

    private Document toDomain(DocumentEntity entity) {
        Document domain = new Document();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setParentId(entity.getParentId());
        domain.setTitle(entity.getTitle());
        domain.setContent(entity.getContent());
        domain.setDocType(DocumentType.valueOf(entity.getDocType()));
        domain.setStatus(DocumentStatus.valueOf(entity.getStatus()));
        domain.setPosition(entity.getPosition());
        domain.setAuthorId(entity.getAuthorId());
        domain.setLastEditedBy(entity.getLastEditedBy());
        domain.setVersion(entity.getVersion());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
