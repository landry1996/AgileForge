package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.TicketLink;
import com.agileforge.domain.model.TicketLinkType;
import com.agileforge.domain.port.out.TicketLinkRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.TicketLinkEntity;
import com.agileforge.infrastructure.persistence.repository.JpaTicketLinkRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TicketLinkRepositoryAdapter implements TicketLinkRepositoryPort {

    private final JpaTicketLinkRepository repository;

    public TicketLinkRepositoryAdapter(JpaTicketLinkRepository repository) {
        this.repository = repository;
    }

    @Override
    public TicketLink save(TicketLink ticketLink) {
        TicketLinkEntity entity = toEntity(ticketLink);
        TicketLinkEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TicketLink> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<TicketLink> findBySourceTicketId(UUID sourceTicketId) {
        return repository.findBySourceTicketId(sourceTicketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<TicketLink> findByTargetTicketId(UUID targetTicketId) {
        return repository.findByTargetTicketId(targetTicketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<TicketLink> findAllByTicketId(UUID ticketId) {
        return repository.findBySourceTicketIdOrTargetTicketId(ticketId, ticketId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void delete(UUID sourceTicketId, UUID targetTicketId, TicketLinkType type) {
        repository.deleteBySourceTicketIdAndTargetTicketIdAndLinkType(
                sourceTicketId, targetTicketId, type.name());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsLink(UUID sourceId, UUID targetId, TicketLinkType type) {
        return repository.existsBySourceTicketIdAndTargetTicketIdAndLinkType(
                sourceId, targetId, type.name());
    }

    private TicketLinkEntity toEntity(TicketLink domain) {
        TicketLinkEntity entity = new TicketLinkEntity();
        entity.setId(domain.getId());
        entity.setSourceTicketId(domain.getSourceTicketId());
        entity.setTargetTicketId(domain.getTargetTicketId());
        entity.setLinkType(domain.getLinkType().name());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        entity.setCreatedBy(domain.getCreatedBy());
        return entity;
    }

    private TicketLink toDomain(TicketLinkEntity entity) {
        TicketLink domain = new TicketLink();
        domain.setId(entity.getId());
        domain.setSourceTicketId(entity.getSourceTicketId());
        domain.setTargetTicketId(entity.getTargetTicketId());
        domain.setLinkType(TicketLinkType.valueOf(entity.getLinkType()));
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setCreatedBy(entity.getCreatedBy());
        return domain;
    }
}
