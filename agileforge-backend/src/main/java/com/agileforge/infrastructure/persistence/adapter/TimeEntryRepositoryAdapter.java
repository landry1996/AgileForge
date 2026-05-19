package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.TimeEntry;
import com.agileforge.domain.port.out.TimeEntryRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.TimeEntryEntity;
import com.agileforge.infrastructure.persistence.repository.JpaTimeEntryRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TimeEntryRepositoryAdapter implements TimeEntryRepositoryPort {

    private final JpaTimeEntryRepository repository;

    public TimeEntryRepositoryAdapter(JpaTimeEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public TimeEntry save(TimeEntry timeEntry) {
        TimeEntryEntity entity = toEntity(timeEntry);
        TimeEntryEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TimeEntry> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<TimeEntry> findByTicketId(UUID ticketId) {
        return repository.findByTicketIdOrderByWorkDateDesc(ticketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<TimeEntry> findByUserId(UUID userId) {
        return repository.findByUserIdOrderByWorkDateDesc(userId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<TimeEntry> findByUserIdAndWorkDateBetween(UUID userId, LocalDate from, LocalDate to) {
        return repository.findByUserIdAndWorkDateBetweenOrderByWorkDateDesc(userId, from, to).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public double sumHoursByTicketId(UUID ticketId) {
        return repository.sumHoursByTicketId(ticketId);
    }

    @Override
    public double sumHoursByUserIdAndWorkDateBetween(UUID userId, LocalDate from, LocalDate to) {
        return repository.sumHoursByUserIdAndWorkDateBetween(userId, from, to);
    }

    private TimeEntryEntity toEntity(TimeEntry domain) {
        TimeEntryEntity entity = new TimeEntryEntity();
        entity.setId(domain.getId());
        entity.setTicketId(domain.getTicketId());
        entity.setUserId(domain.getUserId());
        entity.setHours(domain.getHours());
        entity.setDescription(domain.getDescription());
        entity.setWorkDate(domain.getWorkDate());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        return entity;
    }

    private TimeEntry toDomain(TimeEntryEntity entity) {
        TimeEntry domain = new TimeEntry();
        domain.setId(entity.getId());
        domain.setTicketId(entity.getTicketId());
        domain.setUserId(entity.getUserId());
        domain.setHours(entity.getHours());
        domain.setDescription(entity.getDescription());
        domain.setWorkDate(entity.getWorkDate());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
