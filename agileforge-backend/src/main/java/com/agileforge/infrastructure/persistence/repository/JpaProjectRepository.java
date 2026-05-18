package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    Optional<ProjectEntity> findByIdAndDeletedFalse(UUID id);

    Optional<ProjectEntity> findByOrganizationIdAndKeyAndDeletedFalse(UUID organizationId, String key);

    List<ProjectEntity> findByOrganizationIdAndDeletedFalse(UUID organizationId);

    boolean existsByOrganizationIdAndKeyAndDeletedFalse(UUID organizationId, String key);

    boolean existsByOrganizationIdAndNameAndDeletedFalse(UUID organizationId, String name);

    int countByOrganizationIdAndDeletedFalse(UUID organizationId);

    @Query("SELECT p FROM ProjectEntity p JOIN ProjectMemberEntity pm ON p.id = pm.projectId " +
            "WHERE pm.userId = :userId AND pm.active = true AND p.deleted = false")
    List<ProjectEntity> findByUserId(@Param("userId") UUID userId);
}
