package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepositoryPort ticketRepository;
    private final TicketCommentRepositoryPort commentRepository;
    private final TicketHistoryRepositoryPort historyRepository;
    private final ProjectRepositoryPort projectRepository;

    public TicketService(TicketRepositoryPort ticketRepository,
                         TicketCommentRepositoryPort commentRepository,
                         TicketHistoryRepositoryPort historyRepository,
                         ProjectRepositoryPort projectRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.projectRepository = projectRepository;
    }

    public Ticket create(UUID projectId, String title, String description, String type,
                         String priority, UUID assigneeId, UUID epicId, UUID parentId,
                         Integer storyPoints, Double estimatedHours, String dueDate,
                         String environment, String component, String labels, UUID reporterId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));

        long nextNumber = ticketRepository.getNextNumber(projectId);
        TicketType ticketType = TicketType.valueOf(type.toUpperCase());
        TicketPriority ticketPriority = priority != null ? TicketPriority.valueOf(priority.toUpperCase()) : TicketPriority.MEDIUM;

        Ticket ticket = new Ticket(projectId, project.getKey(), nextNumber, title, ticketType, ticketPriority, reporterId);
        ticket.setDescription(description);
        ticket.setAssigneeId(assigneeId);
        ticket.setEpicId(epicId);
        ticket.setParentId(parentId);
        ticket.setStoryPoints(storyPoints);
        ticket.setEstimatedHours(estimatedHours);
        if (dueDate != null) ticket.setDueDate(LocalDate.parse(dueDate));
        ticket.setEnvironment(environment);
        ticket.setComponent(component);
        ticket.setLabels(labels);
        ticket.calculateQualityScore();

        Ticket saved = ticketRepository.save(ticket);

        historyRepository.save(new TicketHistory(saved.getId(), reporterId, "status", null, "BACKLOG"));

        log.info("Ticket created: {}-{} in project {}", project.getKey(), nextNumber, projectId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Ticket getById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", id));
    }

    @Transactional(readOnly = true)
    public Ticket getByProjectAndNumber(UUID projectId, long number) {
        return ticketRepository.findByProjectIdAndNumber(projectId, number)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found: #" + number));
    }

    @Transactional(readOnly = true)
    public List<Ticket> getByProjectId(UUID projectId) {
        return ticketRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getByProjectIdAndStatus(UUID projectId, TicketStatus status) {
        return ticketRepository.findByProjectIdAndStatus(projectId, status);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getByAssigneeId(UUID assigneeId) {
        return ticketRepository.findByAssigneeId(assigneeId);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getBySprintId(UUID sprintId) {
        return ticketRepository.findBySprintId(sprintId);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getByEpicId(UUID epicId) {
        return ticketRepository.findByEpicId(epicId);
    }

    public Ticket update(UUID id, String title, String description, String type, String status,
                         String priority, UUID assigneeId, UUID epicId, UUID parentId, UUID sprintId,
                         Integer storyPoints, Double estimatedHours, String dueDate,
                         String environment, String component, String labels,
                         String affectedVersion, String fixVersion, UUID userId) {

        Ticket ticket = getById(id);

        if (title != null && !title.equals(ticket.getTitle())) {
            recordChange(id, userId, "title", ticket.getTitle(), title);
            ticket.setTitle(title);
        }
        if (description != null) {
            ticket.setDescription(description);
        }
        if (type != null && !type.equalsIgnoreCase(ticket.getType().name())) {
            recordChange(id, userId, "type", ticket.getType().name(), type.toUpperCase());
            ticket.setType(TicketType.valueOf(type.toUpperCase()));
        }
        if (status != null && !status.equalsIgnoreCase(ticket.getStatus().name())) {
            recordChange(id, userId, "status", ticket.getStatus().name(), status.toUpperCase());
            ticket.setStatus(TicketStatus.valueOf(status.toUpperCase()));
        }
        if (priority != null && !priority.equalsIgnoreCase(ticket.getPriority().name())) {
            recordChange(id, userId, "priority", ticket.getPriority().name(), priority.toUpperCase());
            ticket.setPriority(TicketPriority.valueOf(priority.toUpperCase()));
        }
        if (!Objects.equals(assigneeId, ticket.getAssigneeId())) {
            recordChange(id, userId, "assignee",
                    ticket.getAssigneeId() != null ? ticket.getAssigneeId().toString() : null,
                    assigneeId != null ? assigneeId.toString() : null);
            ticket.setAssigneeId(assigneeId);
        }
        if (epicId != null) ticket.setEpicId(epicId);
        if (parentId != null) ticket.setParentId(parentId);
        if (sprintId != null && !Objects.equals(sprintId, ticket.getSprintId())) {
            recordChange(id, userId, "sprint",
                    ticket.getSprintId() != null ? ticket.getSprintId().toString() : null,
                    sprintId.toString());
            ticket.setSprintId(sprintId);
        }
        if (storyPoints != null) ticket.setStoryPoints(storyPoints);
        if (estimatedHours != null) ticket.setEstimatedHours(estimatedHours);
        if (dueDate != null) ticket.setDueDate(LocalDate.parse(dueDate));
        if (environment != null) ticket.setEnvironment(environment);
        if (component != null) ticket.setComponent(component);
        if (labels != null) ticket.setLabels(labels);
        if (affectedVersion != null) ticket.setAffectedVersion(affectedVersion);
        if (fixVersion != null) ticket.setFixVersion(fixVersion);

        ticket.calculateQualityScore();
        return ticketRepository.save(ticket);
    }

    public Ticket transition(UUID id, TicketStatus newStatus, UUID userId) {
        Ticket ticket = getById(id);
        String oldStatus = ticket.getStatus().name();

        if (ticket.getStatus() == newStatus) {
            throw new BusinessException("Ticket is already in status: " + newStatus);
        }

        ticket.setStatus(newStatus);
        Ticket saved = ticketRepository.save(ticket);

        recordChange(id, userId, "status", oldStatus, newStatus.name());
        log.info("Ticket {}-{} transitioned: {} -> {}", ticket.getKey(), ticket.getNumber(), oldStatus, newStatus);
        return saved;
    }

    public TicketComment addComment(UUID ticketId, UUID authorId, String content) {
        getById(ticketId); // verify ticket exists
        TicketComment comment = new TicketComment(ticketId, authorId, content);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<TicketComment> getComments(UUID ticketId) {
        return commentRepository.findByTicketId(ticketId);
    }

    public void deleteComment(UUID commentId, UUID userId) {
        TicketComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment", commentId));
        if (!comment.getAuthorId().equals(userId)) {
            throw new BusinessException("Only the author can delete their comment");
        }
        commentRepository.delete(commentId);
    }

    @Transactional(readOnly = true)
    public List<TicketHistory> getHistory(UUID ticketId) {
        return historyRepository.findByTicketId(ticketId);
    }

    public void logTime(UUID ticketId, double hours, UUID userId) {
        Ticket ticket = getById(ticketId);
        double current = ticket.getLoggedHours() != null ? ticket.getLoggedHours() : 0;
        ticket.setLoggedHours(current + hours);
        ticketRepository.save(ticket);
        recordChange(ticketId, userId, "logged_hours", String.valueOf(current), String.valueOf(current + hours));
    }

    private void recordChange(UUID ticketId, UUID userId, String field, String oldValue, String newValue) {
        historyRepository.save(new TicketHistory(ticketId, userId, field, oldValue, newValue));
    }
}
