package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.TicketHistory;
import com.agileforge.domain.port.out.TicketHistoryRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.TicketHistoryEntity;
import com.agileforge.infrastructure.persistence.repository.JpaTicketHistoryRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TicketHistoryRepositoryAdapter implements TicketHistoryRepositoryPort {

    private final JpaTicketHistoryRepository repository;

    public TicketHistoryRepositoryAdapter(JpaTicketHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public TicketHistory save(TicketHistory history) {
        TicketHistoryEntity entity = new TicketHistoryEntity();
        entity.setId(history.getId());
        entity.setTicketId(history.getTicketId());
        entity.setUserId(history.getUserId());
        entity.setField(history.getField());
        entity.setOldValue(history.getOldValue());
        entity.setNewValue(history.getNewValue());
        entity.setCreatedAt(history.getCreatedAt() != null ? history.getCreatedAt() : Instant.now());

        TicketHistoryEntity saved = repository.save(entity);

        TicketHistory result = new TicketHistory();
        result.setId(saved.getId());
        result.setTicketId(saved.getTicketId());
        result.setUserId(saved.getUserId());
        result.setField(saved.getField());
        result.setOldValue(saved.getOldValue());
        result.setNewValue(saved.getNewValue());
        result.setCreatedAt(saved.getCreatedAt());
        return result;
    }

    @Override
    public List<TicketHistory> findByTicketId(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(entity -> {
                    TicketHistory h = new TicketHistory();
                    h.setId(entity.getId());
                    h.setTicketId(entity.getTicketId());
                    h.setUserId(entity.getUserId());
                    h.setField(entity.getField());
                    h.setOldValue(entity.getOldValue());
                    h.setNewValue(entity.getNewValue());
                    h.setCreatedAt(entity.getCreatedAt());
                    return h;
                }).toList();
    }
}
