package com.agileforge.domain.port.out;

import com.agileforge.domain.model.TimeEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeEntryRepositoryPort {

    TimeEntry save(TimeEntry timeEntry);

    Optional<TimeEntry> findById(UUID id);

    List<TimeEntry> findByTicketId(UUID ticketId);

    List<TimeEntry> findByUserId(UUID userId);

    List<TimeEntry> findByUserIdAndWorkDateBetween(UUID userId, LocalDate from, LocalDate to);

    void delete(UUID id);

    double sumHoursByTicketId(UUID ticketId);

    double sumHoursByUserIdAndWorkDateBetween(UUID userId, LocalDate from, LocalDate to);
}
