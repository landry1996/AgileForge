package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.PromptTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaPromptTemplateRepository extends JpaRepository<PromptTemplateEntity, UUID> {

    List<PromptTemplateEntity> findByProjectIdOrderByUsageCountDesc(UUID projectId);

    List<PromptTemplateEntity> findByIsGlobalTrueOrderByUsageCountDesc();

    List<PromptTemplateEntity> findByCategoryOrderByUsageCountDesc(String category);

    @Modifying
    @Query("UPDATE PromptTemplateEntity p SET p.usageCount = p.usageCount + 1 WHERE p.id = :id")
    void incrementUsageCount(@Param("id") UUID id);
}
