package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.PromptCategory;
import com.agileforge.domain.model.PromptTemplate;
import com.agileforge.domain.port.out.PromptTemplateRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.PromptTemplateEntity;
import com.agileforge.infrastructure.persistence.repository.JpaPromptTemplateRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PromptTemplateRepositoryAdapter implements PromptTemplateRepositoryPort {

    private final JpaPromptTemplateRepository repository;

    public PromptTemplateRepositoryAdapter(JpaPromptTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public PromptTemplate save(PromptTemplate template) {
        PromptTemplateEntity entity = toEntity(template);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        entity.setUpdatedAt(Instant.now());
        PromptTemplateEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PromptTemplate> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<PromptTemplate> findByProjectId(UUID projectId) {
        return repository.findByProjectIdOrderByUsageCountDesc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<PromptTemplate> findGlobal() {
        return repository.findByIsGlobalTrueOrderByUsageCountDesc().stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<PromptTemplate> findByCategory(PromptCategory category) {
        return repository.findByCategoryOrderByUsageCountDesc(category.name()).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void incrementUsageCount(UUID id) {
        repository.incrementUsageCount(id);
    }

    private PromptTemplateEntity toEntity(PromptTemplate domain) {
        PromptTemplateEntity entity = new PromptTemplateEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setName(domain.getName());
        entity.setCategory(domain.getCategory().name());
        entity.setTemplate(domain.getTemplate());
        entity.setVariables(domain.getVariables());
        entity.setGlobal(domain.isGlobal());
        entity.setUsageCount(domain.getUsageCount());
        entity.setRating(domain.getRating());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private PromptTemplate toDomain(PromptTemplateEntity entity) {
        PromptTemplate domain = new PromptTemplate();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setName(entity.getName());
        domain.setCategory(PromptCategory.valueOf(entity.getCategory()));
        domain.setTemplate(entity.getTemplate());
        domain.setVariables(entity.getVariables());
        domain.setGlobal(entity.isGlobal());
        domain.setUsageCount(entity.getUsageCount());
        domain.setRating(entity.getRating());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
