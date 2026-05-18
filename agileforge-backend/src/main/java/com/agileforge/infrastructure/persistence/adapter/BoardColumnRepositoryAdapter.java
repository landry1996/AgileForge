package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.BoardColumn;
import com.agileforge.domain.model.TicketStatus;
import com.agileforge.domain.port.out.BoardColumnRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.BoardColumnEntity;
import com.agileforge.infrastructure.persistence.repository.JpaBoardColumnRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BoardColumnRepositoryAdapter implements BoardColumnRepositoryPort {

    private final JpaBoardColumnRepository repository;

    public BoardColumnRepositoryAdapter(JpaBoardColumnRepository repository) {
        this.repository = repository;
    }

    @Override
    public BoardColumn save(BoardColumn column) {
        BoardColumnEntity entity = toEntity(column);
        BoardColumnEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<BoardColumn> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<BoardColumn> findByProjectIdOrderByPosition(UUID projectId) {
        return repository.findByProjectIdOrderByPositionAsc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private BoardColumnEntity toEntity(BoardColumn domain) {
        BoardColumnEntity entity = new BoardColumnEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setName(domain.getName());
        entity.setMappedStatus(domain.getMappedStatus().name());
        entity.setPosition(domain.getPosition());
        entity.setWipLimit(domain.getWipLimit());
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private BoardColumn toDomain(BoardColumnEntity entity) {
        BoardColumn domain = new BoardColumn();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setName(entity.getName());
        domain.setMappedStatus(TicketStatus.valueOf(entity.getMappedStatus()));
        domain.setPosition(entity.getPosition());
        domain.setWipLimit(entity.getWipLimit());
        return domain;
    }
}
