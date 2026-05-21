package com.agileforge.application.service;

import com.agileforge.application.dto.response.ClientPortalViewResponse;
import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.*;
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
class ClientPortalServiceTest {

    @Mock private ClientPortalRepositoryPort portalRepository;
    @Mock private ProjectRepositoryPort projectRepository;
    @Mock private ReleaseRepositoryPort releaseRepository;
    @Mock private RoadmapItemRepositoryPort roadmapItemRepository;
    @Mock private TicketRepositoryPort ticketRepository;

    @InjectMocks
    private ClientPortalService clientPortalService;

    @Test
    void shouldConfigureNewPortal() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));
        when(portalRepository.findPortalByProjectId(projectId)).thenReturn(Optional.empty());
        ClientPortal portal = new ClientPortal(projectId);
        portal.setId(UUID.randomUUID());
        when(portalRepository.savePortal(any())).thenReturn(portal);

        ClientPortal result = clientPortalService.configurePortal(projectId, true, "Welcome!", null, true, true, true);

        assertNotNull(result);
        verify(portalRepository).savePortal(any());
    }

    @Test
    void shouldConfigureExistingPortal() {
        UUID projectId = UUID.randomUUID();
        ClientPortal existing = new ClientPortal(projectId);
        existing.setId(UUID.randomUUID());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));
        when(portalRepository.findPortalByProjectId(projectId)).thenReturn(Optional.of(existing));
        when(portalRepository.savePortal(any())).thenAnswer(inv -> inv.getArgument(0));

        ClientPortal result = clientPortalService.configurePortal(projectId, false, "Updated", null, false, false, false);

        assertNotNull(result);
    }

    @Test
    void shouldGetPortalConfig() {
        UUID projectId = UUID.randomUUID();
        ClientPortal portal = new ClientPortal(projectId);
        portal.setId(UUID.randomUUID());
        when(portalRepository.findPortalByProjectId(projectId)).thenReturn(Optional.of(portal));

        ClientPortal result = clientPortalService.getPortalConfig(projectId);

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenPortalNotFound() {
        UUID projectId = UUID.randomUUID();
        when(portalRepository.findPortalByProjectId(projectId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientPortalService.getPortalConfig(projectId));
    }

    @Test
    void shouldAddClientUser() {
        UUID projectId = UUID.randomUUID();
        UUID portalId = UUID.randomUUID();
        ClientPortal portal = new ClientPortal(projectId);
        portal.setId(portalId);
        when(portalRepository.findPortalByProjectId(projectId)).thenReturn(Optional.of(portal));
        ClientUser user = new ClientUser(portalId, "client@test.com", "Client", "Corp");
        user.setId(UUID.randomUUID());
        when(portalRepository.saveClientUser(any())).thenReturn(user);

        ClientUser result = clientPortalService.addClientUser(projectId, "client@test.com", "Client", "Corp");

        assertNotNull(result);
        assertEquals("client@test.com", result.getEmail());
    }

    @Test
    void shouldRemoveClientUser() {
        UUID clientUserId = UUID.randomUUID();
        doNothing().when(portalRepository).deleteClientUser(clientUserId);

        clientPortalService.removeClientUser(clientUserId);

        verify(portalRepository).deleteClientUser(clientUserId);
    }

    @Test
    void shouldSubmitFeedback() {
        UUID portalId = UUID.randomUUID();
        UUID clientUserId = UUID.randomUUID();
        ClientFeedback feedback = new ClientFeedback(portalId, null, clientUserId, FeedbackType.COMMENT, "Great!", 5);
        feedback.setId(UUID.randomUUID());
        when(portalRepository.saveFeedback(any())).thenReturn(feedback);

        ClientFeedback result = clientPortalService.submitFeedback(portalId, clientUserId, null, "COMMENT", "Great!", 5);

        assertNotNull(result);
        assertEquals(5, result.getRating());
    }

    @Test
    void shouldGetPortalViewWhenEnabled() {
        UUID projectId = UUID.randomUUID();
        ClientPortal portal = new ClientPortal(projectId);
        portal.setId(UUID.randomUUID());
        portal.setEnabled(true);
        portal.setWelcomeMessage("Hello");
        portal.setShowRoadmap(true);
        portal.setShowReleases(true);
        when(portalRepository.findPortalByProjectId(projectId)).thenReturn(Optional.of(portal));
        Project project = new Project();
        project.setName("Test Project");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(ticketRepository.findByProjectId(projectId)).thenReturn(List.of());
        when(roadmapItemRepository.findByProjectId(projectId)).thenReturn(List.of());
        when(releaseRepository.findByProjectId(projectId)).thenReturn(List.of());

        ClientPortalViewResponse result = clientPortalService.getPortalView(projectId);

        assertNotNull(result);
        assertEquals("Test Project", result.projectName());
    }

    @Test
    void shouldThrowWhenPortalDisabled() {
        UUID projectId = UUID.randomUUID();
        ClientPortal portal = new ClientPortal(projectId);
        portal.setId(UUID.randomUUID());
        portal.setEnabled(false);
        when(portalRepository.findPortalByProjectId(projectId)).thenReturn(Optional.of(portal));

        assertThrows(BusinessException.class, () -> clientPortalService.getPortalView(projectId));
    }
}
