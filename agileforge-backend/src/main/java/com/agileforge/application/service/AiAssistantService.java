package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.model.ProjectMember;
import com.agileforge.domain.model.Release;
import com.agileforge.domain.model.Sprint;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketStatus;
import com.agileforge.domain.port.out.*;
import com.agileforge.domain.port.out.AiAssistantPort.AcceptanceCriteriaResult;
import com.agileforge.domain.port.out.AiAssistantPort.ChangelogResult;
import com.agileforge.domain.port.out.AiAssistantPort.GeneratedTicket;
import com.agileforge.domain.port.out.AiAssistantPort.QualityAnalysis;
import com.agileforge.domain.port.out.AiAssistantPort.SprintReportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    private final AiAssistantPort aiAssistant;
    private final SprintRepositoryPort sprintRepository;
    private final TicketRepositoryPort ticketRepository;
    private final ReleaseRepositoryPort releaseRepository;
    private final ProjectMemberRepositoryPort projectMemberRepository;

    public AiAssistantService(AiAssistantPort aiAssistant,
                              SprintRepositoryPort sprintRepository,
                              TicketRepositoryPort ticketRepository,
                              ReleaseRepositoryPort releaseRepository,
                              ProjectMemberRepositoryPort projectMemberRepository) {
        this.aiAssistant = aiAssistant;
        this.sprintRepository = sprintRepository;
        this.ticketRepository = ticketRepository;
        this.releaseRepository = releaseRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public List<GeneratedTicket> generateTickets(String description, String projectContext) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("Description is required to generate tickets");
        }

        log.info("Generating tickets from description ({} chars)", description.length());
        List<GeneratedTicket> tickets = aiAssistant.generateTicketsFromDescription(projectContext, description);
        log.info("Generated {} tickets", tickets.size());
        return tickets;
    }

    public List<GeneratedTicket> generateBacklog(String projectName, String projectDescription,
                                                  String projectType, Integer maxTickets) {
        if (projectDescription == null || projectDescription.isBlank()) {
            throw new BusinessException("Project description is required to generate backlog");
        }

        log.info("Generating backlog for project: {}", projectName);
        List<GeneratedTicket> tickets = aiAssistant.generateBacklog(projectName, projectDescription, projectType);

        if (maxTickets != null && maxTickets > 0 && tickets.size() > maxTickets) {
            tickets = tickets.subList(0, maxTickets);
        }

        log.info("Generated backlog with {} tickets", tickets.size());
        return tickets;
    }

    public QualityAnalysis analyzeQuality(String title, String description, String type) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("Title is required for quality analysis");
        }

        log.info("Analyzing ticket quality: {}", title);
        return aiAssistant.analyzeTicketQuality(title, description, type);
    }

    public List<GeneratedTicket> decomposeTicket(String title, String description, String type) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("Title is required to decompose ticket");
        }

        log.info("Decomposing ticket: {}", title);
        List<GeneratedTicket> subtasks = aiAssistant.decomposeTicket(title, description, type);
        log.info("Decomposed into {} subtasks", subtasks.size());
        return subtasks;
    }

    public String suggestDescription(String title, String type, String projectContext) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("Title is required to suggest description");
        }

        log.info("Suggesting description for: {}", title);
        return aiAssistant.suggestDescription(title, type, projectContext);
    }

    public AcceptanceCriteriaResult generateAcceptanceCriteria(String ticketTitle, String ticketDescription, String ticketType) {
        if (ticketTitle == null || ticketTitle.isBlank()) {
            throw new BusinessException("Ticket title is required to generate acceptance criteria");
        }

        log.info("Generating acceptance criteria for: {}", ticketTitle);
        return aiAssistant.generateAcceptanceCriteria(ticketTitle, ticketDescription, ticketType);
    }

    public SprintReportResult generateSprintReport(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new BusinessException("Sprint not found: " + sprintId));

        List<Ticket> sprintTickets = ticketRepository.findBySprintId(sprintId);

        List<String> completedTickets = sprintTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.DONE)
                .map(t -> t.getKey() + " - " + t.getTitle())
                .toList();

        List<String> inProgressTickets = sprintTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS || t.getStatus() == TicketStatus.CODE_REVIEW)
                .map(t -> t.getKey() + " - " + t.getTitle())
                .toList();

        List<String> blockedTickets = sprintTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BLOCKED)
                .map(t -> t.getKey() + " - " + t.getTitle())
                .toList();

        int totalPoints = sprintTickets.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        int completedPoints = sprintTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.DONE)
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        log.info("Generating sprint report for: {}", sprint.getName());
        return aiAssistant.generateSprintReport(
                sprint.getName(), sprint.getGoal(),
                completedTickets, inProgressTickets, blockedTickets,
                totalPoints, completedPoints
        );
    }

    public ChangelogResult generateChangelog(UUID releaseId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new BusinessException("Release not found: " + releaseId));

        List<UUID> ticketIds = releaseRepository.findTicketIdsByReleaseId(releaseId);
        List<String> ticketSummaries = new ArrayList<>();

        for (UUID ticketId : ticketIds) {
            ticketRepository.findById(ticketId).ifPresent(ticket ->
                    ticketSummaries.add(String.format("[%s] %s - %s",
                            ticket.getType() != null ? ticket.getType().name() : "TASK",
                            ticket.getTitle(),
                            ticket.getDescription() != null ? ticket.getDescription() : ""))
            );
        }

        log.info("Generating changelog for release: {}", release.getName());
        return aiAssistant.generateChangelog(release.getName(), release.getVersion(), ticketSummaries);
    }

    public String suggestAssignee(UUID ticketId, UUID projectId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("Ticket not found: " + ticketId));

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        List<String> memberNames = members.stream()
                .map((ProjectMember m) -> m.getUserId().toString())
                .collect(java.util.stream.Collectors.toList());

        log.info("Suggesting assignee for ticket: {}", ticket.getTitle());
        return aiAssistant.suggestAssignee(
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getType() != null ? ticket.getType().name() : null,
                memberNames
        );
    }
}
