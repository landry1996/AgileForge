package com.agileforge.domain.port.out;

import com.agileforge.domain.model.RoadmapItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoadmapItemRepositoryPort {

    RoadmapItem save(RoadmapItem item);

    Optional<RoadmapItem> findById(UUID id);

    List<RoadmapItem> findByProjectId(UUID projectId);

    void delete(UUID id);
}
