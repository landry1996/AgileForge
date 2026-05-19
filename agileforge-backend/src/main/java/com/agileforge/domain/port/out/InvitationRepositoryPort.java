package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Invitation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepositoryPort {

    Invitation save(Invitation invitation);

    Optional<Invitation> findById(UUID id);

    Optional<Invitation> findByToken(String token);

    List<Invitation> findByOrganizationId(UUID organizationId);

    List<Invitation> findByEmail(String email);

    Optional<Invitation> findPendingByOrganizationIdAndEmail(UUID orgId, String email);

    void delete(UUID id);
}
