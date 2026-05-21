package com.agileforge.application.service;

import com.agileforge.application.dto.response.PortfolioDashboardResponse;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Portfolio;
import com.agileforge.domain.model.Project;
import com.agileforge.domain.port.out.PortfolioRepositoryPort;
import com.agileforge.domain.port.out.ProjectRepositoryPort;
import com.agileforge.domain.port.out.SprintRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock private PortfolioRepositoryPort portfolioRepository;
    @Mock private ProjectRepositoryPort projectRepository;
    @Mock private TicketRepositoryPort ticketRepository;
    @Mock private SprintRepositoryPort sprintRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void shouldCreatePortfolio() {
        UUID orgId = UUID.randomUUID();
        Portfolio portfolio = new Portfolio(orgId, "Test Portfolio", "desc", UUID.randomUUID());
        portfolio.setId(UUID.randomUUID());
        when(portfolioRepository.save(any())).thenReturn(portfolio);

        Portfolio result = portfolioService.createPortfolio(orgId, "Test Portfolio", "desc", UUID.randomUUID(), null);

        assertNotNull(result);
        assertEquals("Test Portfolio", result.getName());
    }

    @Test
    void shouldCreatePortfolioWithProjects() {
        UUID orgId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Portfolio portfolio = new Portfolio(orgId, "Test", "desc", UUID.randomUUID());
        portfolio.setId(UUID.randomUUID());
        when(portfolioRepository.save(any())).thenReturn(portfolio);

        portfolioService.createPortfolio(orgId, "Test", "desc", UUID.randomUUID(), List.of(projectId));

        verify(portfolioRepository).addProject(any(), eq(projectId), eq(0));
    }

    @Test
    void shouldGetById() {
        UUID id = UUID.randomUUID();
        Portfolio portfolio = new Portfolio(UUID.randomUUID(), "Test", null, null);
        portfolio.setId(id);
        when(portfolioRepository.findById(id)).thenReturn(Optional.of(portfolio));

        Portfolio result = portfolioService.getById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(portfolioRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> portfolioService.getById(id));
    }

    @Test
    void shouldGetByOrganization() {
        UUID orgId = UUID.randomUUID();
        when(portfolioRepository.findByOrganizationId(orgId)).thenReturn(List.of());

        List<Portfolio> result = portfolioService.getByOrganization(orgId);

        assertNotNull(result);
    }

    @Test
    void shouldUpdatePortfolio() {
        UUID id = UUID.randomUUID();
        Portfolio portfolio = new Portfolio(UUID.randomUUID(), "Old", "old desc", null);
        portfolio.setId(id);
        when(portfolioRepository.findById(id)).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Portfolio result = portfolioService.updatePortfolio(id, "New", "new desc");

        assertEquals("New", result.getName());
    }

    @Test
    void shouldDeletePortfolio() {
        UUID id = UUID.randomUUID();
        Portfolio portfolio = new Portfolio(UUID.randomUUID(), "Test", null, null);
        portfolio.setId(id);
        when(portfolioRepository.findById(id)).thenReturn(Optional.of(portfolio));

        portfolioService.deletePortfolio(id);

        verify(portfolioRepository).delete(id);
    }

    @Test
    void shouldAddProject() {
        UUID portfolioId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Portfolio portfolio = new Portfolio(UUID.randomUUID(), "Test", null, null);
        portfolio.setId(portfolioId);
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));

        portfolioService.addProject(portfolioId, projectId);

        verify(portfolioRepository).addProject(portfolioId, projectId, 0);
    }

    @Test
    void shouldRemoveProject() {
        UUID portfolioId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Portfolio portfolio = new Portfolio(UUID.randomUUID(), "Test", null, null);
        portfolio.setId(portfolioId);
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));

        portfolioService.removeProject(portfolioId, projectId);

        verify(portfolioRepository).removeProject(portfolioId, projectId);
    }

    @Test
    void shouldGetDashboard() {
        UUID portfolioId = UUID.randomUUID();
        Portfolio portfolio = new Portfolio(UUID.randomUUID(), "Test", null, null);
        portfolio.setId(portfolioId);
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.findProjectIdsByPortfolioId(portfolioId)).thenReturn(List.of());

        PortfolioDashboardResponse result = portfolioService.getDashboard(portfolioId);

        assertNotNull(result);
        assertEquals(0, result.totalProjects());
    }
}
