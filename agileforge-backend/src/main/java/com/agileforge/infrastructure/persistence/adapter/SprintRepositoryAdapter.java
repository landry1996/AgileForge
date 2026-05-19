package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Sprint;
import com.agileforge.domain.port.out.SprintRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.SprintEntity;
import com.agileforge.infrastructure.persistence.repository.JpaSprintRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SprintRepositoryAdapter implements SprintRepositoryPort {

    private final JpaSprintRepository repository;

    public SprintRepositoryAdapter(JpaSprintRepository repository) {
        this.repository = repository;
    }

    @Override
    public Sprint save(Sprint sprint) {
        SprintEntity entity = toEntity(sprint);
        SprintEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Sprint> findById(UUID id) {
        return repository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public List<Sprint> findByProjectId(UUID projectId) {
        return repository.findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<Sprint> findActiveByProjectId(UUID projectId) {
        return repository.findByProjectIdAndStatusAndDeletedFalse(projectId, "ACTIVE")
                .map(this::toDomain);
    }

    @Override
    public long countByProjectId(UUID projectId) {
        return repository.countByProjectIdAndDeletedFalse(projectId);
    }

    @Override
    public List<Sprint> findCompletedByProjectId(UUID projectId) {
        return repository.findByProjectIdAndStatusAndDeletedFalseOrderByEndDateDesc(projectId, "COMPLETED").stream()
                .map(this::toDomain).toList();
    }

    private SprintEntity toEntity(Sprint domain) {
        SprintEntity entity = new SprintEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setName(domain.getName());
        entity.setGoal(domain.getGoal());
        entity.setStatus(domain.getStatus().name());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setCapacity(domain.getCapacity());
        return entity;
    }

    private Sprint toDomain(SprintEntity entity) {
        Sprint domain = new Sprint();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setName(entity.getName());
        domain.setGoal(entity.getGoal());
        domain.setStatus(Sprint.SprintStatus.valueOf(entity.getStatus()));
        domain.setStartDate(entity.getStartDate());
        domain.setEndDate(entity.getEndDate());
        domain.setCapacity(entity.getCapacity());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
