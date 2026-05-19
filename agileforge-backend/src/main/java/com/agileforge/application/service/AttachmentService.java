package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Attachment;
import com.agileforge.domain.port.out.AttachmentRepositoryPort;
import com.agileforge.domain.port.out.FileStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepositoryPort attachmentRepository;
    private final FileStoragePort fileStorage;

    public AttachmentService(AttachmentRepositoryPort attachmentRepository, FileStoragePort fileStorage) {
        this.attachmentRepository = attachmentRepository;
        this.fileStorage = fileStorage;
    }

    public Attachment upload(UUID ticketId, MultipartFile file, UUID userId) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        String storagePath;
        try {
            storagePath = fileStorage.store(file.getOriginalFilename(), file.getContentType(), file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException("Failed to read uploaded file");
        }

        Attachment attachment = new Attachment(
                ticketId,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                storagePath,
                userId
        );

        Attachment saved = attachmentRepository.save(attachment);
        log.info("Attachment uploaded: '{}' for ticket {}", file.getOriginalFilename(), ticketId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Attachment> getByTicketId(UUID ticketId) {
        return attachmentRepository.findByTicketId(ticketId);
    }

    @Transactional(readOnly = true)
    public Attachment getById(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment", attachmentId));
    }

    public void delete(UUID attachmentId, UUID userId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment", attachmentId));

        if (!attachment.getUploadedBy().equals(userId)) {
            throw new BusinessException("Only the uploader can delete this attachment");
        }

        fileStorage.delete(attachment.getStoragePath());
        attachmentRepository.delete(attachmentId);
        log.info("Attachment deleted: {}", attachmentId);
    }

    @Transactional(readOnly = true)
    public String getDownloadUrl(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment", attachmentId));
        return fileStorage.getUrl(attachment.getStoragePath());
    }
}
