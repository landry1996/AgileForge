package com.agileforge.domain.port.out;

import com.agileforge.domain.model.GeneratedPrompt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeneratedPromptRepositoryPort {

    GeneratedPrompt save(GeneratedPrompt prompt);

    List<GeneratedPrompt> findByTicketId(UUID ticketId);

    Optional<GeneratedPrompt> findById(UUID id);
}
