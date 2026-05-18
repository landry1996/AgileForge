package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketPriority;
import com.agileforge.domain.model.TicketStatus;
import com.agileforge.domain.model.TicketType;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.TicketEntity;
import com.agileforge.infrastructure.persistence.repository.JpaTicketRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TicketRepositoryAdapter implements TicketRepositoryPort {

    private final JpaTicketRepository repository;

    public TicketRepositoryAdapter(JpaTicketRepository repository) {
        this.repository = repository;
    }

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = toEntity(ticket);
        TicketEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return repository.findByIdAndDeletedFalse(id).map(this::toDomain);
    }

    @Override
    public Optional<Ticket> findByProjectIdAndNumber(UUID projectId, long number) {
        return repository.findByProjectIdAndNumberAndDeletedFalse(projectId, number).map(this::toDomain);
    }

    @Override
    public List<Ticket> findByProjectId(UUID projectId) {
        return repository.findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Ticket> findByProjectIdAndStatus(UUID projectId, TicketStatus status) {
        return repository.findByProjectIdAndStatusAndDeletedFalse(projectId, status.name()).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Ticket> findByProjectIdAndType(UUID projectId, TicketType type) {
        return repository.findByProjectIdAndTypeAndDeletedFalse(projectId, type.name()).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Ticket> findByAssigneeId(UUID assigneeId) {
        return repository.findByAssigneeIdAndDeletedFalseOrderByPriorityAscCreatedAtDesc(assigneeId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Ticket> findBySprintId(UUID sprintId) {
        return repository.findBySprintIdAndDeletedFalse(sprintId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Ticket> findByEpicId(UUID epicId) {
        return repository.findByEpicIdAndDeletedFalse(epicId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public long getNextNumber(UUID projectId) {
        return repository.getNextNumber(projectId);
    }

    @Override
    public long countByProjectId(UUID projectId) {
        return repository.countByProjectIdAndDeletedFalse(projectId);
    }

    @Override
    public long countByProjectIdAndStatus(UUID projectId, TicketStatus status) {
        return repository.countByProjectIdAndStatusAndDeletedFalse(projectId, status.name());
    }

    private TicketEntity toEntity(Ticket domain) {
        TicketEntity entity = new TicketEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setKey(domain.getKey());
        entity.setNumber(domain.getNumber());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setType(domain.getType().name());
        entity.setStatus(domain.getStatus().name());
        entity.setPriority(domain.getPriority().name());
        entity.setAssigneeId(domain.getAssigneeId());
        entity.setReporterId(domain.getReporterId());
        entity.setEpicId(domain.getEpicId());
        entity.setParentId(domain.getParentId());
        entity.setSprintId(domain.getSprintId());
        entity.setStoryPoints(domain.getStoryPoints());
        entity.setEstimatedHours(domain.getEstimatedHours());
        entity.setLoggedHours(domain.getLoggedHours());
        entity.setDueDate(domain.getDueDate());
        entity.setEnvironment(domain.getEnvironment());
        entity.setComponent(domain.getComponent());
        entity.setLabels(domain.getLabels());
        entity.setAffectedVersion(domain.getAffectedVersion());
        entity.setFixVersion(domain.getFixVersion());
        entity.setQualityScore(domain.getQualityScore());
        return entity;
    }

    private Ticket toDomain(TicketEntity entity) {
        Ticket domain = new Ticket();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setKey(entity.getKey());
        domain.setNumber(entity.getNumber());
        domain.setTitle(entity.getTitle());
        domain.setDescription(entity.getDescription());
        domain.setType(TicketType.valueOf(entity.getType()));
        domain.setStatus(TicketStatus.valueOf(entity.getStatus()));
        domain.setPriority(TicketPriority.valueOf(entity.getPriority()));
        domain.setAssigneeId(entity.getAssigneeId());
        domain.setReporterId(entity.getReporterId());
        domain.setEpicId(entity.getEpicId());
        domain.setParentId(entity.getParentId());
        domain.setSprintId(entity.getSprintId());
        domain.setStoryPoints(entity.getStoryPoints());
        domain.setEstimatedHours(entity.getEstimatedHours());
        domain.setLoggedHours(entity.getLoggedHours());
        domain.setDueDate(entity.getDueDate());
        domain.setEnvironment(entity.getEnvironment());
        domain.setComponent(entity.getComponent());
        domain.setLabels(entity.getLabels());
        domain.setAffectedVersion(entity.getAffectedVersion());
        domain.setFixVersion(entity.getFixVersion());
        domain.setQualityScore(entity.getQualityScore());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
