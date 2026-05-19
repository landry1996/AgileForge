package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Attachment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepositoryPort {

    Attachment save(Attachment attachment);

    Optional<Attachment> findById(UUID id);

    List<Attachment> findByTicketId(UUID ticketId);

    void delete(UUID id);
}
