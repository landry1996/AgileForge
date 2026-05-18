package com.agileforge.application.service;

import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.BoardColumnRepositoryPort;
import com.agileforge.domain.port.out.ProjectRepositoryPort;
import com.agileforge.domain.port.out.SprintRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BoardService {

    private final BoardColumnRepositoryPort columnRepository;
    private final TicketRepositoryPort ticketRepository;
    private final SprintRepositoryPort sprintRepository;
    private final ProjectRepositoryPort projectRepository;

    public BoardService(BoardColumnRepositoryPort columnRepository,
                        TicketRepositoryPort ticketRepository,
                        SprintRepositoryPort sprintRepository,
                        ProjectRepositoryPort projectRepository) {
        this.columnRepository = columnRepository;
        this.ticketRepository = ticketRepository;
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public BoardData getBoard(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));

        List<BoardColumn> columns = columnRepository.findByProjectIdOrderByPosition(projectId);

        // If no columns configured, create default ones
        if (columns.isEmpty()) {
            columns = createDefaultColumns(projectId);
        }

        Sprint activeSprint = sprintRepository.findActiveByProjectId(projectId).orElse(null);

        List<Ticket> tickets;
        if (activeSprint != null) {
            tickets = ticketRepository.findBySprintId(activeSprint.getId());
        } else {
            tickets = ticketRepository.findByProjectId(projectId).stream()
                    .filter(t -> !t.isDone())
                    .toList();
        }

        return new BoardData(project, columns, tickets, activeSprint);
    }

    public BoardColumn addColumn(UUID projectId, String name, TicketStatus mappedStatus, int position, Integer wipLimit) {
        BoardColumn column = new BoardColumn(projectId, name, mappedStatus, position);
        column.setWipLimit(wipLimit);
        return columnRepository.save(column);
    }

    public void removeColumn(UUID columnId) {
        columnRepository.delete(columnId);
    }

    public void moveTicket(UUID ticketId, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));
        ticket.setStatus(newStatus);
        ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getBacklog(UUID projectId) {
        return ticketRepository.findByProjectIdAndStatus(projectId, TicketStatus.BACKLOG);
    }

    private List<BoardColumn> createDefaultColumns(UUID projectId) {
        BoardColumn[] defaults = {
                new BoardColumn(projectId, "Backlog", TicketStatus.BACKLOG, 0),
                new BoardColumn(projectId, "To Do", TicketStatus.TODO, 1),
                new BoardColumn(projectId, "In Progress", TicketStatus.IN_PROGRESS, 2),
                new BoardColumn(projectId, "Code Review", TicketStatus.CODE_REVIEW, 3),
                new BoardColumn(projectId, "QA", TicketStatus.QA, 4),
                new BoardColumn(projectId, "Done", TicketStatus.DONE, 5)
        };

        for (BoardColumn col : defaults) {
            columnRepository.save(col);
        }

        return columnRepository.findByProjectIdOrderByPosition(projectId);
    }

    public record BoardData(Project project, List<BoardColumn> columns, List<Ticket> tickets, Sprint activeSprint) {}
}
