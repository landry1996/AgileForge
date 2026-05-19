package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.GeneratedPromptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaGeneratedPromptRepository extends JpaRepository<GeneratedPromptEntity, UUID> {

    List<GeneratedPromptEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
}
