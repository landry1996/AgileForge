package com.agileforge.domain.port.out;

import com.agileforge.domain.model.KeyResult;
import com.agileforge.domain.model.Objective;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OkrRepositoryPort {

    Objective saveObjective(Objective objective);

    Optional<Objective> findObjectiveById(UUID id);

    List<Objective> findObjectivesByProjectId(UUID projectId);

    void deleteObjective(UUID id);

    KeyResult saveKeyResult(KeyResult keyResult);

    Optional<KeyResult> findKeyResultById(UUID id);

    List<KeyResult> findKeyResultsByObjectiveId(UUID objectiveId);

    void deleteKeyResult(UUID id);

    void linkTicketToKeyResult(UUID keyResultId, UUID ticketId);

    void unlinkTicketFromKeyResult(UUID keyResultId, UUID ticketId);
}
