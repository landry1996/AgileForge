package com.agileforge.domain.port.out;

import com.agileforge.domain.model.ClientFeedback;
import com.agileforge.domain.model.ClientPortal;
import com.agileforge.domain.model.ClientUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientPortalRepositoryPort {

    ClientPortal savePortal(ClientPortal portal);

    Optional<ClientPortal> findPortalByProjectId(UUID projectId);

    List<ClientUser> findClientUsers(UUID portalId);

    ClientUser saveClientUser(ClientUser clientUser);

    void deleteClientUser(UUID clientUserId);

    ClientFeedback saveFeedback(ClientFeedback feedback);

    List<ClientFeedback> findFeedbackByPortalId(UUID portalId);

    List<ClientFeedback> findFeedbackByTicketId(UUID ticketId);
}
