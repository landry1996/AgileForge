package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepositoryPort {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    Optional<Project> findByOrganizationIdAndKey(UUID organizationId, String key);

    List<Project> findByOrganizationId(UUID organizationId);

    List<Project> findByUserId(UUID userId);

    boolean existsByOrganizationIdAndKey(UUID organizationId, String key);

    boolean existsByOrganizationIdAndName(UUID organizationId, String name);
}
