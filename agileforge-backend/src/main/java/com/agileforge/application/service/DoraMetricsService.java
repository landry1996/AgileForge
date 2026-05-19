package com.agileforge.application.service;

import com.agileforge.application.dto.response.ChangeFailureRate;
import com.agileforge.application.dto.response.DeploymentFrequency;
import com.agileforge.application.dto.response.DoraMetricsResponse;
import com.agileforge.application.dto.response.LeadTime;
import com.agileforge.application.dto.response.MTTR;
import com.agileforge.domain.model.GitPipeline;
import com.agileforge.domain.model.GitRepository;
import com.agileforge.domain.model.PipelineStatus;
import com.agileforge.domain.model.Release;
import com.agileforge.domain.port.out.GitPipelinePort;
import com.agileforge.domain.port.out.GitRepositoryPort;
import com.agileforge.domain.port.out.ReleaseRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DoraMetricsService {

    private static final Logger log = LoggerFactory.getLogger(DoraMetricsService.class);

    private final GitPipelinePort gitPipelinePort;
    private final GitRepositoryPort gitRepositoryPort;
    private final ReleaseRepositoryPort releaseRepositoryPort;

    public DoraMetricsService(GitPipelinePort gitPipelinePort,
                              GitRepositoryPort gitRepositoryPort,
                              ReleaseRepositoryPort releaseRepositoryPort) {
        this.gitPipelinePort = gitPipelinePort;
        this.gitRepositoryPort = gitRepositoryPort;
        this.releaseRepositoryPort = releaseRepositoryPort;
    }

    public DoraMetricsResponse calculateDoraMetrics(UUID projectId) {
        log.info("Calculating DORA metrics for project {}", projectId);

        List<GitRepository> repositories = gitRepositoryPort.findByProjectId(projectId);
        List<GitPipeline> allPipelines = new ArrayList<>();
        for (GitRepository repo : repositories) {
            List<GitPipeline> pipelines = gitPipelinePort.findByRepositoryId(repo.getId());
            allPipelines.addAll(pipelines);
        }

        List<Release> releases = releaseRepositoryPort.findByProjectId(projectId);

        DeploymentFrequency deploymentFrequency = calculateDeploymentFrequency(allPipelines);
        LeadTime leadTime = calculateLeadTime(allPipelines);
        MTTR mttr = calculateMTTR(allPipelines);
        ChangeFailureRate changeFailureRate = calculateChangeFailureRate(allPipelines);

        String overallLevel = calculateOverallLevel(
                deploymentFrequency.level(),
                leadTime.level(),
                mttr.level(),
                changeFailureRate.level()
        );

        return new DoraMetricsResponse(
                projectId,
                deploymentFrequency,
                leadTime,
                mttr,
                changeFailureRate,
                overallLevel
        );
    }

    private DeploymentFrequency calculateDeploymentFrequency(List<GitPipeline> pipelines) {
        Instant fourWeeksAgo = Instant.now().minus(28, ChronoUnit.DAYS);

        long successfulDeployments = pipelines.stream()
                .filter(p -> p.getStatus() == PipelineStatus.SUCCESS)
                .filter(p -> p.getFinishedAt() != null && p.getFinishedAt().isAfter(fourWeeksAgo))
                .count();

        double deploymentsPerWeek = successfulDeployments / 4.0;

        String level;
        if (deploymentsPerWeek >= 7) {
            level = "ELITE";
        } else if (deploymentsPerWeek >= 1) {
            level = "HIGH";
        } else if (deploymentsPerWeek >= 0.25) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        return new DeploymentFrequency(deploymentsPerWeek, level);
    }

    private LeadTime calculateLeadTime(List<GitPipeline> pipelines) {
        List<Duration> leadTimes = pipelines.stream()
                .filter(p -> p.getStatus() == PipelineStatus.SUCCESS)
                .filter(p -> p.getStartedAt() != null && p.getFinishedAt() != null)
                .map(p -> Duration.between(p.getStartedAt(), p.getFinishedAt()))
                .toList();

        if (leadTimes.isEmpty()) {
            return new LeadTime(0, "LOW");
        }

        double averageHours = leadTimes.stream()
                .mapToLong(Duration::toHours)
                .average()
                .orElse(0);

        String level;
        if (averageHours < 1) {
            level = "ELITE";
        } else if (averageHours < 24) {
            level = "HIGH";
        } else if (averageHours < 168) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        return new LeadTime(averageHours, level);
    }

    private MTTR calculateMTTR(List<GitPipeline> pipelines) {
        List<Duration> recoveryTimes = new ArrayList<>();

        List<GitPipeline> sorted = pipelines.stream()
                .filter(p -> p.getFinishedAt() != null)
                .sorted((a, b) -> a.getFinishedAt().compareTo(b.getFinishedAt()))
                .toList();

        for (int i = 0; i < sorted.size() - 1; i++) {
            GitPipeline current = sorted.get(i);
            if (current.getStatus() == PipelineStatus.FAILED) {
                // Find the next successful pipeline after this failure
                for (int j = i + 1; j < sorted.size(); j++) {
                    GitPipeline next = sorted.get(j);
                    if (next.getStatus() == PipelineStatus.SUCCESS) {
                        recoveryTimes.add(Duration.between(current.getFinishedAt(), next.getFinishedAt()));
                        break;
                    }
                }
            }
        }

        if (recoveryTimes.isEmpty()) {
            return new MTTR(0, "ELITE");
        }

        double averageHours = recoveryTimes.stream()
                .mapToLong(Duration::toHours)
                .average()
                .orElse(0);

        String level;
        if (averageHours < 1) {
            level = "ELITE";
        } else if (averageHours < 24) {
            level = "HIGH";
        } else if (averageHours < 168) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        return new MTTR(averageHours, level);
    }

    private ChangeFailureRate calculateChangeFailureRate(List<GitPipeline> pipelines) {
        long totalDeployments = pipelines.stream()
                .filter(p -> p.getStatus() == PipelineStatus.SUCCESS || p.getStatus() == PipelineStatus.FAILED)
                .filter(p -> p.getFinishedAt() != null)
                .count();

        if (totalDeployments == 0) {
            return new ChangeFailureRate(0, "ELITE");
        }

        long failedDeployments = pipelines.stream()
                .filter(p -> p.getStatus() == PipelineStatus.FAILED)
                .filter(p -> p.getFinishedAt() != null)
                .count();

        double rate = (double) failedDeployments / totalDeployments * 100;

        String level;
        if (rate < 5) {
            level = "ELITE";
        } else if (rate < 10) {
            level = "HIGH";
        } else if (rate < 15) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        return new ChangeFailureRate(rate, level);
    }

    private String calculateOverallLevel(String... levels) {
        int score = 0;
        for (String level : levels) {
            switch (level) {
                case "ELITE" -> score += 4;
                case "HIGH" -> score += 3;
                case "MEDIUM" -> score += 2;
                case "LOW" -> score += 1;
            }
        }

        double average = (double) score / levels.length;
        if (average >= 3.5) return "ELITE";
        if (average >= 2.5) return "HIGH";
        if (average >= 1.5) return "MEDIUM";
        return "LOW";
    }
}
