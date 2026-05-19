package com.agileforge.application.service;

import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.KeyResult;
import com.agileforge.domain.model.Objective;
import com.agileforge.domain.model.ObjectiveStatus;
import com.agileforge.domain.port.out.OkrRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OkrService {

    private static final Logger log = LoggerFactory.getLogger(OkrService.class);

    private final OkrRepositoryPort okrRepository;

    public OkrService(OkrRepositoryPort okrRepository) {
        this.okrRepository = okrRepository;
    }

    public Objective createObjective(UUID projectId, String title, String description,
                                     String period, LocalDate startDate, LocalDate endDate) {
        Objective objective = new Objective(projectId, title, description, period, startDate, endDate);
        Objective saved = okrRepository.saveObjective(objective);
        log.info("Objective created: '{}' in project {}", title, projectId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Objective> getByProject(UUID projectId) {
        List<Objective> objectives = okrRepository.findObjectivesByProjectId(projectId);
        for (Objective obj : objectives) {
            List<KeyResult> keyResults = okrRepository.findKeyResultsByObjectiveId(obj.getId());
            obj.setKeyResults(keyResults);
        }
        return objectives;
    }

    @Transactional(readOnly = true)
    public Objective getById(UUID objectiveId) {
        Objective objective = okrRepository.findObjectiveById(objectiveId)
                .orElseThrow(() -> new EntityNotFoundException("Objective", objectiveId));
        List<KeyResult> keyResults = okrRepository.findKeyResultsByObjectiveId(objectiveId);
        objective.setKeyResults(keyResults);
        return objective;
    }

    public Objective updateObjective(UUID objectiveId, String title, String description,
                                     String period, String status, LocalDate startDate, LocalDate endDate) {
        Objective objective = getById(objectiveId);

        if (title != null) objective.setTitle(title);
        if (description != null) objective.setDescription(description);
        if (period != null) objective.setPeriod(period);
        if (status != null) objective.setStatus(ObjectiveStatus.valueOf(status));
        if (startDate != null) objective.setStartDate(startDate);
        if (endDate != null) objective.setEndDate(endDate);

        Objective saved = okrRepository.saveObjective(objective);
        log.info("Objective updated: {}", objectiveId);
        return saved;
    }

    public void deleteObjective(UUID objectiveId) {
        getById(objectiveId);
        okrRepository.deleteObjective(objectiveId);
        log.info("Objective deleted: {}", objectiveId);
    }

    public KeyResult addKeyResult(UUID objectiveId, String title, double targetValue,
                                  String unit, Double startValue) {
        getById(objectiveId);
        double start = startValue != null ? startValue : 0;
        KeyResult keyResult = new KeyResult(objectiveId, title, targetValue, unit, start);
        KeyResult saved = okrRepository.saveKeyResult(keyResult);
        calculateObjectiveProgress(objectiveId);
        log.info("Key result '{}' added to objective {}", title, objectiveId);
        return saved;
    }

    public KeyResult updateKeyResultProgress(UUID keyResultId, double currentValue) {
        KeyResult keyResult = okrRepository.findKeyResultById(keyResultId)
                .orElseThrow(() -> new EntityNotFoundException("KeyResult", keyResultId));
        keyResult.setCurrentValue(currentValue);
        keyResult.setUpdatedAt(Instant.now());
        KeyResult saved = okrRepository.saveKeyResult(keyResult);
        calculateObjectiveProgress(keyResult.getObjectiveId());
        log.info("Key result {} progress updated to {}", keyResultId, currentValue);
        return saved;
    }

    public void deleteKeyResult(UUID keyResultId) {
        KeyResult keyResult = okrRepository.findKeyResultById(keyResultId)
                .orElseThrow(() -> new EntityNotFoundException("KeyResult", keyResultId));
        UUID objectiveId = keyResult.getObjectiveId();
        okrRepository.deleteKeyResult(keyResultId);
        calculateObjectiveProgress(objectiveId);
        log.info("Key result deleted: {}", keyResultId);
    }

    public void linkTicket(UUID keyResultId, UUID ticketId) {
        okrRepository.findKeyResultById(keyResultId)
                .orElseThrow(() -> new EntityNotFoundException("KeyResult", keyResultId));
        okrRepository.linkTicketToKeyResult(keyResultId, ticketId);
        log.info("Ticket {} linked to key result {}", ticketId, keyResultId);
    }

    public void unlinkTicket(UUID keyResultId, UUID ticketId) {
        okrRepository.findKeyResultById(keyResultId)
                .orElseThrow(() -> new EntityNotFoundException("KeyResult", keyResultId));
        okrRepository.unlinkTicketFromKeyResult(keyResultId, ticketId);
        log.info("Ticket {} unlinked from key result {}", ticketId, keyResultId);
    }

    public void calculateObjectiveProgress(UUID objectiveId) {
        List<KeyResult> keyResults = okrRepository.findKeyResultsByObjectiveId(objectiveId);
        if (keyResults.isEmpty()) {
            return;
        }

        int totalProgress = keyResults.stream()
                .mapToInt(KeyResult::getProgress)
                .sum();
        int averageProgress = totalProgress / keyResults.size();
        averageProgress = Math.max(0, Math.min(100, averageProgress));

        Objective objective = okrRepository.findObjectiveById(objectiveId)
                .orElseThrow(() -> new EntityNotFoundException("Objective", objectiveId));
        objective.setProgress(averageProgress);
        okrRepository.saveObjective(objective);
    }
}
