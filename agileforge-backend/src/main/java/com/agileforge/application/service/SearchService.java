package com.agileforge.application.service;

import com.agileforge.domain.model.SearchResult;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.port.out.SearchPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final SearchPort searchPort;

    public SearchService(SearchPort searchPort) {
        this.searchPort = searchPort;
    }

    public SearchResult<Ticket> searchTickets(UUID projectId, String query, String status,
                                              String type, String priority, UUID assigneeId,
                                              int page, int size) {
        if (size > 100) size = 100;
        if (size < 1) size = 20;
        if (page < 0) page = 0;

        log.debug("Searching tickets: project={}, query='{}', status={}, type={}, priority={}, page={}, size={}",
                projectId, query, status, type, priority, page, size);

        return searchPort.searchTickets(projectId, query, status, type, priority, assigneeId, page, size);
    }
}
