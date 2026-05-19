package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.CapacityEntry;
import com.agileforge.domain.port.out.CapacityEntryRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.CapacityEntryEntity;
import com.agileforge.infrastructure.persistence.repository.JpaCapacityEntryRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CapacityEntryRepositoryAdapter implements CapacityEntryRepositoryPort {

    private final JpaCapacityEntryRepository repository;

    public CapacityEntryRepositoryAdapter(JpaCapacityEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public CapacityEntry save(CapacityEntry entry) {
        CapacityEntryEntity entity = toEntity(entry);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        CapacityEntryEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<CapacityEntry> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<CapacityEntry> findByProjectIdAndSprintId(UUID projectId, UUID sprintId) {
        return repository.findByProjectIdAndSprintId(projectId, sprintId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<CapacityEntry> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<CapacityEntry> findByProjectId(UUID projectId) {
        return repository.findByProjectId(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public double sumAvailableHoursByProjectAndSprint(UUID projectId, UUID sprintId) {
        return repository.sumAvailableHoursByProjectIdAndSprintId(projectId, sprintId);
    }

    private CapacityEntryEntity toEntity(CapacityEntry domain) {
        CapacityEntryEntity entity = new CapacityEntryEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setUserId(domain.getUserId());
        entity.setSprintId(domain.getSprintId());
        entity.setAvailableHours(domain.getAvailableHours());
        entity.setPlannedLeaveHours(domain.getPlannedLeaveHours());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private CapacityEntry toDomain(CapacityEntryEntity entity) {
        CapacityEntry domain = new CapacityEntry();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setUserId(entity.getUserId());
        domain.setSprintId(entity.getSprintId());
        domain.setAvailableHours(entity.getAvailableHours());
        domain.setPlannedLeaveHours(entity.getPlannedLeaveHours());
        domain.setNotes(entity.getNotes());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
