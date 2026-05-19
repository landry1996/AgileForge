package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaDocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    Optional<DocumentEntity> findByIdAndDeletedFalse(UUID id);

    List<DocumentEntity> findByProjectIdAndDeletedFalseOrderByPositionAsc(UUID projectId);

    List<DocumentEntity> findByParentIdAndDeletedFalseOrderByPositionAsc(UUID parentId);

    List<DocumentEntity> findByProjectIdAndDocTypeAndDeletedFalseOrderByPositionAsc(UUID projectId, String docType);
}
