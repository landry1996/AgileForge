package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.SavedFilter;
import com.agileforge.domain.port.out.SavedFilterRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.SavedFilterEntity;
import com.agileforge.infrastructure.persistence.repository.JpaSavedFilterRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SavedFilterRepositoryAdapter implements SavedFilterRepositoryPort {

    private final JpaSavedFilterRepository repository;

    public SavedFilterRepositoryAdapter(JpaSavedFilterRepository repository) {
        this.repository = repository;
    }

    @Override
    public SavedFilter save(SavedFilter savedFilter) {
        SavedFilterEntity entity = toEntity(savedFilter);
        SavedFilterEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SavedFilter> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<SavedFilter> findByProjectIdAndUserId(UUID projectId, UUID userId) {
        return repository.findByProjectIdAndUserId(projectId, userId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<SavedFilter> findSharedByProjectId(UUID projectId) {
        return repository.findByProjectIdAndIsSharedTrue(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private SavedFilterEntity toEntity(SavedFilter domain) {
        SavedFilterEntity entity = new SavedFilterEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setUserId(domain.getUserId());
        entity.setName(domain.getName());
        entity.setFilterConfig(domain.getFilterConfig());
        entity.setShared(domain.isShared());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : java.time.Instant.now());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private SavedFilter toDomain(SavedFilterEntity entity) {
        SavedFilter domain = new SavedFilter();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setUserId(entity.getUserId());
        domain.setName(entity.getName());
        domain.setFilterConfig(entity.getFilterConfig());
        domain.setShared(entity.isShared());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
