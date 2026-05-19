package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreateDocumentRequest;
import com.agileforge.application.dto.request.UpdateDocumentRequest;
import com.agileforge.application.dto.response.DocumentResponse;
import com.agileforge.application.dto.response.DocumentTreeResponse;
import com.agileforge.application.dto.response.DocumentVersionResponse;
import com.agileforge.application.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Documentation", description = "Document management endpoints")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/projects/{projectId}/documents")
    @Operation(summary = "Create a new document")
    public ResponseEntity<DocumentResponse> create(@PathVariable UUID projectId,
                                                   @Valid @RequestBody CreateDocumentRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        DocumentResponse response = documentService.createDocument(projectId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/documents")
    @Operation(summary = "Get all documents for a project")
    public ResponseEntity<List<DocumentResponse>> getByProject(@PathVariable UUID projectId) {
        List<DocumentResponse> documents = documentService.getByProject(projectId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/projects/{projectId}/documents/tree")
    @Operation(summary = "Get document tree structure for a project")
    public ResponseEntity<List<DocumentTreeResponse>> getDocumentTree(@PathVariable UUID projectId) {
        List<DocumentTreeResponse> tree = documentService.getDocumentTree(projectId);
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/documents/{docId}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentResponse> getById(@PathVariable UUID docId) {
        DocumentResponse response = documentService.getById(docId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/documents/{docId}")
    @Operation(summary = "Update a document")
    public ResponseEntity<DocumentResponse> update(@PathVariable UUID docId,
                                                   @Valid @RequestBody UpdateDocumentRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        DocumentResponse response = documentService.updateDocument(docId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/documents/{docId}")
    @Operation(summary = "Delete a document (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID docId) {
        documentService.deleteDocument(docId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/documents/{docId}/versions")
    @Operation(summary = "Get version history of a document")
    public ResponseEntity<List<DocumentVersionResponse>> getVersions(@PathVariable UUID docId) {
        List<DocumentVersionResponse> versions = documentService.getVersions(docId);
        return ResponseEntity.ok(versions);
    }

    @PostMapping("/documents/{docId}/restore/{versionId}")
    @Operation(summary = "Restore a document to a previous version")
    public ResponseEntity<DocumentResponse> restoreVersion(@PathVariable UUID docId,
                                                           @PathVariable UUID versionId,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        DocumentResponse response = documentService.restoreVersion(docId, versionId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/documents/{docId}/link-ticket/{ticketId}")
    @Operation(summary = "Link a document to a ticket")
    public ResponseEntity<Void> linkToTicket(@PathVariable UUID docId, @PathVariable UUID ticketId) {
        documentService.linkToTicket(docId, ticketId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/documents/{docId}/link-ticket/{ticketId}")
    @Operation(summary = "Unlink a document from a ticket")
    public ResponseEntity<Void> unlinkFromTicket(@PathVariable UUID docId, @PathVariable UUID ticketId) {
        documentService.unlinkFromTicket(docId, ticketId);
        return ResponseEntity.noContent().build();
    }
}
