package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Portfolio;
import com.agileforge.domain.port.out.PortfolioRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.PortfolioEntity;
import com.agileforge.infrastructure.persistence.entity.PortfolioProjectEntity;
import com.agileforge.infrastructure.persistence.entity.PortfolioProjectId;
import com.agileforge.infrastructure.persistence.repository.JpaPortfolioProjectRepository;
import com.agileforge.infrastructure.persistence.repository.JpaPortfolioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PortfolioRepositoryAdapter implements PortfolioRepositoryPort {

    private final JpaPortfolioRepository portfolioRepository;
    private final JpaPortfolioProjectRepository portfolioProjectRepository;

    public PortfolioRepositoryAdapter(JpaPortfolioRepository portfolioRepository,
                                      JpaPortfolioProjectRepository portfolioProjectRepository) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
    }

    @Override
    public Portfolio save(Portfolio portfolio) {
        PortfolioEntity entity = toEntity(portfolio);
        PortfolioEntity saved = portfolioRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Portfolio> findById(UUID id) {
        return portfolioRepository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public List<Portfolio> findByOrganizationId(UUID organizationId) {
        return portfolioRepository.findByOrganizationIdAndDeletedFalseOrderByCreatedAtDesc(organizationId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        portfolioRepository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            portfolioRepository.save(entity);
        });
    }

    @Override
    public void addProject(UUID portfolioId, UUID projectId, int priority) {
        PortfolioProjectId id = new PortfolioProjectId(portfolioId, projectId);
        if (!portfolioProjectRepository.existsById(id)) {
            PortfolioProjectEntity entity = new PortfolioProjectEntity(id, priority);
            portfolioProjectRepository.save(entity);
        }
    }

    @Override
    public void removeProject(UUID portfolioId, UUID projectId) {
        PortfolioProjectId id = new PortfolioProjectId(portfolioId, projectId);
        portfolioProjectRepository.deleteById(id);
    }

    @Override
    public List<UUID> findProjectIdsByPortfolioId(UUID portfolioId) {
        return portfolioProjectRepository.findProjectIdsByPortfolioId(portfolioId);
    }

    private PortfolioEntity toEntity(Portfolio domain) {
        PortfolioEntity entity = new PortfolioEntity();
        entity.setId(domain.getId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setOwnerId(domain.getOwnerId());
        return entity;
    }

    private Portfolio toDomain(PortfolioEntity entity) {
        Portfolio domain = new Portfolio();
        domain.setId(entity.getId());
        domain.setOrganizationId(entity.getOrganizationId());
        domain.setName(entity.getName());
        domain.setDescription(entity.getDescription());
        domain.setOwnerId(entity.getOwnerId());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
