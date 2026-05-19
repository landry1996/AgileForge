package com.agileforge.domain.port.out;

import com.agileforge.domain.model.GitRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GitRepositoryPort {

    GitRepository save(GitRepository gitRepository);

    Optional<GitRepository> findById(UUID id);

    List<GitRepository> findByProjectId(UUID projectId);

    void delete(UUID id);
}
