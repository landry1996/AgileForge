package com.agileforge.domain.port.out;

import com.agileforge.domain.model.TicketHistory;

import java.util.List;
import java.util.UUID;

public interface TicketHistoryRepositoryPort {

    TicketHistory save(TicketHistory history);

    List<TicketHistory> findByTicketId(UUID ticketId);
}
