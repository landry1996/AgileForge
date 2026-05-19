package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.KnowledgeCategory;
import com.agileforge.domain.model.KnowledgeEntry;
import com.agileforge.domain.port.out.KnowledgeEntryRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.KnowledgeEntryEntity;
import com.agileforge.infrastructure.persistence.repository.JpaKnowledgeEntryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class KnowledgeEntryRepositoryAdapter implements KnowledgeEntryRepositoryPort {

    private final JpaKnowledgeEntryRepository repository;

    public KnowledgeEntryRepositoryAdapter(JpaKnowledgeEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public KnowledgeEntry save(KnowledgeEntry entry) {
        KnowledgeEntryEntity entity = toEntity(entry);
        KnowledgeEntryEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<KnowledgeEntry> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<KnowledgeEntry> findByProjectId(UUID projectId) {
        return repository.findByProjectIdAndIsActiveTrueOrderByCreatedAtDesc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<KnowledgeEntry> findByProjectIdAndCategory(UUID projectId, KnowledgeCategory category) {
        return repository.findByProjectIdAndCategoryAndIsActiveTrueOrderByCreatedAtDesc(projectId, category.name()).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<KnowledgeEntry> search(UUID projectId, String query) {
        return repository.search(projectId, query).stream()
                .map(this::toDomain).toList();
    }

    private KnowledgeEntryEntity toEntity(KnowledgeEntry domain) {
        KnowledgeEntryEntity entity = new KnowledgeEntryEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setCategory(domain.getCategory().name());
        entity.setTitle(domain.getTitle());
        entity.setContent(domain.getContent());
        entity.setTags(domain.getTags());
        entity.setActive(domain.isActive());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : java.time.Instant.now());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private KnowledgeEntry toDomain(KnowledgeEntryEntity entity) {
        KnowledgeEntry domain = new KnowledgeEntry();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setCategory(KnowledgeCategory.valueOf(entity.getCategory()));
        domain.setTitle(entity.getTitle());
        domain.setContent(entity.getContent());
        domain.setTags(entity.getTags());
        domain.setActive(entity.isActive());
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
