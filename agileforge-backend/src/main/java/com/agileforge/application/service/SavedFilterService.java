package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreateSavedFilterRequest;
import com.agileforge.application.dto.request.UpdateSavedFilterRequest;
import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.SavedFilter;
import com.agileforge.domain.port.out.SavedFilterRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class SavedFilterService {

    private static final Logger log = LoggerFactory.getLogger(SavedFilterService.class);

    private final SavedFilterRepositoryPort savedFilterRepository;
    private final ObjectMapper objectMapper;

    public SavedFilterService(SavedFilterRepositoryPort savedFilterRepository, ObjectMapper objectMapper) {
        this.savedFilterRepository = savedFilterRepository;
        this.objectMapper = objectMapper;
    }

    public SavedFilter create(UUID projectId, UUID userId, CreateSavedFilterRequest request) {
        String filterConfigJson = toJson(request.filterConfig());
        SavedFilter savedFilter = new SavedFilter(projectId, userId, request.name(), filterConfigJson, request.isShared());
        SavedFilter saved = savedFilterRepository.save(savedFilter);
        log.info("Saved filter created: '{}' by user {} in project {}", request.name(), userId, projectId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<SavedFilter> getMyFilters(UUID projectId, UUID userId) {
        List<SavedFilter> ownFilters = savedFilterRepository.findByProjectIdAndUserId(projectId, userId);
        List<SavedFilter> sharedFilters = savedFilterRepository.findSharedByProjectId(projectId);

        List<SavedFilter> result = new ArrayList<>(ownFilters);
        for (SavedFilter shared : sharedFilters) {
            if (!shared.getUserId().equals(userId)) {
                result.add(shared);
            }
        }
        return result;
    }

    public SavedFilter update(UUID filterId, UUID userId, UpdateSavedFilterRequest request) {
        SavedFilter filter = savedFilterRepository.findById(filterId)
                .orElseThrow(() -> new EntityNotFoundException("SavedFilter", filterId));

        if (!filter.getUserId().equals(userId)) {
            throw new BusinessException("You can only update your own filters");
        }

        if (request.name() != null) {
            filter.setName(request.name());
        }
        if (request.filterConfig() != null) {
            filter.setFilterConfig(toJson(request.filterConfig()));
        }
        if (request.isShared() != null) {
            filter.setShared(request.isShared());
        }
        filter.setUpdatedAt(Instant.now());

        SavedFilter saved = savedFilterRepository.save(filter);
        log.info("Saved filter updated: {}", filterId);
        return saved;
    }

    public void delete(UUID filterId, UUID userId) {
        SavedFilter filter = savedFilterRepository.findById(filterId)
                .orElseThrow(() -> new EntityNotFoundException("SavedFilter", filterId));

        if (!filter.getUserId().equals(userId)) {
            throw new BusinessException("You can only delete your own filters");
        }

        savedFilterRepository.delete(filterId);
        log.info("Saved filter deleted: {}", filterId);
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new BusinessException("Invalid filter configuration", e);
        }
    }

    public Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructMapType(Map.class, String.class, Object.class));
        } catch (Exception e) {
            throw new BusinessException("Failed to parse filter configuration", e);
        }
    }
}
