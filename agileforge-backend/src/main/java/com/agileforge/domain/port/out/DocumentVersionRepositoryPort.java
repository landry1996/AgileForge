package com.agileforge.domain.port.out;

import com.agileforge.domain.model.DocumentVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepositoryPort {

    DocumentVersion save(DocumentVersion version);

    Optional<DocumentVersion> findById(UUID id);

    List<DocumentVersion> findByDocumentId(UUID documentId);
}
