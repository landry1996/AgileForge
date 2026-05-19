package com.agileforge.domain.port.out;

import com.agileforge.domain.model.GitPipeline;

import java.util.List;
import java.util.UUID;

public interface GitPipelinePort {

    GitPipeline save(GitPipeline gitPipeline);

    List<GitPipeline> findByTicketId(UUID ticketId);

    List<GitPipeline> findByPrId(UUID prId);
}
