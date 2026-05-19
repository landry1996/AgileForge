package com.agileforge.application.service;

import com.agileforge.application.dto.response.ClientPortalViewResponse;
import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.ClientPortalRepositoryPort;
import com.agileforge.domain.port.out.ProjectRepositoryPort;
import com.agileforge.domain.port.out.ReleaseRepositoryPort;
import com.agileforge.domain.port.out.RoadmapItemRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ClientPortalService {

    private static final Logger log = LoggerFactory.getLogger(ClientPortalService.class);

    private final ClientPortalRepositoryPort portalRepository;
    private final ProjectRepositoryPort projectRepository;
    private final ReleaseRepositoryPort releaseRepository;
    private final RoadmapItemRepositoryPort roadmapItemRepository;
    private final TicketRepositoryPort ticketRepository;

    public ClientPortalService(ClientPortalRepositoryPort portalRepository,
                               ProjectRepositoryPort projectRepository,
                               ReleaseRepositoryPort releaseRepository,
                               RoadmapItemRepositoryPort roadmapItemRepository,
                               TicketRepositoryPort ticketRepository) {
        this.portalRepository = portalRepository;
        this.projectRepository = projectRepository;
        this.releaseRepository = releaseRepository;
        this.roadmapItemRepository = roadmapItemRepository;
        this.ticketRepository = ticketRepository;
    }

    public ClientPortal configurePortal(UUID projectId, Boolean isEnabled, String welcomeMessage,
                                        List<String> allowedTicketTypes, Boolean showRoadmap,
                                        Boolean showReleases, Boolean showChangelog) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));

        ClientPortal portal = portalRepository.findPortalByProjectId(projectId)
                .orElse(new ClientPortal(projectId));

        if (isEnabled != null) portal.setEnabled(isEnabled);
        if (welcomeMessage != null) portal.setWelcomeMessage(welcomeMessage);
        if (allowedTicketTypes != null) portal.setAllowedTicketTypes(String.join(",", allowedTicketTypes));
        if (showRoadmap != null) portal.setShowRoadmap(showRoadmap);
        if (showReleases != null) portal.setShowReleases(showReleases);
        if (showChangelog != null) portal.setShowChangelog(showChangelog);

        ClientPortal saved = portalRepository.savePortal(portal);
        log.info("Client portal configured for project {}", projectId);
        return saved;
    }

    @Transactional(readOnly = true)
    public ClientPortal getPortalConfig(UUID projectId) {
        return portalRepository.findPortalByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("ClientPortal", projectId));
    }

    public ClientUser addClientUser(UUID projectId, String email, String name, String company) {
        ClientPortal portal = portalRepository.findPortalByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("ClientPortal", projectId));

        ClientUser clientUser = new ClientUser(portal.getId(), email, name, company);
        ClientUser saved = portalRepository.saveClientUser(clientUser);
        log.info("Client user added: {} to portal for project {}", email, projectId);
        return saved;
    }

    public void removeClientUser(UUID clientUserId) {
        portalRepository.deleteClientUser(clientUserId);
        log.info("Client user removed: {}", clientUserId);
    }

    @Transactional(readOnly = true)
    public List<ClientUser> getClientUsers(UUID projectId) {
        ClientPortal portal = portalRepository.findPortalByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("ClientPortal", projectId));
        return portalRepository.findClientUsers(portal.getId());
    }

    public ClientFeedback submitFeedback(UUID portalId, UUID clientUserId, UUID ticketId,
                                         String type, String content, Integer rating) {
        FeedbackType feedbackType = type != null ? FeedbackType.valueOf(type) : FeedbackType.COMMENT;
        ClientFeedback feedback = new ClientFeedback(portalId, ticketId, clientUserId, feedbackType, content, rating);
        ClientFeedback saved = portalRepository.saveFeedback(feedback);
        log.info("Client feedback submitted for portal {}", portalId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ClientFeedback> getFeedbackByProject(UUID projectId) {
        ClientPortal portal = portalRepository.findPortalByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("ClientPortal", projectId));
        return portalRepository.findFeedbackByPortalId(portal.getId());
    }

    @Transactional(readOnly = true)
    public List<ClientFeedback> getFeedbackByTicket(UUID ticketId) {
        return portalRepository.findFeedbackByTicketId(ticketId);
    }

    @Transactional(readOnly = true)
    public ClientPortalViewResponse getPortalView(UUID projectId) {
        ClientPortal portal = portalRepository.findPortalByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("ClientPortal", projectId));

        if (!portal.isEnabled()) {
            throw new BusinessException("Client portal is not enabled for this project");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));

        // Public tickets
        List<Map<String, Object>> publicTickets = ticketRepository.findByProjectId(projectId)
                .stream()
                .limit(50)
                .map(t -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", t.getId());
                    map.put("title", t.getTitle());
                    map.put("type", t.getType() != null ? t.getType().name() : null);
                    map.put("status", t.getStatus() != null ? t.getStatus().name() : null);
                    map.put("priority", t.getPriority() != null ? t.getPriority().name() : null);
                    map.put("createdAt", t.getCreatedAt());
                    return map;
                })
                .toList();

        // Roadmap items
        List<Map<String, Object>> roadmapItems = new ArrayList<>();
        if (portal.isShowRoadmap()) {
            roadmapItems = roadmapItemRepository.findByProjectId(projectId).stream()
                    .map(r -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("id", r.getId());
                        map.put("title", r.getTitle());
                        map.put("status", r.getStatus() != null ? r.getStatus().name() : null);
                        map.put("targetDate", r.getEndDate());
                        return map;
                    })
                    .toList();
        }

        // Releases
        List<Map<String, Object>> releases = new ArrayList<>();
        if (portal.isShowReleases()) {
            releases = releaseRepository.findByProjectId(projectId).stream()
                    .map(r -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("id", r.getId());
                        map.put("name", r.getName());
                        map.put("version", r.getVersion());
                        map.put("status", r.getStatus() != null ? r.getStatus().name() : null);
                        map.put("releaseDate", r.getReleaseDate());
                        return map;
                    })
                    .toList();
        }

        return new ClientPortalViewResponse(
                project.getName(),
                portal.getWelcomeMessage(),
                publicTickets,
                roadmapItems,
                releases
        );
    }
}
