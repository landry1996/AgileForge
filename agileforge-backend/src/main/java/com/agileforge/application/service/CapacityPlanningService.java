package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreateCapacityEntryRequest;
import com.agileforge.application.dto.response.CapacityEntryResponse;
import com.agileforge.application.dto.response.CapacityForecastResponse;
import com.agileforge.application.dto.response.MemberCapacity;
import com.agileforge.application.dto.response.TeamCapacityResponse;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.CapacityEntry;
import com.agileforge.domain.model.Sprint;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketStatus;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.out.CapacityEntryRepositoryPort;
import com.agileforge.domain.port.out.SprintRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import com.agileforge.domain.port.out.UserRepositoryPort;
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
public class CapacityPlanningService {

    private static final Logger log = LoggerFactory.getLogger(CapacityPlanningService.class);

    private final CapacityEntryRepositoryPort capacityRepository;
    private final UserRepositoryPort userRepository;
    private final TicketRepositoryPort ticketRepository;
    private final SprintRepositoryPort sprintRepository;

    public CapacityPlanningService(CapacityEntryRepositoryPort capacityRepository,
                                   UserRepositoryPort userRepository,
                                   TicketRepositoryPort ticketRepository,
                                   SprintRepositoryPort sprintRepository) {
        this.capacityRepository = capacityRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.sprintRepository = sprintRepository;
    }

    public CapacityEntryResponse addCapacityEntry(UUID projectId, CreateCapacityEntryRequest request) {
        userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User", request.userId()));

        if (request.sprintId() != null) {
            sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new EntityNotFoundException("Sprint", request.sprintId()));
        }

        CapacityEntry entry = new CapacityEntry(
                projectId,
                request.userId(),
                request.sprintId(),
                request.availableHours(),
                request.plannedLeaveHours() != null ? request.plannedLeaveHours() : 0,
                request.notes()
        );

        CapacityEntry saved = capacityRepository.save(entry);
        log.info("Capacity entry created for user {} in project {}", request.userId(), projectId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TeamCapacityResponse getTeamCapacity(UUID projectId, UUID sprintId) {
        List<CapacityEntry> entries = capacityRepository.findByProjectIdAndSprintId(projectId, sprintId);

        double totalAvailable = 0;
        double totalLeave = 0;
        List<MemberCapacity> members = new ArrayList<>();

        for (CapacityEntry entry : entries) {
            totalAvailable += entry.getAvailableHours();
            totalLeave += entry.getPlannedLeaveHours();

            String userName = getUserDisplayName(entry.getUserId());
            int assignedPoints = calculateAssignedPoints(entry.getUserId(), sprintId);
            double netHours = entry.getAvailableHours() - entry.getPlannedLeaveHours();
            int loadPercentage = netHours > 0 ? (int) Math.min(100, (assignedPoints * 8.0 / netHours) * 100) : 0;

            members.add(new MemberCapacity(
                    entry.getUserId(),
                    userName,
                    entry.getAvailableHours(),
                    entry.getPlannedLeaveHours(),
                    assignedPoints,
                    loadPercentage
            ));
        }

        double netCapacity = totalAvailable - totalLeave;

        return new TeamCapacityResponse(projectId, sprintId, totalAvailable, totalLeave, netCapacity, members);
    }

    @Transactional(readOnly = true)
    public CapacityEntryResponse getMemberCapacity(UUID projectId, UUID userId) {
        List<CapacityEntry> entries = capacityRepository.findByProjectId(projectId).stream()
                .filter(e -> e.getUserId().equals(userId))
                .toList();

        if (entries.isEmpty()) {
            throw new EntityNotFoundException("Capacity entry for user " + userId + " in project " + projectId);
        }

        // Return the most recent entry
        CapacityEntry latest = entries.get(entries.size() - 1);
        return toResponse(latest);
    }

    @Transactional(readOnly = true)
    public CapacityForecastResponse getCapacityForecast(UUID projectId) {
        // Get current sprint capacity
        Optional<Sprint> activeSprint = sprintRepository.findActiveByProjectId(projectId);
        double currentSprintCapacity = 0;

        if (activeSprint.isPresent()) {
            currentSprintCapacity = capacityRepository.sumAvailableHoursByProjectAndSprint(
                    projectId, activeSprint.get().getId());
        }

        // Calculate average velocity from completed sprints
        List<Sprint> completedSprints = sprintRepository.findCompletedByProjectId(projectId);
        double averageVelocity = calculateAverageVelocity(completedSprints);

        // Estimate sprints to clear backlog
        long backlogCount = ticketRepository.countByProjectIdAndStatus(projectId, TicketStatus.BACKLOG)
                + ticketRepository.countByProjectIdAndStatus(projectId, TicketStatus.TODO);
        int estimatedSprints = averageVelocity > 0 ? (int) Math.ceil(backlogCount / averageVelocity) : -1;

        // Identify bottlenecks (simplified: look for users with high load)
        List<String> bottleneckSkills = identifyBottlenecks(projectId, activeSprint.orElse(null));

        return new CapacityForecastResponse(
                projectId,
                currentSprintCapacity,
                averageVelocity,
                estimatedSprints,
                bottleneckSkills
        );
    }

    public CapacityEntryResponse updateEntry(UUID entryId, CreateCapacityEntryRequest request) {
        CapacityEntry entry = capacityRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("CapacityEntry", entryId));

        entry.setAvailableHours(request.availableHours());
        if (request.plannedLeaveHours() != null) {
            entry.setPlannedLeaveHours(request.plannedLeaveHours());
        }
        if (request.notes() != null) {
            entry.setNotes(request.notes());
        }

        CapacityEntry saved = capacityRepository.save(entry);
        log.info("Capacity entry updated: {}", entryId);
        return toResponse(saved);
    }

