package com.agileforge.domain.port.out;

import com.agileforge.domain.model.KnowledgeCategory;
import com.agileforge.domain.model.KnowledgeEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeEntryRepositoryPort {

    KnowledgeEntry save(KnowledgeEntry entry);

    Optional<KnowledgeEntry> findById(UUID id);

    List<KnowledgeEntry> findByProjectId(UUID projectId);

    List<KnowledgeEntry> findByProjectIdAndCategory(UUID projectId, KnowledgeCategory category);

    void delete(UUID id);

    List<KnowledgeEntry> search(UUID projectId, String query);
}
