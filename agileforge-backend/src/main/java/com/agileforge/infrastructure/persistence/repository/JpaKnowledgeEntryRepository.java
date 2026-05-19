package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.KnowledgeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaKnowledgeEntryRepository extends JpaRepository<KnowledgeEntryEntity, UUID> {

    List<KnowledgeEntryEntity> findByProjectIdAndIsActiveTrueOrderByCreatedAtDesc(UUID projectId);

    List<KnowledgeEntryEntity> findByProjectIdAndCategoryAndIsActiveTrueOrderByCreatedAtDesc(UUID projectId, String category);

    @Query("SELECT e FROM KnowledgeEntryEntity e WHERE e.projectId = :projectId AND e.isActive = true " +
           "AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(e.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(e.tags) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<KnowledgeEntryEntity> search(@Param("projectId") UUID projectId, @Param("query") String query);
}
