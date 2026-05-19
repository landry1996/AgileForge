package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepositoryPort {

    Organization save(Organization organization);

    Optional<Organization> findById(UUID id);

    Optional<Organization> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Organization> findByUserId(UUID userId);

    int countMembersByOrganizationId(UUID organizationId);

    int countProjectsByOrganizationId(UUID organizationId);
}
