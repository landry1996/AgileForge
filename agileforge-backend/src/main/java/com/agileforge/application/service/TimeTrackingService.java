package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreateTimeEntryRequest;
import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TimeEntry;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import com.agileforge.domain.port.out.TimeEntryRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TimeTrackingService {

    private static final Logger log = LoggerFactory.getLogger(TimeTrackingService.class);

    private final TimeEntryRepositoryPort timeEntryRepository;
    private final TicketRepositoryPort ticketRepository;

    public TimeTrackingService(TimeEntryRepositoryPort timeEntryRepository,
                               TicketRepositoryPort ticketRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.ticketRepository = ticketRepository;
    }

    public TimeEntry logTime(UUID ticketId, UUID userId, CreateTimeEntryRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        LocalDate workDate = request.workDate() != null ? request.workDate() : LocalDate.now();

        TimeEntry entry = new TimeEntry(ticketId, userId, request.hours(), request.description(), workDate);
        TimeEntry saved = timeEntryRepository.save(entry);

        // Update ticket's logged hours
        double totalHours = timeEntryRepository.sumHoursByTicketId(ticketId);
        ticket.setLoggedHours(totalHours);
        ticketRepository.save(ticket);

        log.info("Time entry logged: {} hours on ticket {} by user {}", request.hours(), ticketId, userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<TimeEntry> getEntriesByTicket(UUID ticketId) {
        ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));
        return timeEntryRepository.findByTicketId(ticketId);
    }

    @Transactional(readOnly = true)
    public List<TimeEntry> getEntriesByUser(UUID userId, LocalDate from, LocalDate to) {
        return timeEntryRepository.findByUserIdAndWorkDateBetween(userId, from, to);
    }

    @Transactional(readOnly = true)
    public TimeTrackingSummary getTicketSummary(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        double totalLogged = timeEntryRepository.sumHoursByTicketId(ticketId);
        List<TimeEntry> entries = timeEntryRepository.findByTicketId(ticketId);

        return new TimeTrackingSummary(ticketId, totalLogged, ticket.getEstimatedHours(), entries);
    }

    public void deleteEntry(UUID entryId, UUID userId) {
        TimeEntry entry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("TimeEntry", entryId));

        if (!entry.getUserId().equals(userId)) {
            throw new BusinessException("Only the author can delete a time entry");
        }

        UUID ticketId = entry.getTicketId();
        timeEntryRepository.delete(entryId);

        // Recalculate ticket's logged hours
        double totalHours = timeEntryRepository.sumHoursByTicketId(ticketId);
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            ticket.setLoggedHours(totalHours);
            ticketRepository.save(ticket);
        });

        log.info("Time entry {} deleted by user {}", entryId, userId);
    }

    @Transactional(readOnly = true)
    public double getUserWeeklyTotal(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        return timeEntryRepository.sumHoursByUserIdAndWorkDateBetween(userId, startOfWeek, endOfWeek);
    }

    public record TimeTrackingSummary(UUID ticketId, double totalLogged, Double estimatedHours, List<TimeEntry> entries) {}
}
