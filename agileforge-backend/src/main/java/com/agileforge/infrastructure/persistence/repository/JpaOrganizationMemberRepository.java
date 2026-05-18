package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.OrganizationMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaOrganizationMemberRepository extends JpaRepository<OrganizationMemberEntity, UUID> {

    Optional<OrganizationMemberEntity> findByOrganizationIdAndUserIdAndActiveTrue(UUID organizationId, UUID userId);

    List<OrganizationMemberEntity> findByOrganizationIdAndActiveTrue(UUID organizationId);

    boolean existsByOrganizationIdAndUserIdAndActiveTrue(UUID organizationId, UUID userId);

    int countByOrganizationIdAndActiveTrue(UUID organizationId);
}
