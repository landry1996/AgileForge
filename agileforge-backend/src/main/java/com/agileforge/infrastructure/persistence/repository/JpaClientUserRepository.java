package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.ClientUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaClientUserRepository extends JpaRepository<ClientUserEntity, UUID> {

    List<ClientUserEntity> findByPortalIdAndActiveTrue(UUID portalId);

    int countByPortalId(UUID portalId);
}
