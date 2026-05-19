package com.agileforge.application.service;

import com.agileforge.application.dto.response.ProjectHealthResponse;
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
public class ProjectHealthService {

    private static final Logger log = LoggerFactory.getLogger(ProjectHealthService.class);

    private final TicketRepositoryPort ticketRepository;
    private final SprintRepositoryPort sprintRepository;
    private final ProjectRepositoryPort projectRepository;
    private final ProjectMemberRepositoryPort projectMemberRepository;
    private final DocumentRepositoryPort documentRepository;
    private final TicketHistoryRepositoryPort ticketHistoryRepository;

    public ProjectHealthService(TicketRepositoryPort ticketRepository,
                                SprintRepositoryPort sprintRepository,
                                ProjectRepositoryPort projectRepository,
                                ProjectMemberRepositoryPort projectMemberRepository,
                                DocumentRepositoryPort documentRepository,
                                TicketHistoryRepositoryPort ticketHistoryRepository) {
        this.ticketRepository = ticketRepository;
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.documentRepository = documentRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
    }

    public ProjectHealthResponse calculateHealthScore(UUID projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));

        List<Ticket> allTickets = ticketRepository.findByProjectId(projectId);
        List<Sprint> completedSprints = sprintRepository.findCompletedByProjectId(projectId);

        // Calculate sub-scores
        int velocityScore = calculateVelocityScore(completedSprints);
        int qualityScore = calculateQualityScore(allTickets);
        int deliveryScore = calculateDeliveryScore(completedSprints);
        int debtScore = calculateDebtScore(allTickets);
        int documentationScore = calculateDocumentationScore(projectId, allTickets);
        int teamLoadScore = calculateTeamLoadScore(projectId, allTickets);

        // Build breakdown
        Map<String, Integer> breakdown = new LinkedHashMap<>();
        breakdown.put("velocity", velocityScore);
        breakdown.put("quality", qualityScore);
        breakdown.put("delivery", deliveryScore);
        breakdown.put("debt", debtScore);
        breakdown.put("documentation", documentationScore);
        breakdown.put("teamLoad", teamLoadScore);

        // Weighted average (velocity and delivery weighted more)
        int overallScore = (int) Math.round(
                velocityScore * 0.20 +
                qualityScore * 0.15 +
                deliveryScore * 0.25 +
                debtScore * 0.15 +
                documentationScore * 0.10 +
                teamLoadScore * 0.15
        );

        // Calculate trend
        String trend = calculateTrend(projectId, allTickets, completedSprints);

        // Generate recommendations
        List<String> recommendations = generateRecommendations(breakdown);

        return new ProjectHealthResponse(
                projectId,
                overallScore,
                breakdown,
                trend,
                recommendations
        );
    }

    // --- Sub-score calculations ---

    private int calculateVelocityScore(List<Sprint> completedSprints) {
        if (completedSprints.size() < 3) {
            return 50; // Not enough data, neutral score
        }

        // Get last 3 sprints' velocities
        List<Integer> velocities = completedSprints.stream()
                .limit(3)
                .map(sprint -> {
                    List<Ticket> tickets = ticketRepository.findBySprintId(sprint.getId());
                    return tickets.stream()
                            .filter(Ticket::isDone)
                            .map(Ticket::getStoryPoints)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .sum();
                })
                .toList();

        if (velocities.isEmpty() || velocities.stream().allMatch(v -> v == 0)) {
            return 30;
        }

        double average = velocities.stream().mapToInt(Integer::intValue).average().orElse(0);
        if (average == 0) return 30;

        // Calculate coefficient of variation (less variance = better)
        double variance = velocities.stream()
                .mapToDouble(v -> Math.pow(v - average, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        double cv = stdDev / average;

        // CV of 0 = perfect (100), CV > 0.5 = poor (30)
        int score = (int) Math.max(30, Math.min(100, 100 - (cv * 140)));
        return score;
    }

    private int calculateQualityScore(List<Ticket> allTickets) {
        if (allTickets.isEmpty()) return 50;

        // Average quality score across all tickets
        double avgQuality = allTickets.stream()
                .mapToInt(Ticket::getQualityScore)
                .average()
                .orElse(0);

        // Quality score in tickets is 0-65 max (based on calculateQualityScore in Ticket)
        // Normalize to 0-100
        int normalized = (int) Math.min(100, (avgQuality / 65.0) * 100);
        return Math.max(0, normalized);
    }

    private int calculateDeliveryScore(List<Sprint> completedSprints) {
        if (completedSprints.isEmpty()) return 50;

        int onTimeCount = 0;
        int total = Math.min(completedSprints.size(), 5); // Consider last 5 sprints

        for (int i = 0; i < total; i++) {
            Sprint sprint = completedSprints.get(i);
            List<Ticket> tickets = ticketRepository.findBySprintId(sprint.getId());

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

            // Consider sprint completed on time if >=80% of points were delivered
            if (totalPoints == 0 || (double) completedPoints / totalPoints >= 0.8) {
                onTimeCount++;
            }
        }

        return (int) ((double) onTimeCount / total * 100);
    }

    private int calculateDebtScore(List<Ticket> allTickets) {
        if (allTickets.isEmpty()) return 80;

        long techDebtCount = allTickets.stream()
                .filter(t -> !t.isDone())
                .filter(t -> t.getType() == TicketType.TECHNICAL_DEBT)
                .count();

        long featureCount = allTickets.stream()
                .filter(t -> !t.isDone())
                .filter(t -> t.getType() == TicketType.STORY || t.getType() == TicketType.FEATURE_REQUEST
                        || t.getType() == TicketType.TASK)
                .count();

        long totalActive = techDebtCount + featureCount;
        if (totalActive == 0) return 80;

        // Ratio of tech debt to total active work (inverse: less debt = higher score)
        double debtRatio = (double) techDebtCount / totalActive;

        // 0% debt = 100, 50% debt = 0
        int score = (int) Math.max(0, Math.min(100, 100 - (debtRatio * 200)));
        return score;
    }

    private int calculateDocumentationScore(UUID projectId, List<Ticket> allTickets) {
        // Get all epics
        List<Ticket> epics = allTickets.stream()
                .filter(t -> t.getType() == TicketType.EPIC)
                .toList();

        if (epics.isEmpty()) return 60; // No epics, neutral

        // Check how many documents exist in the project
        List<Document> documents = documentRepository.findByProjectId(projectId);
        if (documents.isEmpty()) return 20;

        // Simple heuristic: at least 1 document per epic = good
        double docPerEpicRatio = (double) documents.size() / epics.size();
        int score = (int) Math.min(100, docPerEpicRatio * 100);
        return Math.max(0, score);
    }

    private int calculateTeamLoadScore(UUID projectId, List<Ticket> allTickets) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        if (members.isEmpty()) return 50;

        // Calculate workload per member (active tickets)
        Map<UUID, Long> ticketsPerMember = allTickets.stream()
                .filter(t -> !t.isDone() && t.getAssigneeId() != null)
                .collect(Collectors.groupingBy(Ticket::getAssigneeId, Collectors.counting()));

        if (ticketsPerMember.isEmpty()) return 70;

        // Calculate balance: use coefficient of variation
        double average = ticketsPerMember.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        if (average == 0) return 70;

        double variance = ticketsPerMember.values().stream()
                .mapToDouble(v -> Math.pow(v - average, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        double cv = stdDev / average;

        // Check for members with no tickets (also imbalanced)
        long membersWithNoTickets = members.stream()
                .filter(m -> !ticketsPerMember.containsKey(m.getUserId()))
                .count();
        double idleRatio = (double) membersWithNoTickets / members.size();

        // CV of 0 = perfect balance (100), CV > 1 = very imbalanced (20)
        int balanceScore = (int) Math.max(20, Math.min(100, 100 - (cv * 80)));
        int idlePenalty = (int) (idleRatio * 30);

        return Math.max(0, balanceScore - idlePenalty);
    }

    // --- Trend calculation ---

    private String calculateTrend(UUID projectId, List<Ticket> allTickets, List<Sprint> completedSprints) {
        // Compare recent activity vs 30 days ago
        // Simple heuristic: compare velocity of last sprint vs the one before
        if (completedSprints.size() < 2) return "STABLE";

        Sprint lastSprint = completedSprints.get(0);
        Sprint previousSprint = completedSprints.get(1);

        int lastVelocity = ticketRepository.findBySprintId(lastSprint.getId()).stream()
                .filter(Ticket::isDone)
                .map(Ticket::getStoryPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int prevVelocity = ticketRepository.findBySprintId(previousSprint.getId()).stream()
                .filter(Ticket::isDone)
                .map(Ticket::getStoryPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        if (prevVelocity == 0) return "STABLE";

        double change = (double) (lastVelocity - prevVelocity) / prevVelocity;

        if (change > 0.1) return "UP";
        if (change < -0.1) return "DOWN";
        return "STABLE";
    }

    // --- Recommendations ---

    private List<String> generateRecommendations(Map<String, Integer> breakdown) {
        List<String> recommendations = new ArrayList<>();

        if (breakdown.getOrDefault("velocity", 50) < 50) {
            recommendations.add("Velocity is inconsistent. Consider more accurate sprint planning and breaking down large tickets.");
        }

        if (breakdown.getOrDefault("quality", 50) < 50) {
            recommendations.add("Ticket quality is low. Ensure tickets have clear descriptions, acceptance criteria, and estimates.");
        }

        if (breakdown.getOrDefault("delivery", 50) < 60) {
            recommendations.add("Delivery rate is below target. Review sprint commitments and reduce over-commitment.");
        }

        if (breakdown.getOrDefault("debt", 50) < 50) {
            recommendations.add("Technical debt is accumulating. Allocate 20-30% of sprint capacity to debt reduction.");
        }

        if (breakdown.getOrDefault("documentation", 50) < 40) {
            recommendations.add("Documentation coverage is low. Create or update documentation for active epics.");
        }

        if (breakdown.getOrDefault("teamLoad", 50) < 50) {
            recommendations.add("Team workload is imbalanced. Redistribute tickets to avoid bottlenecks and idle capacity.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Project health is good. Maintain current practices and continue monitoring.");
        }

        return recommendations;
    }
}
