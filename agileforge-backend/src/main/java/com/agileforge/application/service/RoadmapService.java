package com.agileforge.application.service;

import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.RoadmapItem;
import com.agileforge.domain.model.RoadmapItemStatus;
import com.agileforge.domain.port.out.RoadmapItemRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoadmapService {

    private static final Logger log = LoggerFactory.getLogger(RoadmapService.class);

    private final RoadmapItemRepositoryPort roadmapItemRepository;

    public RoadmapService(RoadmapItemRepositoryPort roadmapItemRepository) {
        this.roadmapItemRepository = roadmapItemRepository;
    }

    public RoadmapItem create(UUID projectId, String title, String description, String category,
                              String startDate, String endDate, String color, Integer position,
                              UUID releaseId, UUID epicId) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        int pos = position != null ? position : 0;

        RoadmapItem item = new RoadmapItem(projectId, title, description, category, start, end, color, pos);
        item.setReleaseId(releaseId);
        item.setEpicId(epicId);

        RoadmapItem saved = roadmapItemRepository.save(item);
        log.info("Roadmap item created: {} in project {}", title, projectId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<RoadmapItem> getByProject(UUID projectId) {
        return roadmapItemRepository.findByProjectId(projectId);
    }

    public RoadmapItem update(UUID itemId, String title, String description, String category,
                              String status, String startDate, String endDate, String color,
                              Integer position, UUID releaseId, UUID epicId) {
        RoadmapItem item = roadmapItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("RoadmapItem", itemId));

        if (title != null) item.setTitle(title);
        if (description != null) item.setDescription(description);
        if (category != null) item.setCategory(category);
        if (status != null) item.setStatus(RoadmapItemStatus.valueOf(status));
        if (startDate != null) item.setStartDate(LocalDate.parse(startDate));
        if (endDate != null) item.setEndDate(LocalDate.parse(endDate));
        if (color != null) item.setColor(color);
        if (position != null) item.setPosition(position);
        if (releaseId != null) item.setReleaseId(releaseId);
        if (epicId != null) item.setEpicId(epicId);

        RoadmapItem saved = roadmapItemRepository.save(item);
        log.info("Roadmap item updated: {}", itemId);
        return saved;
    }

    public void delete(UUID itemId) {
        roadmapItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("RoadmapItem", itemId));
        roadmapItemRepository.delete(itemId);
        log.info("Roadmap item deleted: {}", itemId);
    }

    public List<RoadmapItem> reorder(UUID projectId, List<UUID> itemIds) {
        List<RoadmapItem> items = roadmapItemRepository.findByProjectId(projectId);

        for (int i = 0; i < itemIds.size(); i++) {
            UUID itemId = itemIds.get(i);
            int newPosition = i;
            items.stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setPosition(newPosition);
                        roadmapItemRepository.save(item);
                    });
        }

        log.info("Roadmap items reordered for project {}", projectId);
        return roadmapItemRepository.findByProjectId(projectId);
    }
}
