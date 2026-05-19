package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreateWorkflowRequest;
import com.agileforge.application.dto.request.WorkflowStatusDto;
import com.agileforge.application.dto.request.WorkflowTransitionDto;
import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.StatusCategory;
import com.agileforge.domain.model.Workflow;
import com.agileforge.domain.model.WorkflowStatus;
import com.agileforge.domain.model.WorkflowTransition;
import com.agileforge.domain.port.out.WorkflowRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private final WorkflowRepositoryPort workflowRepository;

    public WorkflowService(WorkflowRepositoryPort workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public Workflow createWorkflow(UUID projectId, CreateWorkflowRequest request) {
        // Check if workflow already exists for this project and ticket type
        workflowRepository.findByProjectIdAndTicketType(projectId, request.ticketType())
                .ifPresent(existing -> {
                    throw new BusinessException("Workflow already exists for ticket type: " + request.ticketType());
                });

        Workflow workflow = new Workflow(projectId, request.name(), request.ticketType());

        // Map statuses
        if (request.statuses() != null && !request.statuses().isEmpty()) {
            List<WorkflowStatus> statuses = request.statuses().stream()
                    .map(this::toWorkflowStatus)
                    .toList();
            workflow.setStatuses(statuses);
        }

        // Map transitions
        if (request.transitions() != null && !request.transitions().isEmpty()) {
            List<WorkflowTransition> transitions = request.transitions().stream()
                    .map(this::toWorkflowTransition)
                    .toList();
            workflow.setTransitions(transitions);
        }

        Workflow saved = workflowRepository.save(workflow);
        log.info("Workflow created: {} for project {} and ticket type {}", request.name(), projectId, request.ticketType());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Workflow> getWorkflowsByProject(UUID projectId) {
        return workflowRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public Optional<Workflow> getWorkflowForTicketType(UUID projectId, String ticketType) {
        return workflowRepository.findByProjectIdAndTicketType(projectId, ticketType);
    }

    public Workflow updateWorkflow(UUID workflowId, CreateWorkflowRequest request) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow", workflowId));

        workflow.setName(request.name());
        workflow.setTicketType(request.ticketType());

        // Update statuses
        if (request.statuses() != null) {
            List<WorkflowStatus> statuses = request.statuses().stream()
                    .map(this::toWorkflowStatus)
                    .toList();
            workflow.setStatuses(statuses);
        }

        // Update transitions
        if (request.transitions() != null) {
            List<WorkflowTransition> transitions = request.transitions().stream()
                    .map(this::toWorkflowTransition)
                    .toList();
            workflow.setTransitions(transitions);
        }

        Workflow saved = workflowRepository.save(workflow);
        log.info("Workflow updated: {}", workflowId);
        return saved;
    }

    public void deleteWorkflow(UUID workflowId) {
        workflowRepository.findById(workflowId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow", workflowId));
        workflowRepository.delete(workflowId);
        log.info("Workflow deleted (soft): {}", workflowId);
    }

    @Transactional(readOnly = true)
    public boolean validateTransition(UUID projectId, String ticketType, String fromStatus, String toStatus) {
        Optional<Workflow> workflowOpt = workflowRepository.findByProjectIdAndTicketType(projectId, ticketType);

        if (workflowOpt.isEmpty()) {
            // No workflow defined - allow all transitions
            return true;
        }

        Workflow workflow = workflowOpt.get();
        return workflow.getTransitions().stream()
                .anyMatch(t -> t.getFromStatus().equals(fromStatus) && t.getToStatus().equals(toStatus));
    }

    @Transactional(readOnly = true)
    public boolean validateTransitionByWorkflowId(UUID workflowId, String fromStatus, String toStatus) {
        Optional<Workflow> workflowOpt = workflowRepository.findById(workflowId);

        if (workflowOpt.isEmpty()) {
            throw new EntityNotFoundException("Workflow", workflowId);
        }

        Workflow workflow = workflowOpt.get();
        return workflow.getTransitions().stream()
                .anyMatch(t -> t.getFromStatus().equals(fromStatus) && t.getToStatus().equals(toStatus));
    }

    @Transactional(readOnly = true)
    public Workflow getDefaultWorkflow(String ticketType) {
        Workflow workflow = new Workflow();
        workflow.setName("Default " + ticketType + " Workflow");
        workflow.setTicketType(ticketType);
        workflow.setDefault(true);

        List<WorkflowStatus> statuses = new ArrayList<>();
        statuses.add(new WorkflowStatus(null, "TODO", StatusCategory.TODO, 0, "#E2E8F0"));
        statuses.add(new WorkflowStatus(null, "IN_PROGRESS", StatusCategory.IN_PROGRESS, 1, "#3B82F6"));
        statuses.add(new WorkflowStatus(null, "IN_REVIEW", StatusCategory.IN_PROGRESS, 2, "#F59E0B"));
        statuses.add(new WorkflowStatus(null, "DONE", StatusCategory.DONE, 3, "#10B981"));
        workflow.setStatuses(statuses);

        List<WorkflowTransition> transitions = new ArrayList<>();
        transitions.add(new WorkflowTransition(null, "TODO", "IN_PROGRESS"));
        transitions.add(new WorkflowTransition(null, "IN_PROGRESS", "IN_REVIEW"));
        transitions.add(new WorkflowTransition(null, "IN_REVIEW", "DONE"));
        transitions.add(new WorkflowTransition(null, "IN_REVIEW", "IN_PROGRESS"));
        transitions.add(new WorkflowTransition(null, "IN_PROGRESS", "TODO"));
        workflow.setTransitions(transitions);

        return workflow;
    }

    private WorkflowStatus toWorkflowStatus(WorkflowStatusDto dto) {
        WorkflowStatus status = new WorkflowStatus();
        status.setName(dto.name());
        status.setCategory(dto.category() != null ? StatusCategory.valueOf(dto.category()) : StatusCategory.IN_PROGRESS);
        status.setPosition(dto.position());
        status.setColor(dto.color());
        return status;
    }

    private WorkflowTransition toWorkflowTransition(WorkflowTransitionDto dto) {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setFromStatus(dto.fromStatus());
        transition.setToStatus(dto.toStatus());
        return transition;
    }
}
