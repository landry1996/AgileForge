package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Portfolio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepositoryPort {

    Portfolio save(Portfolio portfolio);

    Optional<Portfolio> findById(UUID id);

    List<Portfolio> findByOrganizationId(UUID organizationId);

    void delete(UUID id);

    void addProject(UUID portfolioId, UUID projectId, int priority);

    void removeProject(UUID portfolioId, UUID projectId);

    List<UUID> findProjectIdsByPortfolioId(UUID portfolioId);
}
