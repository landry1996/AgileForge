package com.agileforge.domain.port.out;

import com.agileforge.domain.model.Label;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LabelRepositoryPort {

    Label save(Label label);

    Optional<Label> findById(UUID id);

    List<Label> findByProjectId(UUID projectId);

    Optional<Label> findByProjectIdAndName(UUID projectId, String name);

    void delete(UUID id);

    void addLabelToTicket(UUID ticketId, UUID labelId);

    void removeLabelFromTicket(UUID ticketId, UUID labelId);

    List<Label> findLabelsByTicketId(UUID ticketId);
}
