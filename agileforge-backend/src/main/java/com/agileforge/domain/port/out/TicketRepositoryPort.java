package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketStatus;
import com.agileforge.domain.model.TicketType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepositoryPort {

    Ticket save(Ticket ticket);

    Optional<Ticket> findById(UUID id);

    Optional<Ticket> findByProjectIdAndNumber(UUID projectId, long number);

    List<Ticket> findByProjectId(UUID projectId);

    List<Ticket> findByProjectIdAndStatus(UUID projectId, TicketStatus status);

    List<Ticket> findByProjectIdAndType(UUID projectId, TicketType type);

    List<Ticket> findByAssigneeId(UUID assigneeId);

    List<Ticket> findBySprintId(UUID sprintId);

    List<Ticket> findByEpicId(UUID epicId);

    long getNextNumber(UUID projectId);

    long countByProjectId(UUID projectId);

    long countByProjectIdAndStatus(UUID projectId, TicketStatus status);

    List<Ticket> findByProjectIdAndStatusIn(UUID projectId, List<TicketStatus> statuses);
}
