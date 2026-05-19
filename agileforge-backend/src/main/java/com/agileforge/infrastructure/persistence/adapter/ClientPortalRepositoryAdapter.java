package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.ClientFeedback;
import com.agileforge.domain.model.ClientPortal;
import com.agileforge.domain.model.ClientUser;
import com.agileforge.domain.model.FeedbackType;
import com.agileforge.domain.port.out.ClientPortalRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.ClientFeedbackEntity;
import com.agileforge.infrastructure.persistence.entity.ClientPortalEntity;
import com.agileforge.infrastructure.persistence.entity.ClientUserEntity;
import com.agileforge.infrastructure.persistence.repository.JpaClientFeedbackRepository;
import com.agileforge.infrastructure.persistence.repository.JpaClientPortalRepository;
import com.agileforge.infrastructure.persistence.repository.JpaClientUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ClientPortalRepositoryAdapter implements ClientPortalRepositoryPort {

    private final JpaClientPortalRepository portalRepository;
    private final JpaClientUserRepository clientUserRepository;
    private final JpaClientFeedbackRepository feedbackRepository;

    public ClientPortalRepositoryAdapter(JpaClientPortalRepository portalRepository,
                                         JpaClientUserRepository clientUserRepository,
                                         JpaClientFeedbackRepository feedbackRepository) {
        this.portalRepository = portalRepository;
        this.clientUserRepository = clientUserRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public ClientPortal savePortal(ClientPortal portal) {
        ClientPortalEntity entity = portalToEntity(portal);
        ClientPortalEntity saved = portalRepository.save(entity);
        return portalToDomain(saved);
    }

    @Override
    public Optional<ClientPortal> findPortalByProjectId(UUID projectId) {
        return portalRepository.findByProjectId(projectId).map(this::portalToDomain);
    }

    @Override
    public List<ClientUser> findClientUsers(UUID portalId) {
        return clientUserRepository.findByPortalIdAndActiveTrue(portalId)
                .stream().map(this::clientUserToDomain).toList();
    }

    @Override
    public ClientUser saveClientUser(ClientUser clientUser) {
        ClientUserEntity entity = clientUserToEntity(clientUser);
        ClientUserEntity saved = clientUserRepository.save(entity);
        return clientUserToDomain(saved);
    }

    @Override
    public void deleteClientUser(UUID clientUserId) {
        clientUserRepository.deleteById(clientUserId);
    }

    @Override
    public ClientFeedback saveFeedback(ClientFeedback feedback) {
        ClientFeedbackEntity entity = feedbackToEntity(feedback);
        ClientFeedbackEntity saved = feedbackRepository.save(entity);
        return feedbackToDomain(saved);
    }

    @Override
    public List<ClientFeedback> findFeedbackByPortalId(UUID portalId) {
        return feedbackRepository.findByPortalIdOrderByCreatedAtDesc(portalId)
                .stream().map(this::feedbackToDomain).toList();
    }

    @Override
    public List<ClientFeedback> findFeedbackByTicketId(UUID ticketId) {
        return feedbackRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream().map(this::feedbackToDomain).toList();
    }

    // Portal mapping
    private ClientPortalEntity portalToEntity(ClientPortal domain) {
        ClientPortalEntity entity = new ClientPortalEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setEnabled(domain.isEnabled());
        entity.setWelcomeMessage(domain.getWelcomeMessage());
        entity.setAllowedTicketTypes(domain.getAllowedTicketTypes());
        entity.setShowRoadmap(domain.isShowRoadmap());
        entity.setShowReleases(domain.isShowReleases());
        entity.setShowChangelog(domain.isShowChangelog());
        entity.setCustomBranding(domain.getCustomBranding());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private ClientPortal portalToDomain(ClientPortalEntity entity) {
        ClientPortal domain = new ClientPortal();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setEnabled(entity.isEnabled());
        domain.setWelcomeMessage(entity.getWelcomeMessage());
        domain.setAllowedTicketTypes(entity.getAllowedTicketTypes());
        domain.setShowRoadmap(entity.isShowRoadmap());
        domain.setShowReleases(entity.isShowReleases());
        domain.setShowChangelog(entity.isShowChangelog());
        domain.setCustomBranding(entity.getCustomBranding());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    // ClientUser mapping
    private ClientUserEntity clientUserToEntity(ClientUser domain) {
        ClientUserEntity entity = new ClientUserEntity();
        entity.setId(domain.getId());
        entity.setPortalId(domain.getPortalId());
        entity.setEmail(domain.getEmail());
        entity.setName(domain.getName());
        entity.setCompany(domain.getCompany());
        entity.setActive(domain.isActive());
        entity.setLastLoginAt(domain.getLastLoginAt());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private ClientUser clientUserToDomain(ClientUserEntity entity) {
        ClientUser domain = new ClientUser();
        domain.setId(entity.getId());
        domain.setPortalId(entity.getPortalId());
        domain.setEmail(entity.getEmail());
        domain.setName(entity.getName());
        domain.setCompany(entity.getCompany());
        domain.setActive(entity.isActive());
        domain.setLastLoginAt(entity.getLastLoginAt());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }

    // Feedback mapping
    private ClientFeedbackEntity feedbackToEntity(ClientFeedback domain) {
        ClientFeedbackEntity entity = new ClientFeedbackEntity();
        entity.setId(domain.getId());
        entity.setPortalId(domain.getPortalId());
        entity.setTicketId(domain.getTicketId());
        entity.setClientUserId(domain.getClientUserId());
        entity.setType(domain.getType() != null ? domain.getType().name() : "COMMENT");
        entity.setContent(domain.getContent());
        entity.setRating(domain.getRating());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private ClientFeedback feedbackToDomain(ClientFeedbackEntity entity) {
        ClientFeedback domain = new ClientFeedback();
        domain.setId(entity.getId());
        domain.setPortalId(entity.getPortalId());
        domain.setTicketId(entity.getTicketId());
        domain.setClientUserId(entity.getClientUserId());
        domain.setType(entity.getType() != null ? FeedbackType.valueOf(entity.getType()) : FeedbackType.COMMENT);
        domain.setContent(entity.getContent());
        domain.setRating(entity.getRating());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
