package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Label;
import com.agileforge.domain.port.out.LabelRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LabelService {

    private static final Logger log = LoggerFactory.getLogger(LabelService.class);

    private final LabelRepositoryPort labelRepository;

    public LabelService(LabelRepositoryPort labelRepository) {
        this.labelRepository = labelRepository;
    }

    public Label createLabel(UUID projectId, String name, String color, String description) {
        labelRepository.findByProjectIdAndName(projectId, name).ifPresent(existing -> {
            throw new BusinessException("Label with name '" + name + "' already exists in this project");
        });

        Label label = new Label(projectId, name, color, description);
        Label saved = labelRepository.save(label);
        log.info("Label created: '{}' in project {}", name, projectId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Label> getProjectLabels(UUID projectId) {
        return labelRepository.findByProjectId(projectId);
    }

    public Label updateLabel(UUID labelId, String name, String color, String description) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new EntityNotFoundException("Label", labelId));

        if (name != null && !name.equals(label.getName())) {
            labelRepository.findByProjectIdAndName(label.getProjectId(), name).ifPresent(existing -> {
                throw new BusinessException("Label with name '" + name + "' already exists in this project");
            });
            label.setName(name);
        }
        if (color != null) {
            label.setColor(color);
        }
        if (description != null) {
            label.setDescription(description);
        }

        Label saved = labelRepository.save(label);
        log.info("Label updated: {}", labelId);
        return saved;
    }

    public void deleteLabel(UUID labelId) {
        labelRepository.findById(labelId)
                .orElseThrow(() -> new EntityNotFoundException("Label", labelId));
        labelRepository.delete(labelId);
        log.info("Label deleted: {}", labelId);
    }

    public void addLabelToTicket(UUID ticketId, UUID labelId) {
        labelRepository.findById(labelId)
                .orElseThrow(() -> new EntityNotFoundException("Label", labelId));
        labelRepository.addLabelToTicket(ticketId, labelId);
        log.info("Label {} added to ticket {}", labelId, ticketId);
    }

    public void removeLabelFromTicket(UUID ticketId, UUID labelId) {
        labelRepository.removeLabelFromTicket(ticketId, labelId);
        log.info("Label {} removed from ticket {}", labelId, ticketId);
    }

    @Transactional(readOnly = true)
    public List<Label> getTicketLabels(UUID ticketId) {
        return labelRepository.findLabelsByTicketId(ticketId);
    }
}
