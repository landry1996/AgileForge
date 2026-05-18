package com.agileforge.domain.port.out;

import com.agileforge.domain.model.SearchResult;
import com.agileforge.domain.model.Ticket;

import java.util.UUID;

public interface SearchPort {

    SearchResult<Ticket> searchTickets(UUID projectId, String query, String status,
                                       String type, String priority, UUID assigneeId,
                                       int page, int size);
}
