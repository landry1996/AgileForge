package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreateDocumentRequest;
import com.agileforge.application.dto.request.UpdateDocumentRequest;
import com.agileforge.application.dto.response.DocumentResponse;
import com.agileforge.application.dto.response.DocumentTreeResponse;
import com.agileforge.application.dto.response.DocumentVersionResponse;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Document;
import com.agileforge.domain.model.DocumentStatus;
import com.agileforge.domain.model.DocumentType;
import com.agileforge.domain.model.DocumentVersion;
import com.agileforge.domain.port.out.DocumentRepositoryPort;
import com.agileforge.domain.port.out.DocumentVersionRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepositoryPort documentRepository;
    private final DocumentVersionRepositoryPort versionRepository;

    public DocumentService(DocumentRepositoryPort documentRepository,
                           DocumentVersionRepositoryPort versionRepository) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
    }

    public DocumentResponse createDocument(UUID projectId, UUID userId, CreateDocumentRequest request) {
        DocumentType docType = request.docType() != null ? DocumentType.valueOf(request.docType()) : DocumentType.PAGE;
        DocumentStatus status = request.status() != null ? DocumentStatus.valueOf(request.status()) : DocumentStatus.DRAFT;

        Document document = new Document(projectId, request.title(), request.content(), docType, status,
                request.parentId(), userId);
        Document saved = documentRepository.save(document);

        // Save initial version
        DocumentVersion firstVersion = new DocumentVersion(
                saved.getId(), saved.getTitle(), saved.getContent(), 1, userId, "Initial version"
        );
        versionRepository.save(firstVersion);

        log.info("Document created: '{}' in project {}", saved.getTitle(), projectId);
        return toResponse(saved, List.of());
    }

    public DocumentResponse updateDocument(UUID docId, UUID userId, UpdateDocumentRequest request) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document", docId));

        if (request.title() != null) {
            document.setTitle(request.title());
        }
        if (request.content() != null) {
            document.setContent(request.content());
        }
        if (request.status() != null) {
            document.setStatus(DocumentStatus.valueOf(request.status()));
        }

        document.setVersion(document.getVersion() + 1);
        document.setLastEditedBy(userId);

        Document saved = documentRepository.save(document);

        // Save version history
        String changeSummary = request.changeSummary() != null ? request.changeSummary() : "Updated document";
        DocumentVersion version = new DocumentVersion(
                saved.getId(), saved.getTitle(), saved.getContent(), saved.getVersion(), userId, changeSummary
        );
        versionRepository.save(version);

        List<UUID> linkedTicketIds = documentRepository.findLinkedTicketIds(docId);
        log.info("Document updated: '{}' to version {}", saved.getTitle(), saved.getVersion());
        return toResponse(saved, linkedTicketIds);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(UUID docId) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document", docId));
        List<UUID> linkedTicketIds = documentRepository.findLinkedTicketIds(docId);
        return toResponse(document, linkedTicketIds);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getByProject(UUID projectId) {
        return documentRepository.findByProjectId(projectId).stream()
                .map(doc -> {
                    List<UUID> linkedTicketIds = documentRepository.findLinkedTicketIds(doc.getId());
                    return toResponse(doc, linkedTicketIds);
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentTreeResponse> getDocumentTree(UUID projectId) {
        List<Document> allDocs = documentRepository.findByProjectId(projectId);

        // Group by parent ID
        Map<UUID, List<Document>> childrenMap = allDocs.stream()
                .filter(doc -> doc.getParentId() != null)
                .collect(Collectors.groupingBy(Document::getParentId));

        // Build tree from root documents (those without parent)
        List<Document> rootDocs = allDocs.stream()
                .filter(doc -> doc.getParentId() == null)
                .toList();

        return rootDocs.stream()
                .map(doc -> buildTreeNode(doc, childrenMap))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> getVersions(UUID docId) {
        documentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document", docId));

        return versionRepository.findByDocumentId(docId).stream()
                .map(this::toVersionResponse)
                .toList();
    }

    public DocumentResponse restoreVersion(UUID docId, UUID versionId, UUID userId) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document", docId));

        DocumentVersion targetVersion = versionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("DocumentVersion", versionId));

        document.setTitle(targetVersion.getTitle());
        document.setContent(targetVersion.getContent());
        document.setVersion(document.getVersion() + 1);
        document.setLastEditedBy(userId);

        Document saved = documentRepository.save(document);

        // Save as new version
        DocumentVersion restoredVersion = new DocumentVersion(
                saved.getId(), saved.getTitle(), saved.getContent(), saved.getVersion(), userId,
                "Restored from version " + targetVersion.getVersion()
        );
        versionRepository.save(restoredVersion);

        List<UUID> linkedTicketIds = documentRepository.findLinkedTicketIds(docId);
        log.info("Document '{}' restored to version {} content", saved.getTitle(), targetVersion.getVersion());
        return toResponse(saved, linkedTicketIds);
    }

    public void deleteDocument(UUID docId) {
        documentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document", docId));
        documentRepository.delete(docId);
        log.info("Document deleted: {}", docId);
    }

    public void linkToTicket(UUID docId, UUID ticketId) {
        documentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document", docId));
        documentRepository.linkToTicket(docId, ticketId);
        log.info("Document {} linked to ticket {}", docId, ticketId);
    }

    public void unlinkFromTicket(UUID docId, UUID ticketId) {
        documentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document", docId));
        documentRepository.unlinkFromTicket(docId, ticketId);
        log.info("Document {} unlinked from ticket {}", docId, ticketId);
    }

    public DocumentResponse reorder(UUID docId, int newPosition) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document", docId));
        document.setPosition(newPosition);
        Document saved = documentRepository.save(document);
        List<UUID> linkedTicketIds = documentRepository.findLinkedTicketIds(docId);
        log.info("Document '{}' reordered to position {}", saved.getTitle(), newPosition);
        return toResponse(saved, linkedTicketIds);
    }

    private DocumentTreeResponse buildTreeNode(Document doc, Map<UUID, List<Document>> childrenMap) {
        List<Document> children = childrenMap.getOrDefault(doc.getId(), new ArrayList<>());
        List<DocumentTreeResponse> childNodes = children.stream()
                .map(child -> buildTreeNode(child, childrenMap))
                .toList();

        return new DocumentTreeResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getDocType().name(),
                doc.getPosition(),
                childNodes
        );
    }

    private DocumentResponse toResponse(Document doc, List<UUID> linkedTicketIds) {
        return new DocumentResponse(
                doc.getId(),
                doc.getProjectId(),
                doc.getParentId(),
                doc.getTitle(),
                doc.getContent(),
                doc.getDocType().name(),
                doc.getStatus().name(),
                doc.getPosition(),
                doc.getAuthorId(),
                doc.getLastEditedBy(),
                doc.getVersion(),
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                linkedTicketIds
        );
    }

    private DocumentVersionResponse toVersionResponse(DocumentVersion version) {
        return new DocumentVersionResponse(
                version.getId(),
                version.getDocumentId(),
                version.getTitle(),
                version.getVersion(),
                version.getEditedBy(),
                version.getChangeSummary(),
                version.getCreatedAt()
        );
    }
}
