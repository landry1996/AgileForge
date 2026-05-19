package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Release;
import com.agileforge.domain.model.ReleaseStatus;
import com.agileforge.domain.port.out.ReleaseRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.ReleaseEntity;
import com.agileforge.infrastructure.persistence.entity.ReleaseTicketEntity;
import com.agileforge.infrastructure.persistence.entity.ReleaseTicketId;
import com.agileforge.infrastructure.persistence.repository.JpaReleaseRepository;
import com.agileforge.infrastructure.persistence.repository.JpaReleaseTicketRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ReleaseRepositoryAdapter implements ReleaseRepositoryPort {

    private final JpaReleaseRepository releaseRepository;
    private final JpaReleaseTicketRepository releaseTicketRepository;

    public ReleaseRepositoryAdapter(JpaReleaseRepository releaseRepository,
                                    JpaReleaseTicketRepository releaseTicketRepository) {
        this.releaseRepository = releaseRepository;
        this.releaseTicketRepository = releaseTicketRepository;
    }

    @Override
    public Release save(Release release) {
        ReleaseEntity entity = toEntity(release);
        ReleaseEntity saved = releaseRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Release> findById(UUID id) {
        return releaseRepository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public List<Release> findByProjectId(UUID projectId) {
        return releaseRepository.findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Release> findByProjectIdAndStatus(UUID projectId, ReleaseStatus status) {
        return releaseRepository.findByProjectIdAndStatusAndDeletedFalse(projectId, status.name()).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        releaseRepository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            releaseRepository.save(entity);
        });
    }

    @Override
    public void addTicketToRelease(UUID releaseId, UUID ticketId) {
        ReleaseTicketId id = new ReleaseTicketId(releaseId, ticketId);
        if (!releaseTicketRepository.existsById(id)) {
            releaseTicketRepository.save(new ReleaseTicketEntity(id));
        }
    }

    @Override
    public void removeTicketFromRelease(UUID releaseId, UUID ticketId) {
        ReleaseTicketId id = new ReleaseTicketId(releaseId, ticketId);
        releaseTicketRepository.deleteById(id);
    }

    @Override
    public List<UUID> findTicketIdsByReleaseId(UUID releaseId) {
        return releaseTicketRepository.findTicketIdsByReleaseId(releaseId);
    }

    private ReleaseEntity toEntity(Release domain) {
        ReleaseEntity entity = new ReleaseEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setName(domain.getName());
        entity.setVersion(domain.getVersion());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus().name());
        entity.setStartDate(domain.getStartDate());
        entity.setReleaseDate(domain.getReleaseDate());
        entity.setReleasedAt(domain.getReleasedAt());
        return entity;
    }

    private Release toDomain(ReleaseEntity entity) {
        Release domain = new Release();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setName(entity.getName());
        domain.setVersion(entity.getVersion());
        domain.setDescription(entity.getDescription());
        domain.setStatus(ReleaseStatus.valueOf(entity.getStatus()));
        domain.setStartDate(entity.getStartDate());
        domain.setReleaseDate(entity.getReleaseDate());
        domain.setReleasedAt(entity.getReleasedAt());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
