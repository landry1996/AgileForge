package com.agileforge.domain.port.out;

import com.agileforge.domain.model.BoardColumn;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardColumnRepositoryPort {

    BoardColumn save(BoardColumn column);

    Optional<BoardColumn> findById(UUID id);

    List<BoardColumn> findByProjectIdOrderByPosition(UUID projectId);

    void delete(UUID id);
}
