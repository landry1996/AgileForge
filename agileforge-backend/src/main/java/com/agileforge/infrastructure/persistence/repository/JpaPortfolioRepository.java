package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPortfolioRepository extends JpaRepository<PortfolioEntity, UUID> {

    Optional<PortfolioEntity> findByIdAndDeletedFalse(UUID id);

    List<PortfolioEntity> findByOrganizationIdAndDeletedFalseOrderByCreatedAtDesc(UUID organizationId);
}
