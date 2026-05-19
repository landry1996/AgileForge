package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaInvitationRepository extends JpaRepository<InvitationEntity, UUID> {

    Optional<InvitationEntity> findByToken(String token);

    List<InvitationEntity> findByOrganizationId(UUID organizationId);

    List<InvitationEntity> findByEmail(String email);

    @Query("SELECT i FROM InvitationEntity i WHERE i.organizationId = :orgId AND i.email = :email AND i.status = 'PENDING'")
    Optional<InvitationEntity> findPendingByOrganizationIdAndEmail(@Param("orgId") UUID orgId, @Param("email") String email);
}
