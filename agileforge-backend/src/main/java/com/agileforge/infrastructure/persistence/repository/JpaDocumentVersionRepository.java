package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.DocumentVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaDocumentVersionRepository extends JpaRepository<DocumentVersionEntity, UUID> {

    List<DocumentVersionEntity> findByDocumentIdOrderByVersionDesc(UUID documentId);
}
