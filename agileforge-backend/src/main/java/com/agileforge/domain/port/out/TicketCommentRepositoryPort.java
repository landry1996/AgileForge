package com.agileforge.domain.port.out;

import com.agileforge.domain.model.TicketComment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketCommentRepositoryPort {

    TicketComment save(TicketComment comment);

    Optional<TicketComment> findById(UUID id);

    List<TicketComment> findByTicketId(UUID ticketId);

    void delete(UUID id);
}
