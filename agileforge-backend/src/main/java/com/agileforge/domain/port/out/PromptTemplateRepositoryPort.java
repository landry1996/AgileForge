package com.agileforge.domain.port.out;

import com.agileforge.domain.model.PromptCategory;
import com.agileforge.domain.model.PromptTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromptTemplateRepositoryPort {

    PromptTemplate save(PromptTemplate template);

    Optional<PromptTemplate> findById(UUID id);

    List<PromptTemplate> findByProjectId(UUID projectId);

    List<PromptTemplate> findGlobal();

    List<PromptTemplate> findByCategory(PromptCategory category);

    void delete(UUID id);

    void incrementUsageCount(UUID id);
}
