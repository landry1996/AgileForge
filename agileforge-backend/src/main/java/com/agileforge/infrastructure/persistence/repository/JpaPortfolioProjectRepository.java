package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.PortfolioProjectEntity;
import com.agileforge.infrastructure.persistence.entity.PortfolioProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaPortfolioProjectRepository extends JpaRepository<PortfolioProjectEntity, PortfolioProjectId> {

    @Query("SELECT pp.id.projectId FROM PortfolioProjectEntity pp WHERE pp.id.portfolioId = :portfolioId ORDER BY pp.priority DESC")
    List<UUID> findProjectIdsByPortfolioId(@Param("portfolioId") UUID portfolioId);

    void deleteById(PortfolioProjectId id);
}
