package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.StatusCategory;
import com.agileforge.domain.model.Workflow;
import com.agileforge.domain.model.WorkflowStatus;
import com.agileforge.domain.model.WorkflowTransition;
import com.agileforge.domain.port.out.WorkflowRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.WorkflowEntity;
import com.agileforge.infrastructure.persistence.entity.WorkflowStatusEntity;
import com.agileforge.infrastructure.persistence.entity.WorkflowTransitionEntity;
import com.agileforge.infrastructure.persistence.repository.JpaWorkflowRepository;
import com.agileforge.infrastructure.persistence.repository.JpaWorkflowStatusRepository;
import com.agileforge.infrastructure.persistence.repository.JpaWorkflowTransitionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WorkflowRepositoryAdapter implements WorkflowRepositoryPort {

    private final JpaWorkflowRepository workflowRepository;
    private final JpaWorkflowStatusRepository statusRepository;
    private final JpaWorkflowTransitionRepository transitionRepository;

    public WorkflowRepositoryAdapter(JpaWorkflowRepository workflowRepository,
                                     JpaWorkflowStatusRepository statusRepository,
                                     JpaWorkflowTransitionRepository transitionRepository) {
        this.workflowRepository = workflowRepository;
        this.statusRepository = statusRepository;
        this.transitionRepository = transitionRepository;
    }

    @Override
    @Transactional
    public Workflow save(Workflow workflow) {
        WorkflowEntity entity = toEntity(workflow);
        WorkflowEntity saved = workflowRepository.save(entity);

        // Save statuses
        if (workflow.getStatuses() != null && !workflow.getStatuses().isEmpty()) {
            statusRepository.deleteByWorkflowId(saved.getId());
            for (WorkflowStatus status : workflow.getStatuses()) {
                WorkflowStatusEntity statusEntity = toStatusEntity(status, saved.getId());
                statusRepository.save(statusEntity);
            }
        }

        // Save transitions
        if (workflow.getTransitions() != null && !workflow.getTransitions().isEmpty()) {
            transitionRepository.deleteByWorkflowId(saved.getId());
            for (WorkflowTransition transition : workflow.getTransitions()) {
                WorkflowTransitionEntity transitionEntity = toTransitionEntity(transition, saved.getId());
                transitionRepository.save(transitionEntity);
            }
        }

        return findById(saved.getId()).orElse(toDomain(saved));
    }

    @Override
    public Optional<Workflow> findById(UUID id) {
        return workflowRepository.findByIdAndDeletedFalse(id).map(entity -> {
            Workflow workflow = toDomain(entity);
            workflow.setStatuses(loadStatuses(entity.getId()));
            workflow.setTransitions(loadTransitions(entity.getId()));
            return workflow;
        });
    }

    @Override
    public List<Workflow> findByProjectId(UUID projectId) {
        return workflowRepository.findByProjectIdAndDeletedFalse(projectId).stream()
                .map(entity -> {
                    Workflow workflow = toDomain(entity);
                    workflow.setStatuses(loadStatuses(entity.getId()));
                    workflow.setTransitions(loadTransitions(entity.getId()));
                    return workflow;
                }).toList();
    }

    @Override
    public Optional<Workflow> findByProjectIdAndTicketType(UUID projectId, String ticketType) {
        return workflowRepository.findByProjectIdAndTicketTypeAndDeletedFalse(projectId, ticketType)
                .map(entity -> {
                    Workflow workflow = toDomain(entity);
                    workflow.setStatuses(loadStatuses(entity.getId()));
                    workflow.setTransitions(loadTransitions(entity.getId()));
                    return workflow;
                });
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        workflowRepository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            workflowRepository.save(entity);
        });
    }

    private List<WorkflowStatus> loadStatuses(UUID workflowId) {
        return statusRepository.findByWorkflowIdOrderByPositionAsc(workflowId).stream()
                .map(this::toStatusDomain).toList();
    }

    private List<WorkflowTransition> loadTransitions(UUID workflowId) {
        return transitionRepository.findByWorkflowId(workflowId).stream()
                .map(this::toTransitionDomain).toList();
    }

    private WorkflowEntity toEntity(Workflow domain) {
        WorkflowEntity entity = new WorkflowEntity();
        entity.setId(domain.getId());
        entity.setProjectId(domain.getProjectId());
        entity.setName(domain.getName());
        entity.setTicketType(domain.getTicketType());
        entity.setDefault(domain.isDefault());
        return entity;
    }

    private Workflow toDomain(WorkflowEntity entity) {
        Workflow domain = new Workflow();
        domain.setId(entity.getId());
        domain.setProjectId(entity.getProjectId());
        domain.setName(entity.getName());
        domain.setTicketType(entity.getTicketType());
        domain.setDefault(entity.isDefault());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    private WorkflowStatusEntity toStatusEntity(WorkflowStatus domain, UUID workflowId) {
        WorkflowStatusEntity entity = new WorkflowStatusEntity();
        entity.setId(domain.getId());
        entity.setWorkflowId(workflowId);
        entity.setName(domain.getName());
        entity.setCategory(domain.getCategory() != null ? domain.getCategory().name() : "IN_PROGRESS");
        entity.setPosition(domain.getPosition());
        entity.setColor(domain.getColor());
        return entity;
    }

    private WorkflowStatus toStatusDomain(WorkflowStatusEntity entity) {
        WorkflowStatus domain = new WorkflowStatus();
        domain.setId(entity.getId());
        domain.setWorkflowId(entity.getWorkflowId());
        domain.setName(entity.getName());
        domain.setCategory(StatusCategory.valueOf(entity.getCategory()));
        domain.setPosition(entity.getPosition());
        domain.setColor(entity.getColor());
        return domain;
    }

    private WorkflowTransitionEntity toTransitionEntity(WorkflowTransition domain, UUID workflowId) {
        WorkflowTransitionEntity entity = new WorkflowTransitionEntity();
        entity.setId(domain.getId());
        entity.setWorkflowId(workflowId);
        entity.setFromStatus(domain.getFromStatus());
        entity.setToStatus(domain.getToStatus());
        return entity;
    }

    private WorkflowTransition toTransitionDomain(WorkflowTransitionEntity entity) {
        WorkflowTransition domain = new WorkflowTransition();
        domain.setId(entity.getId());
        domain.setWorkflowId(entity.getWorkflowId());
        domain.setFromStatus(entity.getFromStatus());
        domain.setToStatus(entity.getToStatus());
        return domain;
    }
}
