package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Invitation;
import com.agileforge.domain.model.InvitationStatus;
import com.agileforge.domain.port.out.InvitationRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.InvitationEntity;
import com.agileforge.infrastructure.persistence.repository.JpaInvitationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InvitationRepositoryAdapter implements InvitationRepositoryPort {

    private final JpaInvitationRepository repository;

    public InvitationRepositoryAdapter(JpaInvitationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Invitation save(Invitation invitation) {
        InvitationEntity entity = toEntity(invitation);
        InvitationEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Invitation> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        return repository.findByToken(token).map(this::toDomain);
    }

    @Override
    public List<Invitation> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Invitation> findByEmail(String email) {
        return repository.findByEmail(email).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<Invitation> findPendingByOrganizationIdAndEmail(UUID orgId, String email) {
        return repository.findPendingByOrganizationIdAndEmail(orgId, email).map(this::toDomain);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private InvitationEntity toEntity(Invitation domain) {
        InvitationEntity entity = new InvitationEntity();
        entity.setId(domain.getId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setEmail(domain.getEmail());
        entity.setRole(domain.getRole());
        entity.setToken(domain.getToken());
        entity.setStatus(domain.getStatus().name());
        entity.setInvitedBy(domain.getInvitedBy());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setAcceptedAt(domain.getAcceptedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private Invitation toDomain(InvitationEntity entity) {
        Invitation domain = new Invitation();
        domain.setId(entity.getId());
        domain.setOrganizationId(entity.getOrganizationId());
        domain.setEmail(entity.getEmail());
        domain.setRole(entity.getRole());
        domain.setToken(entity.getToken());
        domain.setStatus(InvitationStatus.valueOf(entity.getStatus()));
        domain.setInvitedBy(entity.getInvitedBy());
        domain.setExpiresAt(entity.getExpiresAt());
        domain.setAcceptedAt(entity.getAcceptedAt());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
