package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Sprint;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketStatus;
import com.agileforge.domain.port.out.SprintRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SprintService {

    private static final Logger log = LoggerFactory.getLogger(SprintService.class);

    private final SprintRepositoryPort sprintRepository;
    private final TicketRepositoryPort ticketRepository;

    public SprintService(SprintRepositoryPort sprintRepository, TicketRepositoryPort ticketRepository) {
        this.sprintRepository = sprintRepository;
        this.ticketRepository = ticketRepository;
    }

    public Sprint create(UUID projectId, String name, String goal, String startDate, String endDate, Integer capacity) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException("End date cannot be before start date");
        }

        long count = sprintRepository.countByProjectId(projectId);
        String sprintName = name != null ? name : "Sprint " + (count + 1);

        Sprint sprint = new Sprint(projectId, sprintName, goal, start, end);
        sprint.setCapacity(capacity);

        Sprint saved = sprintRepository.save(sprint);
        log.info("Sprint created: {} in project {}", sprintName, projectId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Sprint getById(UUID id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sprint", id));
    }

    @Transactional(readOnly = true)
    public List<Sprint> getByProjectId(UUID projectId) {
        return sprintRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public Sprint getActiveSprint(UUID projectId) {
        return sprintRepository.findActiveByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("No active sprint for this project"));
    }

    public Sprint start(UUID id) {
        Sprint sprint = getById(id);
        if (!sprint.canStart()) {
            throw new BusinessException("Sprint cannot be started. Current status: " + sprint.getStatus());
        }

        // Check no other active sprint
        sprintRepository.findActiveByProjectId(sprint.getProjectId()).ifPresent(active -> {
            throw new BusinessException("Project already has an active sprint: " + active.getName());
        });

        sprint.setStatus(Sprint.SprintStatus.ACTIVE);
        if (sprint.getStartDate() == null) {
            sprint.setStartDate(LocalDate.now());
        }
        if (sprint.getEndDate() == null) {
            sprint.setEndDate(LocalDate.now().plusDays(14));
        }

        Sprint saved = sprintRepository.save(sprint);
        log.info("Sprint started: {}", sprint.getName());
        return saved;
    }

    public Sprint complete(UUID id) {
        Sprint sprint = getById(id);
        if (!sprint.canComplete()) {
            throw new BusinessException("Sprint cannot be completed. Current status: " + sprint.getStatus());
        }

        sprint.setStatus(Sprint.SprintStatus.COMPLETED);
        Sprint saved = sprintRepository.save(sprint);

        // Move unfinished tickets back to backlog
        List<Ticket> unfinished = ticketRepository.findBySprintId(id).stream()
                .filter(t -> !t.isDone())
                .toList();

        for (Ticket ticket : unfinished) {
            ticket.setSprintId(null);
            ticket.setStatus(TicketStatus.BACKLOG);
            ticketRepository.save(ticket);
        }

        log.info("Sprint completed: {}. {} unfinished tickets moved to backlog", sprint.getName(), unfinished.size());
        return saved;
    }

    public void addTicketToSprint(UUID sprintId, UUID ticketId) {
        Sprint sprint = getById(sprintId);
        if (sprint.getStatus() == Sprint.SprintStatus.COMPLETED || sprint.getStatus() == Sprint.SprintStatus.CANCELLED) {
            throw new BusinessException("Cannot add tickets to a completed/cancelled sprint");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        ticket.setSprintId(sprintId);
        if (ticket.getStatus() == TicketStatus.BACKLOG) {
            ticket.setStatus(TicketStatus.TODO);
        }
        ticketRepository.save(ticket);
    }

    public void removeTicketFromSprint(UUID sprintId, UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        if (!sprintId.equals(ticket.getSprintId())) {
            throw new BusinessException("Ticket is not in this sprint");
        }

        ticket.setSprintId(null);
        ticket.setStatus(TicketStatus.BACKLOG);
        ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public SprintMetrics getMetrics(UUID sprintId) {
        List<Ticket> tickets = ticketRepository.findBySprintId(sprintId);
        long total = tickets.size();
        long done = tickets.stream().filter(Ticket::isDone).count();
        int totalPoints = tickets.stream()
                .map(Ticket::getStoryPoints)
                .filter(sp -> sp != null)
                .mapToInt(Integer::intValue)
                .sum();
        int donePoints = tickets.stream()
                .filter(Ticket::isDone)
                .map(Ticket::getStoryPoints)
                .filter(sp -> sp != null)
                .mapToInt(Integer::intValue)
                .sum();
        return new SprintMetrics(total, done, totalPoints, donePoints);
    }

    public record SprintMetrics(long totalTickets, long doneTickets, int totalPoints, int donePoints) {}
}
