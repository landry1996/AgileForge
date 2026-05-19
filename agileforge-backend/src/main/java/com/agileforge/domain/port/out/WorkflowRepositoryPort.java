package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowRepositoryPort {

    Workflow save(Workflow workflow);

    Optional<Workflow> findById(UUID id);

    List<Workflow> findByProjectId(UUID projectId);

    Optional<Workflow> findByProjectIdAndTicketType(UUID projectId, String ticketType);

    void delete(UUID id);
}
