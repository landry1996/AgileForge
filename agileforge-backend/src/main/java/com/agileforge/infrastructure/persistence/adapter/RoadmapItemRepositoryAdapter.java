package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.RoadmapItem;
import com.agileforge.domain.model.RoadmapItemStatus;
import com.agileforge.domain.port.out.RoadmapItemRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.RoadmapItemEntity;
import com.agileforge.infrastructure.persistence.repository.JpaRoadmapItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RoadmapItemRepositoryAdapter implements RoadmapItemRepositoryPort {

    private final JpaRoadmapItemRepository repository;

    public RoadmapItemRepositoryAdapter(JpaRoadmapItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public RoadmapItem save(RoadmapItem item) {
        RoadmapItemEntity entity = toEntity(item);
        RoadmapItemEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RoadmapItem> findById(UUID id) {
        return repository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public List<RoadmapItem> findByProjectId(UUID projectId) {
        return repository.findByProjectIdAndDeletedFalseOrderByPositionAsc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            repository.save(entity);
        });
    }

    private RoadmapItemEntity toEntity(RoadmapItem domain) {
        RoadmapItemEntity entity = new RoadmapItemEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setCategory(domain.getCategory());
        entity.setStatus(domain.getStatus().name());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setColor(domain.getColor());
        entity.setPosition(domain.getPosition());
        entity.setReleaseId(domain.getReleaseId());
        entity.setEpicId(domain.getEpicId());
        return entity;
    }

    private RoadmapItem toDomain(RoadmapItemEntity entity) {
        RoadmapItem domain = new RoadmapItem();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setTitle(entity.getTitle());
        domain.setDescription(entity.getDescription());
        domain.setCategory(entity.getCategory());
        domain.setStatus(RoadmapItemStatus.valueOf(entity.getStatus()));
        domain.setStartDate(entity.getStartDate());
        domain.setEndDate(entity.getEndDate());
        domain.setColor(entity.getColor());
        domain.setPosition(entity.getPosition());
        domain.setReleaseId(entity.getReleaseId());
        domain.setEpicId(entity.getEpicId());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