    public void deleteEntry(UUID entryId) {
        capacityRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("CapacityEntry", entryId));
        capacityRepository.delete(entryId);
        log.info("Capacity entry deleted: {}", entryId);
    }

    private double calculateAverageVelocity(List<Sprint> completedSprints) {
        if (completedSprints.isEmpty()) return 0;

        double totalVelocity = 0;
        int count = 0;

        for (Sprint sprint : completedSprints) {
            List<Ticket> sprintTickets = ticketRepository.findBySprintId(sprint.getId());
            long doneCount = sprintTickets.stream().filter(Ticket::isDone).count();
            totalVelocity += doneCount;
            count++;
        }

        return count > 0 ? totalVelocity / count : 0;
    }

    private List<String> identifyBottlenecks(UUID projectId, Sprint activeSprint) {
        List<String> bottlenecks = new ArrayList<>();

        if (activeSprint == null) return bottlenecks;

        List<CapacityEntry> entries = capacityRepository.findByProjectIdAndSprintId(
                projectId, activeSprint.getId());

        for (CapacityEntry entry : entries) {
            double netHours = entry.getAvailableHours() - entry.getPlannedLeaveHours();
            int assignedPoints = calculateAssignedPoints(entry.getUserId(), activeSprint.getId());

            // If load exceeds 90%, consider it a bottleneck
            if (netHours > 0 && (assignedPoints * 8.0 / netHours) > 0.9) {
                String userName = getUserDisplayName(entry.getUserId());
                bottlenecks.add(userName + " (overloaded)");
            }
        }

        return bottlenecks;
    }

    private int calculateAssignedPoints(UUID userId, UUID sprintId) {
        List<Ticket> assignedTickets = ticketRepository.findByAssigneeId(userId).stream()
                .filter(t -> sprintId.equals(t.getSprintId()))
                .filter(t -> !t.isDone())
                .toList();

        return assignedTickets.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
    }

    private String getUserDisplayName(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
                        return user.getDisplayName();
                    }
                    return user.getFirstName() + " " + user.getLastName();
                })
                .orElse("Unknown");
    }

    private CapacityEntryResponse toResponse(CapacityEntry entry) {
        String userName = getUserDisplayName(entry.getUserId());
        return new CapacityEntryResponse(
                entry.getId(),
                entry.getProjectId(),
                entry.getUserId(),
                userName,
                entry.getSprintId(),
                entry.getAvailableHours(),
                entry.getPlannedLeaveHours(),
                entry.getNotes(),
                entry.getCreatedAt()
        );
    }
}
