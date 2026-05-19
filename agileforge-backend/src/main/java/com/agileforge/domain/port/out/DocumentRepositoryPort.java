package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Document;
import com.agileforge.domain.model.DocumentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepositoryPort {

    Document save(Document document);

    Optional<Document> findById(UUID id);

    List<Document> findByProjectId(UUID projectId);

    List<Document> findByParentId(UUID parentId);

    List<Document> findByProjectIdAndType(UUID projectId, DocumentType docType);

    void delete(UUID id);

    void linkToTicket(UUID documentId, UUID ticketId);

    void unlinkFromTicket(UUID documentId, UUID ticketId);

    List<UUID> findLinkedTicketIds(UUID documentId);
}
