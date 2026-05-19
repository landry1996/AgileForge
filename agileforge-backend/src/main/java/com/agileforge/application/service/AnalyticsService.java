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
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final TicketRepositoryPort ticketRepository;
    private final SprintRepositoryPort sprintRepository;
    private final ProjectRepositoryPort projectRepository;
    private final UserRepositoryPort userRepository;
    private final ProjectMemberRepositoryPort projectMemberRepository;
    private final TicketHistoryRepositoryPort ticketHistoryRepository;

    public AnalyticsService(TicketRepositoryPort ticketRepository,
                            SprintRepositoryPort sprintRepository,
                            ProjectRepositoryPort projectRepository,
                            UserRepositoryPort userRepository,
                            ProjectMemberRepositoryPort projectMemberRepository,
                            TicketHistoryRepositoryPort ticketHistoryRepository) {
        this.ticketRepository = ticketRepository;
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
    }

    public SprintMetricsResponse getSprintMetrics(UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint", sprintId));

        List<Ticket> tickets = ticketRepository.findBySprintId(sprintId);

        int totalTickets = tickets.size();
        int completedTickets = (int) tickets.stream().filter(Ticket::isDone).count();
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

        List<BurndownDataPoint> burndownData = calculateBurndown(sprint, tickets);
        List<BurnupDataPoint> burnupData = calculateBurnup(sprint, tickets);

        return new SprintMetricsResponse(
                sprintId,
                sprint.getName(),
                totalTickets,
                completedTickets,
                totalPoints,
                completedPoints,
                burndownData,
                burnupData
        );
    }

    public ProjectAnalyticsResponse getProjectAnalytics(UUID projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));

        List<Ticket> allTickets = ticketRepository.findByProjectId(projectId);

        long totalTickets = allTickets.size();
        long closedTickets = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.DONE || t.getStatus() == TicketStatus.CANCELLED)
                .count();
        long openTickets = totalTickets - closedTickets;

        // Velocity history over last 5 sprints
        List<VelocityDataPoint> velocityHistory = getVelocityHistory(projectId, 5);
        double averageVelocity = velocityHistory.stream()
                .mapToInt(VelocityDataPoint::completedPoints)
                .average()
                .orElse(0.0);

        // Average cycle time: time from IN_PROGRESS to DONE
        double averageCycleTimeDays = calculateAverageCycleTime(allTickets);

        // Ticket distribution maps
        Map<String, Long> ticketsByType = allTickets.stream()
                .filter(t -> t.getType() != null)
                .collect(Collectors.groupingBy(t -> t.getType().name(), Collectors.counting()));

        Map<String, Long> ticketsByPriority = allTickets.stream()
                .filter(t -> t.getPriority() != null)
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        Map<String, Long> ticketsByStatus = allTickets.stream()
                .filter(t -> t.getStatus() != null)
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        return new ProjectAnalyticsResponse(
                projectId,
                totalTickets,
                openTickets,
                closedTickets,
                averageVelocity,
                averageCycleTimeDays,
                velocityHistory,
                ticketsByType,
                ticketsByPriority,
                ticketsByStatus
        );
    }

    public List<VelocityDataPoint> getVelocityHistory(UUID projectId, int lastN) {
        List<Sprint> completedSprints = sprintRepository.findCompletedByProjectId(projectId);

        // Take only the last N sprints (already ordered by endDate desc)
        List<Sprint> recentSprints = completedSprints.stream()
                .limit(lastN)
                .toList();

        // Reverse to get chronological order
        List<Sprint> chronological = new ArrayList<>(recentSprints);
        Collections.reverse(chronological);

        List<VelocityDataPoint> velocityData = new ArrayList<>();
        int sprintNumber = 1;

        for (Sprint sprint : chronological) {
            List<Ticket> sprintTickets = ticketRepository.findBySprintId(sprint.getId());

            int committedPoints = sprintTickets.stream()
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

            velocityData.add(new VelocityDataPoint(
                    sprint.getName(),
                    committedPoints,
                    completedPoints,
                    sprintNumber++
            ));
        }

        return velocityData;
    }

    public List<TeamWorkloadResponse> getTeamWorkload(UUID projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        List<Ticket> allProjectTickets = ticketRepository.findByProjectId(projectId);

        // Find active sprint for this project
        Optional<Sprint> activeSprint = sprintRepository.findActiveByProjectId(projectId);
        UUID activeSprintId = activeSprint.map(Sprint::getId).orElse(null);

        List<TeamWorkloadResponse> workload = new ArrayList<>();

        for (ProjectMember member : members) {
            UUID userId = member.getUserId();
            String userName = userRepository.findById(userId)
                    .map(User::getDisplayName)
                    .orElse("Unknown");

            // Tickets assigned to this user in this project
            List<Ticket> userTickets = allProjectTickets.stream()
                    .filter(t -> userId.equals(t.getAssigneeId()))
                    .toList();

            int assignedTickets = (int) userTickets.stream()
                    .filter(t -> !t.isDone())
                    .count();

            int inProgressTickets = (int) userTickets.stream()
                    .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                    .count();

            int completedThisSprint = 0;
            if (activeSprintId != null) {
                completedThisSprint = (int) userTickets.stream()
                        .filter(t -> activeSprintId.equals(t.getSprintId()))
                        .filter(Ticket::isDone)
                        .count();
            }

            int totalPointsAssigned = userTickets.stream()
                    .filter(t -> !t.isDone())
                    .map(Ticket::getStoryPoints)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            workload.add(new TeamWorkloadResponse(
                    userId,
                    userName,
                    assignedTickets,
                    inProgressTickets,
                    completedThisSprint,
                    totalPointsAssigned
            ));
        }

        return workload;
    }

    private List<BurndownDataPoint> calculateBurndown(Sprint sprint, List<Ticket> tickets) {
        LocalDate startDate = sprint.getStartDate();
        LocalDate endDate = sprint.getEndDate();

        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }

        int totalPoints = tickets.stream()
                .map(Ticket::getStoryPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (totalDays <= 0) {
            return Collections.emptyList();
        }

        // Build a map of date -> points completed on that date using ticket history
        Map<LocalDate, Integer> pointsCompletedByDate = buildPointsCompletedByDate(tickets);

        List<BurndownDataPoint> burndownData = new ArrayList<>();
        int remainingPoints = totalPoints;
        LocalDate today = LocalDate.now();
        LocalDate effectiveEnd = endDate.isBefore(today) ? endDate : today;

        for (LocalDate date = startDate; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
            long dayIndex = ChronoUnit.DAYS.between(startDate, date);
            double idealPoints = totalPoints - (totalPoints * (double) dayIndex / totalDays);

            Integer completedOnDate = pointsCompletedByDate.get(date);
            if (completedOnDate != null) {
                remainingPoints -= completedOnDate;
            }

            burndownData.add(new BurndownDataPoint(date, Math.max(remainingPoints, 0), idealPoints));
        }

        return burndownData;
    }

    private List<BurnupDataPoint> calculateBurnup(Sprint sprint, List<Ticket> tickets) {
        LocalDate startDate = sprint.getStartDate();
        LocalDate endDate = sprint.getEndDate();

        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }

        int totalScope = tickets.stream()
                .map(Ticket::getStoryPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        // Build a map of date -> points completed on that date
        Map<LocalDate, Integer> pointsCompletedByDate = buildPointsCompletedByDate(tickets);

        List<BurnupDataPoint> burnupData = new ArrayList<>();
        int cumulativeCompleted = 0;
        LocalDate today = LocalDate.now();
        LocalDate effectiveEnd = endDate.isBefore(today) ? endDate : today;

        for (LocalDate date = startDate; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
            Integer completedOnDate = pointsCompletedByDate.get(date);
            if (completedOnDate != null) {
                cumulativeCompleted += completedOnDate;
            }

            burnupData.add(new BurnupDataPoint(date, totalScope, cumulativeCompleted));
        }

        return burnupData;
    }

    private Map<LocalDate, Integer> buildPointsCompletedByDate(List<Ticket> tickets) {
        Map<LocalDate, Integer> pointsCompletedByDate = new HashMap<>();

        for (Ticket ticket : tickets) {
            if (!ticket.isDone()) {
                continue;
            }

            int points = ticket.getStoryPoints() != null ? ticket.getStoryPoints() : 0;
            if (points == 0) {
                continue;
            }

            // Try to find when the ticket was moved to DONE via history
            List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticket.getId());
            Optional<TicketHistory> doneEntry = history.stream()
                    .filter(h -> "status".equals(h.getField()))
                    .filter(h -> "DONE".equals(h.getNewValue()) || "CANCELLED".equals(h.getNewValue()))
                    .findFirst();

            LocalDate completedDate;
            if (doneEntry.isPresent()) {
                completedDate = doneEntry.get().getCreatedAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } else {
                // Fallback: use updatedAt from the ticket
                Instant updatedAt = ticket.getUpdatedAt();
                completedDate = updatedAt != null
                        ? updatedAt.atZone(ZoneId.systemDefault()).toLocalDate()
                        : LocalDate.now();
            }

            pointsCompletedByDate.merge(completedDate, points, Integer::sum);
        }

        return pointsCompletedByDate;
    }

    private double calculateAverageCycleTime(List<Ticket> tickets) {
        List<Long> cycleTimes = new ArrayList<>();

        for (Ticket ticket : tickets) {
            if (!ticket.isDone()) {
                continue;
            }

            List<TicketHistory> history = ticketHistoryRepository.findByTicketId(ticket.getId());

            // Find when the ticket moved to IN_PROGRESS
            Optional<TicketHistory> inProgressEntry = history.stream()
                    .filter(h -> "status".equals(h.getField()))
                    .filter(h -> "IN_PROGRESS".equals(h.getNewValue()))
                    .reduce((first, second) -> first); // Get the earliest (last in desc-ordered list is earliest, but we want first occurrence)

            // Find when the ticket moved to DONE
            Optional<TicketHistory> doneEntry = history.stream()
                    .filter(h -> "status".equals(h.getField()))
                    .filter(h -> "DONE".equals(h.getNewValue()) || "CANCELLED".equals(h.getNewValue()))
                    .findFirst();

            if (inProgressEntry.isPresent() && doneEntry.isPresent()) {
                long days = ChronoUnit.DAYS.between(
                        inProgressEntry.get().getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        doneEntry.get().getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()
                );
                if (days >= 0) {
                    cycleTimes.add(days);
                }
            }
        }

        return cycleTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }
}
