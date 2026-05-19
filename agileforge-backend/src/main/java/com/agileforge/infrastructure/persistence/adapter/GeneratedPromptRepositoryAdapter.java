package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.GeneratedPrompt;
import com.agileforge.domain.port.out.GeneratedPromptRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.GeneratedPromptEntity;
import com.agileforge.infrastructure.persistence.repository.JpaGeneratedPromptRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GeneratedPromptRepositoryAdapter implements GeneratedPromptRepositoryPort {

    private final JpaGeneratedPromptRepository repository;

    public GeneratedPromptRepositoryAdapter(JpaGeneratedPromptRepository repository) {
        this.repository = repository;
    }

    @Override
    public GeneratedPrompt save(GeneratedPrompt prompt) {
        GeneratedPromptEntity entity = toEntity(prompt);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        GeneratedPromptEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<GeneratedPrompt> findByTicketId(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<GeneratedPrompt> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private GeneratedPromptEntity toEntity(GeneratedPrompt domain) {
        GeneratedPromptEntity entity = new GeneratedPromptEntity();
        entity.setId(domain.getId());
        entity.setTicketId(domain.getTicketId());
        entity.setTemplateId(domain.getTemplateId());
        entity.setPromptText(domain.getPromptText());
        entity.setGeneratedBy(domain.getGeneratedBy());
        entity.setRating(domain.getRating());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private GeneratedPrompt toDomain(GeneratedPromptEntity entity) {
        GeneratedPrompt domain = new GeneratedPrompt();
        domain.setId(entity.getId());
        domain.setTicketId(entity.getTicketId());
        domain.setTemplateId(entity.getTemplateId());
        domain.setPromptText(entity.getPromptText());
        domain.setGeneratedBy(entity.getGeneratedBy());
        domain.setRating(entity.getRating());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
