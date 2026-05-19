package com.agileforge.domain.port.out;

import com.agileforge.domain.model.ProjectMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepositoryPort {

    ProjectMember save(ProjectMember member);

    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    List<ProjectMember> findByProjectId(UUID projectId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    void delete(UUID id);
}
