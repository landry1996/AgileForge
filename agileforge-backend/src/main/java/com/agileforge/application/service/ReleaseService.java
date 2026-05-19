package com.agileforge.application.service;

import com.agileforge.application.dto.response.ReleaseReadinessResponse;
import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Release;
import com.agileforge.domain.model.ReleaseStatus;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketType;
import com.agileforge.domain.port.out.ReleaseRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ReleaseService {

    private static final Logger log = LoggerFactory.getLogger(ReleaseService.class);

    private final ReleaseRepositoryPort releaseRepository;
    private final TicketRepositoryPort ticketRepository;

    public ReleaseService(ReleaseRepositoryPort releaseRepository, TicketRepositoryPort ticketRepository) {
        this.releaseRepository = releaseRepository;
        this.ticketRepository = ticketRepository;
    }

    public Release createRelease(UUID projectId, String name, String version, String description,
                                 String startDate, String releaseDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = releaseDate != null ? LocalDate.parse(releaseDate) : null;

        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException("Release date cannot be before start date");
        }

        Release release = new Release(projectId, name, version, description, start, end);
        Release saved = releaseRepository.save(release);
        log.info("Release created: {} v{} in project {}", name, version, projectId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Release> getByProject(UUID projectId) {
        return releaseRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public Release getById(UUID releaseId) {
        return releaseRepository.findById(releaseId)
                .orElseThrow(() -> new EntityNotFoundException("Release", releaseId));
    }

    public Release updateRelease(UUID releaseId, String name, String version, String description,
                                 String status, String startDate, String releaseDate) {
        Release release = getById(releaseId);

        if (release.isReleased()) {
            throw new BusinessException("Cannot update a released release");
        }

        if (name != null) release.setName(name);
        if (version != null) release.setVersion(version);
        if (description != null) release.setDescription(description);
        if (status != null) release.setStatus(ReleaseStatus.valueOf(status));
        if (startDate != null) release.setStartDate(LocalDate.parse(startDate));
        if (releaseDate != null) release.setReleaseDate(LocalDate.parse(releaseDate));

        Release saved = releaseRepository.save(release);
        log.info("Release updated: {}", releaseId);
        return saved;
    }

    public void deleteRelease(UUID releaseId) {
        Release release = getById(releaseId);
        if (release.isReleased()) {
            throw new BusinessException("Cannot delete a released release");
        }
        releaseRepository.delete(releaseId);
        log.info("Release deleted: {}", releaseId);
    }

    public void addTicketToRelease(UUID releaseId, UUID ticketId) {
        Release release = getById(releaseId);
        if (release.isReleased()) {
            throw new BusinessException("Cannot add tickets to a released release");
        }

        ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        releaseRepository.addTicketToRelease(releaseId, ticketId);
        log.info("Ticket {} added to release {}", ticketId, releaseId);
    }

    public void removeTicketFromRelease(UUID releaseId, UUID ticketId) {
        Release release = getById(releaseId);
        if (release.isReleased()) {
            throw new BusinessException("Cannot remove tickets from a released release");
        }

        releaseRepository.removeTicketFromRelease(releaseId, ticketId);
        log.info("Ticket {} removed from release {}", ticketId, releaseId);
    }

    public Release releaseNow(UUID releaseId) {
        Release release = getById(releaseId);

        if (!release.canRelease()) {
            throw new BusinessException("Release cannot be marked as released. Current status: " + release.getStatus());
        }

        release.setStatus(ReleaseStatus.RELEASED);
        release.setReleasedAt(Instant.now());

        Release saved = releaseRepository.save(release);
        log.info("Release {} v{} has been released", release.getName(), release.getVersion());
        return saved;
    }

    @Transactional(readOnly = true)
    public ReleaseReadinessResponse getReadinessReport(UUID releaseId) {
        Release release = getById(releaseId);

        List<UUID> ticketIds = releaseRepository.findTicketIdsByReleaseId(releaseId);
        List<Ticket> tickets = ticketIds.stream()
                .map(ticketRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        int totalTickets = tickets.size();
        int completedTickets = (int) tickets.stream().filter(Ticket::isDone).count();
        int openBugs = (int) tickets.stream()
                .filter(t -> t.getType() == TicketType.BUG && !t.isDone())
                .count();
        int unresolvedDependencies = (int) tickets.stream()
                .filter(Ticket::isBlocked)
                .count();

        // Calculate readiness score (0-100)
        int readinessScore = calculateReadinessScore(totalTickets, completedTickets, openBugs, unresolvedDependencies);

        String recommendation = generateRecommendation(readinessScore, openBugs, unresolvedDependencies);

        return new ReleaseReadinessResponse(
                releaseId,
                release.getVersion(),
                totalTickets,
                completedTickets,
                openBugs,
                unresolvedDependencies,
                readinessScore,
                recommendation
        );
    }

    public int getTicketCount(UUID releaseId) {
        return releaseRepository.findTicketIdsByReleaseId(releaseId).size();
    }

    public int getCompletedCount(UUID releaseId) {
        List<UUID> ticketIds = releaseRepository.findTicketIdsByReleaseId(releaseId);
        return (int) ticketIds.stream()
                .map(ticketRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(Ticket::isDone)
                .count();
    }

    private int calculateReadinessScore(int total, int completed, int openBugs, int blocked) {
        if (total == 0) return 0;

        // Completion weight: 60%
        double completionRatio = (double) completed / total;
        double completionScore = completionRatio * 60;

        // Bug penalty: up to 25%
        double bugPenalty = Math.min(openBugs * 5.0, 25.0);

        // Blocked penalty: up to 15%
        double blockedPenalty = Math.min(blocked * 5.0, 15.0);

        int score = (int) Math.round(completionScore + (40 - bugPenalty - blockedPenalty));
        return Math.max(0, Math.min(100, score));
    }

    private String generateRecommendation(int score, int openBugs, int blocked) {
        if (score >= 90) {
            return "Ready for release. All major items are completed.";
        } else if (score >= 70) {
            return "Almost ready. Review remaining items before releasing.";
        } else if (score >= 50) {
            String msg = "Not yet ready.";
            if (openBugs > 0) msg += " " + openBugs + " open bug(s) need attention.";
            if (blocked > 0) msg += " " + blocked + " blocked ticket(s) need resolution.";
            return msg;
        } else {
            return "Significant work remaining. Consider postponing the release.";
        }
    }
}
