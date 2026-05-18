package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.ProjectMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaProjectMemberRepository extends JpaRepository<ProjectMemberEntity, UUID> {

    Optional<ProjectMemberEntity> findByProjectIdAndUserIdAndActiveTrue(UUID projectId, UUID userId);

    List<ProjectMemberEntity> findByProjectIdAndActiveTrue(UUID projectId);

    boolean existsByProjectIdAndUserIdAndActiveTrue(UUID projectId, UUID userId);
}
