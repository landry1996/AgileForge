package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.response.AttachmentResponse;
import com.agileforge.application.service.AttachmentService;
import com.agileforge.domain.model.Attachment;
import com.agileforge.domain.port.out.FileStoragePort;
import com.agileforge.domain.port.out.UserRepositoryPort;
import com.agileforge.infrastructure.storage.LocalFileStorageAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Attachments", description = "File attachment endpoints")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final LocalFileStorageAdapter fileStorage;
    private final UserRepositoryPort userRepository;

    public AttachmentController(AttachmentService attachmentService,
                                LocalFileStorageAdapter fileStorage,
                                UserRepositoryPort userRepository) {
        this.attachmentService = attachmentService;
        this.fileStorage = fileStorage;
        this.userRepository = userRepository;
    }

    @PostMapping("/tickets/{ticketId}/attachments")
    @Operation(summary = "Upload a file attachment to a ticket")
    public ResponseEntity<AttachmentResponse> upload(@PathVariable UUID ticketId,
                                                     @RequestParam("file") MultipartFile file,
                                                     Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        Attachment attachment = attachmentService.upload(ticketId, file, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(attachment));
    }

    @GetMapping("/tickets/{ticketId}/attachments")
    @Operation(summary = "Get all attachments for a ticket")
    public ResponseEntity<List<AttachmentResponse>> getByTicket(@PathVariable UUID ticketId) {
        List<AttachmentResponse> attachments = attachmentService.getByTicketId(ticketId).stream()
                .map(this::toResponse).toList();
        return ResponseEntity.ok(attachments);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<Void> delete(@PathVariable UUID attachmentId, Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        attachmentService.delete(attachmentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(summary = "Download an attachment")
    public ResponseEntity<Resource> download(@PathVariable UUID attachmentId) {
        Attachment attachment = attachmentService.getById(attachmentId);
        Path filePath = fileStorage.getFilePath(attachment.getStoragePath());

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private AttachmentResponse toResponse(Attachment a) {
        return new AttachmentResponse(
                a.getId(),
                a.getTicketId(),
                a.getFileName(),
                a.getFileSize(),
                a.getContentType(),
                a.getUploadedBy(),
                a.getCreatedAt(),
                fileStorage.getUrl(a.getStoragePath())
        );
    }
}
