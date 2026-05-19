package com.agileforge.application.service;

import com.agileforge.application.dto.response.*;
import com.agileforge.domain.exception.EntityNotFoundException;
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
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);

    private final TicketRepositoryPort ticketRepository;
    private final SprintRepositoryPort sprintRepository;
    private final TicketHistoryRepositoryPort ticketHistoryRepository;
    private final TicketLinkRepositoryPort ticketLinkRepository;

    public PredictionService(TicketRepositoryPort ticketRepository,
                             SprintRepositoryPort sprintRepository,
                             TicketHistoryRepositoryPort ticketHistoryRepository,
                             TicketLinkRepositoryPort ticketLinkRepository) {
        this.ticketRepository = ticketRepository;
        this.sprintRepository = sprintRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.ticketLinkRepository = ticketLinkRepository;
    }

    public SprintPredictionResponse predictSprintCompletion(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint", sprintId));

        List<Ticket> tickets = ticketRepository.findBySprintId(sprintId);

        int totalPoints = tickets.stream()
                .map(Ticket::getStoryPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int completedPoints = tickets.stream()
                .filter(Ticket::isDone)
                .map(Ticket::getStoryPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int remainingPoints = totalPoints - completedPoints;

        // Calculate days remaining
        LocalDate today = LocalDate.now();
        LocalDate endDate = sprint.getEndDate();
        int daysRemaining = endDate != null ? (int) Math.max(0, ChronoUnit.DAYS.between(today, endDate)) : 0;

        // Calculate current velocity per day based on this sprint's progress
        LocalDate startDate = sprint.getStartDate();
        long elapsedDays = startDate != null ? Math.max(1, ChronoUnit.DAYS.between(startDate, today)) : 1;
        double currentVelocityPerDay = (double) completedPoints / elapsedDays;

        // Calculate historical sprint completion rate
        List<Sprint> completedSprints = sprintRepository.findCompletedByProjectId(sprint.getProjectId());
        double historicalCompletionRate = calculateHistoricalCompletionRate(completedSprints);

        // Count blocked tickets
        long blockedCount = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BLOCKED)
                .count();

        // Calculate average cycle time for this project's tickets
        double avgCycleTime = calculateAverageCycleTimeForTickets(tickets);

        // Compute completion probability
        List<String> riskFactors = new ArrayList<>();
        int probability = computeCompletionProbability(
                remainingPoints, daysRemaining, currentVelocityPerDay,
                blockedCount, tickets.size(), historicalCompletionRate,
                avgCycleTime, riskFactors
        );

        String recommendation = generateRecommendation(probability, riskFactors, remainingPoints, daysRemaining);

        return new SprintPredictionResponse(
                sprintId,
                sprint.getName(),
                probability,
                remainingPoints,
                daysRemaining,
                Math.round(currentVelocityPerDay * 100.0) / 100.0,
                riskFactors,
                recommendation
        );
    }

    public TicketDelayPredictionResponse predictTicketDelay(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        List<String> reasons = new ArrayList<>();
        int delayDays = 0;

        // Analyze time in current status vs average
        List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticketId);
        int daysInCurrentStatus = calculateDaysInCurrentStatus(ticket, history);
        double avgTimeInStatus = calculateAverageTimeInStatus(ticket.getStatus(), ticket.getProjectId());

        if (avgTimeInStatus > 0 && daysInCurrentStatus > avgTimeInStatus * 1.5) {
            int excess = (int) (daysInCurrentStatus - avgTimeInStatus);
            delayDays += excess;
            reasons.add(String.format("Ticket has been in %s for %d days (avg: %.0f days)",
                    ticket.getStatus(), daysInCurrentStatus, avgTimeInStatus));
        }

        // Check ticket complexity
        Integer storyPoints = ticket.getStoryPoints();
        if (storyPoints != null && storyPoints >= 8) {
            delayDays += 2;
            reasons.add("High complexity ticket (" + storyPoints + " story points)");
        }

        // Check assignee workload
        if (ticket.getAssigneeId() != null) {
            List<Ticket> assigneeTickets = ticketRepository.findByAssigneeId(ticket.getAssigneeId());
            long inProgressCount = assigneeTickets.stream()
                    .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                    .count();
            if (inProgressCount > 3) {
                delayDays += 1;
                reasons.add("Assignee is overloaded with " + inProgressCount + " tickets in progress");
            }
        }

        // Check unresolved dependencies (tickets that block this one)
        List<TicketLink> links = ticketLinkRepository.findByTargetTicketId(ticketId);
        long unresolvedBlockers = links.stream()
                .filter(l -> l.getLinkType() == TicketLinkType.BLOCKS)
                .map(l -> ticketRepository.findById(l.getSourceTicketId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(t -> !t.isDone())
                .count();

        if (unresolvedBlockers > 0) {
            delayDays += (int) (unresolvedBlockers * 2);
            reasons.add(unresolvedBlockers + " unresolved blocking ticket(s)");
        }

        // Check if past due date
        if (ticket.getDueDate() != null && LocalDate.now().isAfter(ticket.getDueDate())) {
            int overdue = (int) ChronoUnit.DAYS.between(ticket.getDueDate(), LocalDate.now());
            delayDays = Math.max(delayDays, overdue);
            reasons.add("Ticket is " + overdue + " days past due date");
        }

        // Determine risk level
        boolean atRisk = !reasons.isEmpty();
        String riskLevel = determineRiskLevel(delayDays, reasons.size());

        return new TicketDelayPredictionResponse(
                ticketId,
                ticket.getFullKey(),
                atRisk,
                delayDays,
                riskLevel,
                reasons
        );
    }

    public ScopeCreepResponse detectScopeCreep(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint", sprintId));

        List<Ticket> currentTickets = ticketRepository.findBySprintId(sprintId);
        LocalDate sprintStart = sprint.getStartDate();

        if (sprintStart == null) {
            return new ScopeCreepResponse(sprintId, 0, currentTickets.size(), 0, 0, 0.0, "Sprint has no start date");
        }

        Instant sprintStartInstant = sprintStart.atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Analyze ticket history to determine which tickets were added after sprint start
        int addedCount = 0;
        int removedCount = 0;

        for (Ticket ticket : currentTickets) {
            // Check if the ticket was moved to this sprint after sprint start
            List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticket.getId());
            Optional<TicketHistory> sprintAssignment = history.stream()
                    .filter(h -> "sprintId".equals(h.getField()))
                    .filter(h -> sprintId.toString().equals(h.getNewValue()))
                    .findFirst();

            if (sprintAssignment.isPresent() && sprintAssignment.get().getCreatedAt().isAfter(sprintStartInstant)) {
                addedCount++;
            }
        }

        // Check for tickets that were removed from sprint (look for history where sprintId changed away)
        // We scan all tickets in the project that have a history of being in this sprint but are no longer
        List<Ticket> projectTickets = ticketRepository.findByProjectId(sprint.getProjectId());
        for (Ticket ticket : projectTickets) {
            if (ticket.getSprintId() != null && ticket.getSprintId().equals(sprintId)) {
                continue; // Still in this sprint
            }
            List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticket.getId());
            boolean wasInSprint = history.stream()
                    .filter(h -> "sprintId".equals(h.getField()))
                    .anyMatch(h -> sprintId.toString().equals(h.getOldValue())
                            && h.getCreatedAt().isAfter(sprintStartInstant));
            if (wasInSprint) {
                removedCount++;
            }
        }

        int currentScope = currentTickets.size();
        int originalScope = currentScope - addedCount + removedCount;
        double scopeChangePercent = originalScope > 0
                ? ((double) (currentScope - originalScope) / originalScope) * 100.0
                : 0.0;

        String warning = generateScopeCreepWarning(scopeChangePercent);

        return new ScopeCreepResponse(
                sprintId,
                originalScope,
                currentScope,
                addedCount,
                removedCount,
                Math.round(scopeChangePercent * 10.0) / 10.0,
                warning
        );
    }

    public SprintCapacitySuggestionResponse suggestSprintCapacity(UUID projectId) {
        List<Sprint> completedSprints = sprintRepository.findCompletedByProjectId(projectId);

        // Take the last 5 completed sprints
        List<Sprint> recentSprints = completedSprints.stream()
                .limit(5)
                .toList();

        List<Integer> historicalVelocities = new ArrayList<>();
        for (Sprint sprint : recentSprints) {
            List<Ticket> sprintTickets = ticketRepository.findBySprintId(sprint.getId());
            int completedPoints = sprintTickets.stream()
                    .filter(Ticket::isDone)
                    .map(Ticket::getStoryPoints)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();
            historicalVelocities.add(completedPoints);
        }

        if (historicalVelocities.isEmpty()) {
            return new SprintCapacitySuggestionResponse(
                    projectId, 0, 0, historicalVelocities,
                    "No completed sprints found. Cannot suggest capacity."
            );
        }

        // Calculate average velocity
        double average = historicalVelocities.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        // Calculate standard deviation for confidence
        double variance = historicalVelocities.stream()
                .mapToDouble(v -> Math.pow(v - average, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // Confidence: less variance = higher confidence (max 95%)
        int confidence;
        if (average == 0) {
            confidence = 0;
        } else {
            double coefficientOfVariation = stdDev / average;
            confidence = (int) Math.max(20, Math.min(95, 95 - (coefficientOfVariation * 100)));
        }

        // Suggested points: use average minus a small buffer (10%) for safety
        int suggestedPoints = (int) Math.round(average * 0.9);

        String reasoning = String.format(
                "Based on %d completed sprints with average velocity of %.1f points (std dev: %.1f). " +
                        "Suggested capacity includes a 10%% buffer for unexpected work.",
                historicalVelocities.size(), average, stdDev
        );

        // Reverse to show chronological order
        Collections.reverse(historicalVelocities);

        return new SprintCapacitySuggestionResponse(
                projectId,
                suggestedPoints,
                confidence,
                historicalVelocities,
                reasoning
        );
    }

    // --- Private helper methods ---

    private double calculateHistoricalCompletionRate(List<Sprint> completedSprints) {
        if (completedSprints.isEmpty()) {
            return 0.7; // Default assumption: 70%
        }

        int fullyCompleted = 0;
        for (Sprint sprint : completedSprints) {
            List<Ticket> tickets = ticketRepository.findBySprintId(sprint.getId());
            long totalWithPoints = tickets.stream()
                    .filter(t -> t.getStoryPoints() != null && t.getStoryPoints() > 0)
                    .count();
            long doneWithPoints = tickets.stream()
                    .filter(Ticket::isDone)
                    .filter(t -> t.getStoryPoints() != null && t.getStoryPoints() > 0)
                    .count();
            if (totalWithPoints > 0 && doneWithPoints >= totalWithPoints * 0.9) {
                fullyCompleted++;
            }
        }

        return (double) fullyCompleted / completedSprints.size();
    }

    private double calculateAverageCycleTimeForTickets(List<Ticket> tickets) {
        List<Long> cycleTimes = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (!ticket.isDone()) continue;
            List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticket.getId());
            Optional<TicketHistory> inProgress = history.stream()
                    .filter(h -> "status".equals(h.getField()))
                    .filter(h -> "IN_PROGRESS".equals(h.getNewValue()))
                    .findFirst();
            Optional<TicketHistory> done = history.stream()
                    .filter(h -> "status".equals(h.getField()))
                    .filter(h -> "DONE".equals(h.getNewValue()))
                    .findFirst();
            if (inProgress.isPresent() && done.isPresent()) {
                long days = ChronoUnit.DAYS.between(
                        inProgress.get().getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        done.get().getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()
                );
                if (days >= 0) cycleTimes.add(days);
            }
        }
        return cycleTimes.stream().mapToLong(Long::longValue).average().orElse(3.0);
    }

    private int computeCompletionProbability(int remainingPoints, int daysRemaining,
                                             double velocityPerDay, long blockedCount,
                                             int totalTickets, double historicalCompletionRate,
                                             double avgCycleTime, List<String> riskFactors) {
        if (remainingPoints == 0) return 100;
        if (daysRemaining == 0 && remainingPoints > 0) {
            riskFactors.add("Sprint end date reached with remaining work");
            return 5;
        }

        // Factor 1: Can we deliver remaining points at current velocity?
        double projectedCompletion = velocityPerDay * daysRemaining;
        double velocityRatio = projectedCompletion / remainingPoints;
        int velocityScore = (int) Math.min(100, velocityRatio * 100);

        if (velocityRatio < 1.0) {
            riskFactors.add(String.format("Current velocity (%.1f pts/day) insufficient to complete %d remaining points in %d days",
                    velocityPerDay, remainingPoints, daysRemaining));
        }

        // Factor 2: Blocked tickets
        double blockedRatio = totalTickets > 0 ? (double) blockedCount / totalTickets : 0;
        int blockedPenalty = 0;
        if (blockedCount > 0) {
            blockedPenalty = (int) (blockedRatio * 30);
            riskFactors.add(blockedCount + " ticket(s) currently blocked");
        }

        // Factor 3: Historical completion rate
        int historicalScore = (int) (historicalCompletionRate * 100);
        if (historicalCompletionRate < 0.6) {
            riskFactors.add("Historical sprint completion rate is low (" + (int) (historicalCompletionRate * 100) + "%)");
        }

        // Factor 4: Average cycle time vs days remaining
        if (avgCycleTime > daysRemaining && remainingPoints > 0) {
            riskFactors.add(String.format("Average cycle time (%.1f days) exceeds remaining days (%d)",
                    avgCycleTime, daysRemaining));
        }

        // Weighted average
        int probability = (int) ((velocityScore * 0.4) + (historicalScore * 0.3) + ((100 - blockedPenalty) * 0.3));
        return Math.max(0, Math.min(100, probability));
    }

    private String generateRecommendation(int probability, List<String> riskFactors, int remainingPoints, int daysRemaining) {
        if (probability >= 80) {
            return "Sprint is on track. Continue with current pace.";
        } else if (probability >= 60) {
            return "Sprint completion is at moderate risk. Consider removing low-priority items or addressing blocked tickets.";
        } else if (probability >= 40) {
            return "Sprint is at significant risk. Recommend reducing scope by " +
                    (int) (remainingPoints * 0.3) + " points or extending timeline.";
        } else {
            return "Sprint is unlikely to complete on time. Immediate scope reduction and blocker resolution needed.";
        }
    }

    private int calculateDaysInCurrentStatus(Ticket ticket, List<TicketHistory> history) {
        // Find the most recent status change to current status
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

        // Fallback: use ticket updatedAt
        if (ticket.getUpdatedAt() != null) {
            return (int) ChronoUnit.DAYS.between(
                    ticket.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                    LocalDate.now()
            );
        }

        return 0;
    }

    private double calculateAverageTimeInStatus(TicketStatus status, UUID projectId) {
        List<Ticket> projectTickets = ticketRepository.findByProjectId(projectId);
        List<Long> durations = new ArrayList<>();

        for (Ticket ticket : projectTickets) {
            if (!ticket.isDone()) continue;
            List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticket.getId());

            // Find transitions into and out of this status
            List<TicketHistory> statusChanges = history.stream()
                    .filter(h -> "status".equals(h.getField()))
                    .sorted(Comparator.comparing(TicketHistory::getCreatedAt))
                    .toList();

            for (int i = 0; i < statusChanges.size(); i++) {
                TicketHistory entry = statusChanges.get(i);
                if (status.name().equals(entry.getNewValue()) && i + 1 < statusChanges.size()) {
                    TicketHistory exit = statusChanges.get(i + 1);
                    long days = ChronoUnit.DAYS.between(
                            entry.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                            exit.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()
                    );
                    if (days >= 0) durations.add(days);
                }
            }
        }

        return durations.stream().mapToLong(Long::longValue).average().orElse(3.0);
    }

    private String determineRiskLevel(int delayDays, int reasonCount) {
        if (delayDays == 0 && reasonCount == 0) return "LOW";
        if (delayDays <= 2 && reasonCount <= 1) return "LOW";
        if (delayDays <= 5 && reasonCount <= 2) return "MEDIUM";
        if (delayDays <= 10) return "HIGH";
        return "CRITICAL";
    }

    private String generateScopeCreepWarning(double scopeChangePercent) {
        if (scopeChangePercent <= 5) {
            return "Scope is stable. No significant changes detected.";
        } else if (scopeChangePercent <= 15) {
            return "Minor scope creep detected. Monitor closely.";
        } else if (scopeChangePercent <= 30) {
            return "Significant scope creep detected. Consider removing low-priority items to maintain delivery goals.";
        } else {
            return "Critical scope creep! Sprint scope has grown by " + (int) scopeChangePercent +
                    "%. Immediate action required to re-scope or adjust timeline.";
        }
    }
}
