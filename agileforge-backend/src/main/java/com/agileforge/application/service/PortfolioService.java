package com.agileforge.application.service;

import com.agileforge.application.dto.response.PortfolioDashboardResponse;
import com.agileforge.application.dto.response.PortfolioProjectSummary;
import com.agileforge.application.dto.response.RiskHeatMapEntry;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Portfolio;
import com.agileforge.domain.model.Project;
import com.agileforge.domain.model.Sprint;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketStatus;
import com.agileforge.domain.port.out.PortfolioRepositoryPort;
import com.agileforge.domain.port.out.ProjectRepositoryPort;
import com.agileforge.domain.port.out.SprintRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

    private final PortfolioRepositoryPort portfolioRepository;
    private final ProjectRepositoryPort projectRepository;
    private final TicketRepositoryPort ticketRepository;
    private final SprintRepositoryPort sprintRepository;

    public PortfolioService(PortfolioRepositoryPort portfolioRepository,
                            ProjectRepositoryPort projectRepository,
                            TicketRepositoryPort ticketRepository,
                            SprintRepositoryPort sprintRepository) {
        this.portfolioRepository = portfolioRepository;
        this.projectRepository = projectRepository;
        this.ticketRepository = ticketRepository;
        this.sprintRepository = sprintRepository;
    }

    public Portfolio createPortfolio(UUID organizationId, String name, String description, UUID ownerId, List<UUID> projectIds) {
        Portfolio portfolio = new Portfolio(organizationId, name, description, ownerId);
        Portfolio saved = portfolioRepository.save(portfolio);

        if (projectIds != null) {
            for (int i = 0; i < projectIds.size(); i++) {
                portfolioRepository.addProject(saved.getId(), projectIds.get(i), i);
            }
        }

        log.info("Portfolio created: {} in organization {}", name, organizationId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Portfolio getById(UUID portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio", portfolioId));
    }

    @Transactional(readOnly = true)
    public List<Portfolio> getByOrganization(UUID organizationId) {
        return portfolioRepository.findByOrganizationId(organizationId);
    }

    public Portfolio updatePortfolio(UUID portfolioId, String name, String description) {
        Portfolio portfolio = getById(portfolioId);

        if (name != null) portfolio.setName(name);
        if (description != null) portfolio.setDescription(description);

        Portfolio saved = portfolioRepository.save(portfolio);
        log.info("Portfolio updated: {}", portfolioId);
        return saved;
    }

    public void deletePortfolio(UUID portfolioId) {
        getById(portfolioId);
        portfolioRepository.delete(portfolioId);
        log.info("Portfolio deleted: {}", portfolioId);
    }

    public void addProject(UUID portfolioId, UUID projectId) {
        getById(portfolioId);
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));
        portfolioRepository.addProject(portfolioId, projectId, 0);
        log.info("Project {} added to portfolio {}", projectId, portfolioId);
    }

    public void removeProject(UUID portfolioId, UUID projectId) {
        getById(portfolioId);
        portfolioRepository.removeProject(portfolioId, projectId);
        log.info("Project {} removed from portfolio {}", projectId, portfolioId);
    }

    @Transactional(readOnly = true)
    public PortfolioDashboardResponse getDashboard(UUID portfolioId) {
        Portfolio portfolio = getById(portfolioId);
        List<UUID> projectIds = portfolioRepository.findProjectIdsByPortfolioId(portfolioId);

        List<PortfolioProjectSummary> summaries = new ArrayList<>();
        List<RiskHeatMapEntry> riskEntries = new ArrayList<>();
        int totalHealthScore = 0;

        for (UUID projectId : projectIds) {
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) continue;

            Project project = projectOpt.get();
            PortfolioProjectSummary summary = buildProjectSummary(project);
            summaries.add(summary);
            totalHealthScore += summary.healthScore();

            RiskHeatMapEntry riskEntry = buildRiskEntry(project, summary);
            riskEntries.add(riskEntry);
        }

        int overallHealth = projectIds.isEmpty() ? 0 : totalHealthScore / projectIds.size();

        return new PortfolioDashboardResponse(
                portfolioId,
                portfolio.getName(),
                projectIds.size(),
                overallHealth,
                summaries,
                riskEntries
        );
    }

    @Transactional(readOnly = true)
    public List<RiskHeatMapEntry> getRiskHeatMap(UUID portfolioId) {
        getById(portfolioId);
        List<UUID> projectIds = portfolioRepository.findProjectIdsByPortfolioId(portfolioId);

        List<RiskHeatMapEntry> riskEntries = new ArrayList<>();
        for (UUID projectId : projectIds) {
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) continue;

            Project project = projectOpt.get();
            PortfolioProjectSummary summary = buildProjectSummary(project);
            riskEntries.add(buildRiskEntry(project, summary));
        }

        return riskEntries;
    }

    @Transactional(readOnly = true)
    public List<PortfolioProjectSummary> getProjectSummaries(UUID portfolioId) {
        getById(portfolioId);
        List<UUID> projectIds = portfolioRepository.findProjectIdsByPortfolioId(portfolioId);

        List<PortfolioProjectSummary> summaries = new ArrayList<>();
        for (UUID projectId : projectIds) {
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) continue;
            summaries.add(buildProjectSummary(projectOpt.get()));
        }
        return summaries;
    }

    private PortfolioProjectSummary buildProjectSummary(Project project) {
        UUID projectId = project.getId();
        long totalTickets = ticketRepository.countByProjectId(projectId);
        long openTickets = totalTickets - ticketRepository.countByProjectIdAndStatus(projectId, TicketStatus.DONE)
                - ticketRepository.countByProjectIdAndStatus(projectId, TicketStatus.CANCELLED);

        // Calculate active sprint progress
        int activeSprintProgress = 0;
        Optional<Sprint> activeSprint = sprintRepository.findActiveByProjectId(projectId);
        if (activeSprint.isPresent()) {
            List<Ticket> sprintTickets = ticketRepository.findBySprintId(activeSprint.get().getId());
            if (!sprintTickets.isEmpty()) {
                long doneInSprint = sprintTickets.stream().filter(Ticket::isDone).count();
                activeSprintProgress = (int) ((doneInSprint * 100) / sprintTickets.size());
            }
        }

        // Health score based on completion ratio and blocked tickets
        int healthScore = calculateHealthScore(projectId, totalTickets, openTickets);

        return new PortfolioProjectSummary(
                projectId,
                project.getName(),
                project.getKey(),
                healthScore,
                activeSprintProgress,
                totalTickets,
                openTickets
        );
    }

    private int calculateHealthScore(UUID projectId, long totalTickets, long openTickets) {
        if (totalTickets == 0) return 100;

        double completionRatio = (double) (totalTickets - openTickets) / totalTickets;
        long blockedCount = ticketRepository.countByProjectIdAndStatus(projectId, TicketStatus.BLOCKED);
        double blockedRatio = (double) blockedCount / totalTickets;

        // Score: 70% based on completion, 30% penalty for blocked items
        int score = (int) ((completionRatio * 70) + (30 * (1 - blockedRatio)));
        return Math.max(0, Math.min(100, score));
    }

    private RiskHeatMapEntry buildRiskEntry(Project project, PortfolioProjectSummary summary) {
        int riskScore = 100 - summary.healthScore();
        String riskLevel;

        if (riskScore >= 70) {
            riskLevel = "CRITICAL";
        } else if (riskScore >= 50) {
            riskLevel = "HIGH";
        } else if (riskScore >= 30) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        return new RiskHeatMapEntry(project.getId(), project.getName(), riskLevel, riskScore);
    }
}
