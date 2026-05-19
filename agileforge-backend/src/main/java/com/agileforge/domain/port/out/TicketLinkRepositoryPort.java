package com.agileforge.domain.port.out;

import com.agileforge.domain.model.TicketLink;
import com.agileforge.domain.model.TicketLinkType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketLinkRepositoryPort {

    TicketLink save(TicketLink ticketLink);

    Optional<TicketLink> findById(UUID id);

    List<TicketLink> findBySourceTicketId(UUID sourceTicketId);

    List<TicketLink> findByTargetTicketId(UUID targetTicketId);

    List<TicketLink> findAllByTicketId(UUID ticketId);

    void delete(UUID sourceTicketId, UUID targetTicketId, TicketLinkType type);

    void deleteById(UUID id);

    boolean existsLink(UUID sourceId, UUID targetId, TicketLinkType type);
}
