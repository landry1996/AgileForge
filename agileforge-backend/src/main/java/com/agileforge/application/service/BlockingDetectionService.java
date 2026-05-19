package com.agileforge.application.service;

import com.agileforge.application.dto.response.BlockedTicketAlert;
import com.agileforge.application.dto.response.RiskAlert;
import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BlockingDetectionService {

    private static final Logger log = LoggerFactory.getLogger(BlockingDetectionService.class);

    private static final int STALE_DAYS_THRESHOLD = 7;
    private static final int OVERLOADED_THRESHOLD = 3;

    private final TicketRepositoryPort ticketRepository;
    private final SprintRepositoryPort sprintRepository;
    private final TicketHistoryRepositoryPort ticketHistoryRepository;
    private final TicketLinkRepositoryPort ticketLinkRepository;
    private final ReleaseRepositoryPort releaseRepository;
    private final ProjectMemberRepositoryPort projectMemberRepository;

    public BlockingDetectionService(TicketRepositoryPort ticketRepository,
                                    SprintRepositoryPort sprintRepository,
                                    TicketHistoryRepositoryPort ticketHistoryRepository,
                                    TicketLinkRepositoryPort ticketLinkRepository,
                                    ReleaseRepositoryPort releaseRepository,
                                    ProjectMemberRepositoryPort projectMemberRepository) {
        this.ticketRepository = ticketRepository;
        this.sprintRepository = sprintRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketLinkRepository = ticketLinkRepository;
        this.releaseRepository = releaseRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public List<BlockedTicketAlert> detectBlockedTickets(UUID projectId) {
        List<Ticket> tickets = ticketRepository.findByProjectId(projectId);
        List<BlockedTicketAlert> alerts = new ArrayList<>();

        // Calculate average cycle time per status for the project
        Map<TicketStatus, Double> avgTimeByStatus = calculateAverageTimeByStatus(tickets);

        for (Ticket ticket : tickets) {
            if (ticket.isDone()) continue;

            // Check: ticket in same status too long (> 2x average)
            int daysInStatus = calculateDaysInCurrentStatus(ticket);
            Double avgForStatus = avgTimeByStatus.get(ticket.getStatus());
            if (avgForStatus != null && avgForStatus > 0 && daysInStatus > avgForStatus * 2) {
                alerts.add(new BlockedTicketAlert(
                        ticket.getId(),
                        ticket.getFullKey(),
                        ticket.getTitle(),
                        String.format("In %s for %d days (avg: %.0f days, threshold: %.0f days)",
                                ticket.getStatus(), daysInStatus, avgForStatus, avgForStatus * 2),
                        ticket.getStatus().name(),
                        daysInStatus,
                        "Review the ticket and unblock or reassign if needed"
                ));
                continue; // Avoid duplicate alerts for same ticket
            }

            // Check: unresolved dependencies (blocked by tickets not DONE)
            List<TicketLink> links = ticketLinkRepository.findByTargetTicketId(ticket.getId());
            List<TicketLink> unresolvedBlockers = links.stream()
                    .filter(l -> l.getLinkType() == TicketLinkType.BLOCKS)
                    .filter(l -> {
                        Optional<Ticket> blocker = ticketRepository.findById(l.getSourceTicketId());
                        return blocker.isPresent() && !blocker.get().isDone();
                    })
                    .toList();

            if (!unresolvedBlockers.isEmpty()) {
                alerts.add(new BlockedTicketAlert(
                        ticket.getId(),
                        ticket.getFullKey(),
                        ticket.getTitle(),
                        unresolvedBlockers.size() + " unresolved blocking dependency(ies)",
                        ticket.getStatus().name(),
                        daysInStatus,
                        "Resolve blocking tickets first or remove the dependency"
                ));
                continue;
            }

            // Check: IN_PROGRESS with no assignee
            if (ticket.getStatus() == TicketStatus.IN_PROGRESS && ticket.getAssigneeId() == null) {
                alerts.add(new BlockedTicketAlert(
                        ticket.getId(),
                        ticket.getFullKey(),
                        ticket.getTitle(),
                        "Ticket is IN_PROGRESS but has no assignee",
                        ticket.getStatus().name(),
                        daysInStatus,
                        "Assign this ticket to a team member"
                ));
                continue;
            }

            // Check: past due date
            if (ticket.getDueDate() != null && LocalDate.now().isAfter(ticket.getDueDate())) {
                int overdueDays = (int) ChronoUnit.DAYS.between(ticket.getDueDate(), LocalDate.now());
                alerts.add(new BlockedTicketAlert(
                        ticket.getId(),
                        ticket.getFullKey(),
                        ticket.getTitle(),
                        "Ticket is " + overdueDays + " days past due date",
                        ticket.getStatus().name(),
                        daysInStatus,
                        "Update due date or prioritize completion"
                ));
                continue;
            }

            // Check: exceeding estimated hours
            if (ticket.getEstimatedHours() != null && ticket.getLoggedHours() != null
                    && ticket.getLoggedHours() > ticket.getEstimatedHours()) {
                double excess = ticket.getLoggedHours() - ticket.getEstimatedHours();
                alerts.add(new BlockedTicketAlert(
                        ticket.getId(),
                        ticket.getFullKey(),
                        ticket.getTitle(),
                        String.format("Logged hours (%.1f) exceed estimate (%.1f) by %.1f hours",
                                ticket.getLoggedHours(), ticket.getEstimatedHours(), excess),
                        ticket.getStatus().name(),
                        daysInStatus,
                        "Re-estimate the remaining work or split the ticket"
                ));
            }
        }

        return alerts;
    }

    public List<RiskAlert> getProjectRisks(UUID projectId) {
        List<RiskAlert> risks = new ArrayList<>();
        List<Ticket> allTickets = ticketRepository.findByProjectId(projectId);

        // Risk: Sprint at risk
        Optional<Sprint> activeSprint = sprintRepository.findActiveByProjectId(projectId);
        if (activeSprint.isPresent()) {
            Sprint sprint = activeSprint.get();
            RiskAlert sprintRisk = evaluateSprintRisk(sprint, allTickets);
            if (sprintRisk != null) {
                risks.add(sprintRisk);
            }
        }

        // Risk: Release at risk
        List<Release> activeReleases = releaseRepository.findByProjectIdAndStatus(projectId, ReleaseStatus.IN_PROGRESS);
        activeReleases.addAll(releaseRepository.findByProjectIdAndStatus(projectId, ReleaseStatus.PLANNING));
        for (Release release : activeReleases) {
            RiskAlert releaseRisk = evaluateReleaseRisk(release);
            if (releaseRisk != null) {
                risks.add(releaseRisk);
            }
        }

        // Risk: Overloaded team members (>3 IN_PROGRESS tickets)
        Map<UUID, List<Ticket>> ticketsByAssignee = allTickets.stream()
                .filter(t -> t.getAssigneeId() != null)
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                .collect(Collectors.groupingBy(Ticket::getAssigneeId));

        for (Map.Entry<UUID, List<Ticket>> entry : ticketsByAssignee.entrySet()) {
            if (entry.getValue().size() > OVERLOADED_THRESHOLD) {
                risks.add(new RiskAlert(
                        "TEAM_OVERLOAD",
                        "HIGH",
                        "Overloaded team member",
                        "Team member has " + entry.getValue().size() + " tickets in progress simultaneously",
                        "Redistribute work or limit work in progress",
                        entry.getKey()
                ));
            }
        }

        // Risk: Stale tickets (no activity in 7+ days while in progress)
        List<TicketStatus> activeStatuses = List.of(
                TicketStatus.IN_PROGRESS, TicketStatus.CODE_REVIEW, TicketStatus.QA, TicketStatus.UAT
        );
        List<Ticket> activeTickets = allTickets.stream()
                .filter(t -> activeStatuses.contains(t.getStatus()))
                .toList();

        for (Ticket ticket : activeTickets) {
            Instant lastActivity = getLastActivityDate(ticket);
            if (lastActivity != null) {
                long daysSinceActivity = ChronoUnit.DAYS.between(
                        lastActivity.atZone(ZoneId.systemDefault()).toLocalDate(),
                        LocalDate.now()
                );
                if (daysSinceActivity >= STALE_DAYS_THRESHOLD) {
                    risks.add(new RiskAlert(
                            "STALE_TICKET",
                            "MEDIUM",
                            "Stale ticket: " + ticket.getFullKey(),
                            "No activity for " + daysSinceActivity + " days while in " + ticket.getStatus(),
                            "Check with assignee or reassign the ticket",
                            ticket.getId()
                    ));
                }
            }
        }

        return risks;
    }

    // --- Private helper methods ---

    private Map<TicketStatus, Double> calculateAverageTimeByStatus(List<Ticket> tickets) {
        Map<TicketStatus, List<Long>> durationsMap = new EnumMap<>(TicketStatus.class);

        for (Ticket ticket : tickets) {
            if (!ticket.isDone()) continue;
            List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticket.getId());
            List<TicketHistory> statusChanges = history.stream()
                    .filter(h -> "status".equals(h.getField()))
                    .sorted(Comparator.comparing(TicketHistory::getCreatedAt))
                    .toList();

            for (int i = 0; i < statusChanges.size(); i++) {
                TicketHistory entry = statusChanges.get(i);
                String statusName = entry.getNewValue();
                try {
                    TicketStatus status = TicketStatus.valueOf(statusName);
                    if (i + 1 < statusChanges.size()) {
                        TicketHistory next = statusChanges.get(i + 1);
                        long days = ChronoUnit.DAYS.between(
                                entry.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                                next.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()
                        );
                        if (days >= 0) {
                            durationsMap.computeIfAbsent(status, k -> new ArrayList<>()).add(days);
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                    // Skip unknown status values
                }
            }
        }

        Map<TicketStatus, Double> averages = new EnumMap<>(TicketStatus.class);
        for (Map.Entry<TicketStatus, List<Long>> entry : durationsMap.entrySet()) {
            double avg = entry.getValue().stream().mapToLong(Long::longValue).average().orElse(3.0);
            averages.put(entry.getKey(), avg);
        }
        return averages;
    }

    private int calculateDaysInCurrentStatus(Ticket ticket) {
        List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticket.getId());
        Optional<TicketHistory> lastStatusChange = history.stream()
                .filter(h -> "status".equals(h.getField()))
                .filter(h -> ticket.getStatus().name().equals(h.getNewValue()))
                .max(Comparator.comparing(TicketHistory::getCreatedAt));

        if (lastStatusChange.isPresent()) {
            return (int) ChronoUnit.DAYS.between(
                    lastStatusChange.get().getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                    LocalDate.now()
            );
        }

        if (ticket.getUpdatedAt() != null) {
            return (int) ChronoUnit.DAYS.between(
                    ticket.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                    LocalDate.now()
            );
        }

        return 0;
    }

    private RiskAlert evaluateSprintRisk(Sprint sprint, List<Ticket> allTickets) {
        if (sprint.getEndDate() == null) return null;

        int daysRemaining = (int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), sprint.getEndDate()));
        long totalDays = sprint.getDurationDays();
        if (totalDays == 0) return null;

        List<Ticket> sprintTickets = allTickets.stream()
                .filter(t -> sprint.getId().equals(t.getSprintId()))
                .toList();

        int totalPoints = sprintTickets.stream()
                .map(Ticket::getStoryPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int completedPoints = sprintTickets.stream()
                .filter(Ticket::isDone)
                .map(Ticket::getStoryPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int remainingPoints = totalPoints - completedPoints;
        double timeElapsedRatio = 1.0 - ((double) daysRemaining / totalDays);
        double completionRatio = totalPoints > 0 ? (double) completedPoints / totalPoints : 1.0;

        // Sprint is at risk if time elapsed ratio significantly exceeds completion ratio
        if (timeElapsedRatio > 0.5 && completionRatio < timeElapsedRatio * 0.6) {
            String severity = completionRatio < timeElapsedRatio * 0.3 ? "CRITICAL" : "HIGH";
            return new RiskAlert(
                    "SPRINT_AT_RISK",
                    severity,
                    "Sprint '" + sprint.getName() + "' at risk",
                    String.format("%d points remaining with %d days left (%.0f%% time elapsed, %.0f%% completed)",
                            remainingPoints, daysRemaining, timeElapsedRatio * 100, completionRatio * 100),
                    "Consider reducing scope or addressing blockers immediately",
                    sprint.getId()
            );
        }

        return null;
    }

    private RiskAlert evaluateReleaseRisk(Release release) {
        if (release.getReleaseDate() == null) return null;

        int daysUntilRelease = (int) ChronoUnit.DAYS.between(LocalDate.now(), release.getReleaseDate());
        if (daysUntilRelease > 14) return null; // Only flag if within 2 weeks

        List<UUID> ticketIds = releaseRepository.findTicketIdsByReleaseId(release.getId());
        if (ticketIds.isEmpty()) return null;

        long incompleteCount = ticketIds.stream()
                .map(ticketRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(t -> !t.isDone())
                .count();

        double completionRatio = 1.0 - ((double) incompleteCount / ticketIds.size());

        if (incompleteCount > 0 && daysUntilRelease <= 7 && completionRatio < 0.8) {
            return new RiskAlert(
                    "RELEASE_AT_RISK",
                    "CRITICAL",
                    "Release '" + release.getName() + "' at risk",
                    String.format("%d incomplete tickets with %d days until release date (%.0f%% complete)",
                            incompleteCount, daysUntilRelease, completionRatio * 100),
                    "Prioritize remaining tickets or consider postponing the release",
                    release.getId()
            );
        } else if (incompleteCount > 0 && daysUntilRelease <= 14 && completionRatio < 0.6) {
            return new RiskAlert(
                    "RELEASE_AT_RISK",
                    "HIGH",
                    "Release '" + release.getName() + "' at risk",
                    String.format("%d incomplete tickets with %d days until release date (%.0f%% complete)",
                            incompleteCount, daysUntilRelease, completionRatio * 100),
                    "Review scope and ensure critical items are prioritized",
                    release.getId()
            );
        }

        return null;
    }

    private Instant getLastActivityDate(Ticket ticket) {
        List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticket.getId());
        Optional<Instant> lastHistoryDate = history.stream()
                .map(TicketHistory::getCreatedAt)
                .max(Comparator.naturalOrder());

        if (lastHistoryDate.isPresent()) {
            return lastHistoryDate.get();
        }

        return ticket.getUpdatedAt();
    }
}
