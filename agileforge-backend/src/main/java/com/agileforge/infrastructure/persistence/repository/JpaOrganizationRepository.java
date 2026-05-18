package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaOrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {

    Optional<OrganizationEntity> findByIdAndDeletedFalse(UUID id);

    Optional<OrganizationEntity> findBySlugAndDeletedFalse(String slug);

    boolean existsBySlugAndDeletedFalse(String slug);

    @Query("SELECT o FROM OrganizationEntity o JOIN OrganizationMemberEntity m ON o.id = m.organizationId " +
            "WHERE m.userId = :userId AND m.active = true AND o.deleted = false")
    List<OrganizationEntity> findByUserId(@Param("userId") UUID userId);
}
