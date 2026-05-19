package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.Label;
import com.agileforge.domain.port.out.LabelRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.LabelEntity;
import com.agileforge.infrastructure.persistence.entity.TicketLabelEntity;
import com.agileforge.infrastructure.persistence.entity.TicketLabelId;
import com.agileforge.infrastructure.persistence.repository.JpaLabelRepository;
import com.agileforge.infrastructure.persistence.repository.JpaTicketLabelRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LabelRepositoryAdapter implements LabelRepositoryPort {

    private final JpaLabelRepository labelRepository;
    private final JpaTicketLabelRepository ticketLabelRepository;

    public LabelRepositoryAdapter(JpaLabelRepository labelRepository,
                                  JpaTicketLabelRepository ticketLabelRepository) {
        this.labelRepository = labelRepository;
        this.ticketLabelRepository = ticketLabelRepository;
    }

    @Override
    public Label save(Label label) {
        LabelEntity entity = toEntity(label);
        LabelEntity saved = labelRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Label> findById(UUID id) {
        return labelRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Label> findByProjectId(UUID projectId) {
        return labelRepository.findByProjectIdOrderByNameAsc(projectId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<Label> findByProjectIdAndName(UUID projectId, String name) {
        return labelRepository.findByProjectIdAndName(projectId, name).map(this::toDomain);
    }

    @Override
    public void delete(UUID id) {
        labelRepository.deleteById(id);
    }

    @Override
    public void addLabelToTicket(UUID ticketId, UUID labelId) {
        TicketLabelId compositeId = new TicketLabelId(ticketId, labelId);
        if (!ticketLabelRepository.existsById(compositeId)) {
            ticketLabelRepository.save(new TicketLabelEntity(ticketId, labelId));
        }
    }

    @Override
    @Transactional
    public void removeLabelFromTicket(UUID ticketId, UUID labelId) {
        ticketLabelRepository.deleteByTicketIdAndLabelId(ticketId, labelId);
    }

    @Override
    public List<Label> findLabelsByTicketId(UUID ticketId) {
        List<UUID> labelIds = ticketLabelRepository.findByTicketId(ticketId).stream()
                .map(TicketLabelEntity::getLabelId).toList();
        if (labelIds.isEmpty()) {
            return List.of();
        }
        return labelRepository.findAllById(labelIds).stream()
                .map(this::toDomain).toList();
    }

    private LabelEntity toEntity(Label domain) {
        LabelEntity entity = new LabelEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setName(domain.getName());
        entity.setColor(domain.getColor());
        entity.setDescription(domain.getDescription());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : java.time.Instant.now());
        return entity;
    }

    private Label toDomain(LabelEntity entity) {
        Label domain = new Label();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setName(entity.getName());
        domain.setColor(entity.getColor());
        domain.setDescription(entity.getDescription());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
