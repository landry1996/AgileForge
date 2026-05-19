package com.agileforge.domain.port.out;

import com.agileforge.domain.model.OrganizationMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepositoryPort {

    OrganizationMember save(OrganizationMember member);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    List<OrganizationMember> findByOrganizationId(UUID organizationId);

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    void delete(UUID id);
}
