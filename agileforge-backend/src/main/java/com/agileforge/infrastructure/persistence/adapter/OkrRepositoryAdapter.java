package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.KeyResult;
import com.agileforge.domain.model.Objective;
import com.agileforge.domain.model.ObjectiveStatus;
import com.agileforge.domain.port.out.OkrRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.KeyResultEntity;
import com.agileforge.infrastructure.persistence.entity.KeyResultTicketEntity;
import com.agileforge.infrastructure.persistence.entity.KeyResultTicketId;
import com.agileforge.infrastructure.persistence.entity.ObjectiveEntity;
import com.agileforge.infrastructure.persistence.repository.JpaKeyResultRepository;
import com.agileforge.infrastructure.persistence.repository.JpaKeyResultTicketRepository;
import com.agileforge.infrastructure.persistence.repository.JpaObjectiveRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OkrRepositoryAdapter implements OkrRepositoryPort {

    private final JpaObjectiveRepository objectiveRepository;
    private final JpaKeyResultRepository keyResultRepository;
    private final JpaKeyResultTicketRepository keyResultTicketRepository;

    public OkrRepositoryAdapter(JpaObjectiveRepository objectiveRepository,
                                JpaKeyResultRepository keyResultRepository,
                                JpaKeyResultTicketRepository keyResultTicketRepository) {
        this.objectiveRepository = objectiveRepository;
        this.keyResultRepository = keyResultRepository;
        this.keyResultTicketRepository = keyResultTicketRepository;
    }

    @Override
    public Objective saveObjective(Objective objective) {
        ObjectiveEntity entity = toObjectiveEntity(objective);
        ObjectiveEntity saved = objectiveRepository.save(entity);
        return toObjectiveDomain(saved);
    }

    @Override
    public Optional<Objective> findObjectiveById(UUID id) {
        return objectiveRepository.findByIdAndDeletedFalse(id).map(this::toObjectiveDomain);
    }

    @Override
    public List<Objective> findObjectivesByProjectId(UUID projectId) {
        return objectiveRepository.findByProjectIdAndDeletedFalseOrderByCreatedAtDesc(projectId).stream()
                .map(this::toObjectiveDomain).toList();
    }

    @Override
    public void deleteObjective(UUID id) {
        objectiveRepository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            objectiveRepository.save(entity);
        });
    }

    @Override
    public KeyResult saveKeyResult(KeyResult keyResult) {
        KeyResultEntity entity = toKeyResultEntity(keyResult);
        KeyResultEntity saved = keyResultRepository.save(entity);
        return toKeyResultDomain(saved);
    }

    @Override
    public Optional<KeyResult> findKeyResultById(UUID id) {
        return keyResultRepository.findById(id).map(this::toKeyResultDomain);
    }

    @Override
    public List<KeyResult> findKeyResultsByObjectiveId(UUID objectiveId) {
        return keyResultRepository.findByObjectiveIdOrderByCreatedAtAsc(objectiveId).stream()
                .map(this::toKeyResultDomain).toList();
    }

    @Override
    public void deleteKeyResult(UUID id) {
        keyResultRepository.deleteById(id);
    }

    @Override
    public void linkTicketToKeyResult(UUID keyResultId, UUID ticketId) {
        KeyResultTicketId id = new KeyResultTicketId(keyResultId, ticketId);
        if (!keyResultTicketRepository.existsById(id)) {
            keyResultTicketRepository.save(new KeyResultTicketEntity(id));
        }
    }

    @Override
    public void unlinkTicketFromKeyResult(UUID keyResultId, UUID ticketId) {
        KeyResultTicketId id = new KeyResultTicketId(keyResultId, ticketId);
        keyResultTicketRepository.deleteById(id);
    }

    private ObjectiveEntity toObjectiveEntity(Objective domain) {
        ObjectiveEntity entity = new ObjectiveEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setPeriod(domain.getPeriod());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setStatus(domain.getStatus().name());
        entity.setProgress(domain.getProgress());
        if (domain.getCreatedBy() != null) {
            entity.setCreatedBy(domain.getCreatedBy().toString());
        }
        return entity;
    }

    private Objective toObjectiveDomain(ObjectiveEntity entity) {
        Objective domain = new Objective();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setTitle(entity.getTitle());
        domain.setDescription(entity.getDescription());
        domain.setPeriod(entity.getPeriod());
        domain.setStartDate(entity.getStartDate());
        domain.setEndDate(entity.getEndDate());
        domain.setStatus(ObjectiveStatus.valueOf(entity.getStatus()));
        domain.setProgress(entity.getProgress());
        if (entity.getCreatedBy() != null) {
            domain.setCreatedBy(UUID.fromString(entity.getCreatedBy()));
        }
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    private KeyResultEntity toKeyResultEntity(KeyResult domain) {
        KeyResultEntity entity = new KeyResultEntity();
        entity.setId(domain.getId());
        entity.setObjectiveId(domain.getObjectiveId());
        entity.setTitle(domain.getTitle());
        entity.setTargetValue(domain.getTargetValue());
        entity.setCurrentValue(domain.getCurrentValue());
        entity.setUnit(domain.getUnit());
        entity.setStartValue(domain.getStartValue());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private KeyResult toKeyResultDomain(KeyResultEntity entity) {
        KeyResult domain = new KeyResult();
        domain.setId(entity.getId());
        domain.setObjectiveId(entity.getObjectiveId());
        domain.setTitle(entity.getTitle());
        domain.setTargetValue(entity.getTargetValue());
        domain.setCurrentValue(entity.getCurrentValue());
        domain.setUnit(entity.getUnit());
        domain.setStartValue(entity.getStartValue());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
