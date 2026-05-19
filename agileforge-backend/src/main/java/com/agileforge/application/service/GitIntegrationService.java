package com.agileforge.application.service;

import com.agileforge.application.dto.request.ConnectRepositoryRequest;
import com.agileforge.application.dto.request.CreateBranchRequest;
import com.agileforge.application.dto.response.*;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class GitIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(GitIntegrationService.class);

    private final GitRepositoryPort gitRepositoryPort;
    private final GitBranchPort gitBranchPort;
    private final GitPullRequestPort gitPullRequestPort;
    private final GitPipelinePort gitPipelinePort;

    public GitIntegrationService(GitRepositoryPort gitRepositoryPort,
                                  GitBranchPort gitBranchPort,
                                  GitPullRequestPort gitPullRequestPort,
                                  GitPipelinePort gitPipelinePort) {
        this.gitRepositoryPort = gitRepositoryPort;
        this.gitBranchPort = gitBranchPort;
        this.gitPullRequestPort = gitPullRequestPort;
        this.gitPipelinePort = gitPipelinePort;
    }

    public GitRepositoryResponse connectRepository(UUID projectId, ConnectRepositoryRequest request) {
        log.info("Connecting repository {}/{} to project {}", request.owner(), request.repoName(), projectId);

        GitRepository repo = new GitRepository(projectId, request.owner(), request.repoName(),
                request.defaultBranch() != null ? request.defaultBranch() : "main");

        GitRepository saved = gitRepositoryPort.save(repo);
        return toRepositoryResponse(saved);
    }

    public void disconnectRepository(UUID repoId) {
        log.info("Disconnecting repository {}", repoId);
        gitRepositoryPort.findById(repoId)
                .orElseThrow(() -> new EntityNotFoundException("GitRepository", repoId));
        gitRepositoryPort.delete(repoId);
    }

    @Transactional(readOnly = true)
    public List<GitRepositoryResponse> getRepositories(UUID projectId) {
        return gitRepositoryPort.findByProjectId(projectId).stream()
                .map(this::toRepositoryResponse).toList();
    }

    public String suggestBranchName(String ticketKey, String ticketTitle, String ticketType) {
        String prefix = switch (ticketType.toUpperCase()) {
            case "BUG" -> "bugfix";
            case "EPIC" -> "epic";
            case "HOTFIX" -> "hotfix";
            default -> "feature";
        };

        String slug = ticketTitle.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");

        // Limit slug length to keep branch name reasonable
        if (slug.length() > 40) {
            slug = slug.substring(0, 40);
            int lastDash = slug.lastIndexOf('-');
            if (lastDash > 20) {
                slug = slug.substring(0, lastDash);
            }
        }

        return prefix + "/" + ticketKey.toLowerCase() + "-" + slug;
    }

    public GitBranchResponse registerBranch(UUID repoId, CreateBranchRequest request) {
        gitRepositoryPort.findById(repoId)
                .orElseThrow(() -> new EntityNotFoundException("GitRepository", repoId));

        GitBranch branch = new GitBranch(repoId, request.ticketId(), request.branchName());
        GitBranch saved = gitBranchPort.save(branch);
        return toBranchResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GitBranchResponse> getBranches(UUID repoId) {
        return gitBranchPort.findByRepositoryId(repoId).stream()
                .map(this::toBranchResponse).toList();
    }

    public GitPullRequestResponse registerPullRequest(UUID repoId, UUID ticketId, int prNumber,
                                                       String title, String author,
                                                       String sourceBranch, String targetBranch, String url) {
        gitRepositoryPort.findById(repoId)
                .orElseThrow(() -> new EntityNotFoundException("GitRepository", repoId));

        GitPullRequest pr = new GitPullRequest(repoId, ticketId, prNumber, title, author,
                sourceBranch, targetBranch, url);
        GitPullRequest saved = gitPullRequestPort.save(pr);
        return toPullRequestResponse(saved);
    }

    public GitPipelineResponse registerPipeline(UUID repoId, UUID ticketId, UUID prId,
                                                 String pipelineId, String status, String url,
                                                 Instant startedAt, Instant finishedAt) {
        gitRepositoryPort.findById(repoId)
                .orElseThrow(() -> new EntityNotFoundException("GitRepository", repoId));

        PipelineStatus pipelineStatus = PipelineStatus.valueOf(status.toUpperCase());
        GitPipeline pipeline = new GitPipeline(repoId, ticketId, prId, pipelineId, pipelineStatus, url);
        pipeline.setStartedAt(startedAt);
        pipeline.setFinishedAt(finishedAt);

        GitPipeline saved = gitPipelinePort.save(pipeline);
        return toPipelineResponse(saved);
    }

    @Transactional(readOnly = true)
    public TicketDevInfoResponse getTicketDevInfo(UUID ticketId) {
        List<GitBranchResponse> branches = gitBranchPort.findByTicketId(ticketId).stream()
                .map(this::toBranchResponse).toList();
        List<GitPullRequestResponse> pullRequests = gitPullRequestPort.findByTicketId(ticketId).stream()
                .map(this::toPullRequestResponse).toList();
        List<GitPipelineResponse> pipelines = gitPipelinePort.findByTicketId(ticketId).stream()
                .map(this::toPipelineResponse).toList();

        return new TicketDevInfoResponse(ticketId, branches, pullRequests, pipelines);
    }

    @SuppressWarnings("unchecked")
    public void processWebhook(Map<String, Object> payload) {
        String action = (String) payload.get("action");
        log.info("Processing GitHub webhook with action: {}", action);

        if (payload.containsKey("pull_request")) {
            processPullRequestWebhook(action, payload);
        } else if (payload.containsKey("check_run") || payload.containsKey("check_suite")) {
            processPipelineWebhook(action, payload);
        }
    }

    @SuppressWarnings("unchecked")
    private void processPullRequestWebhook(String action, Map<String, Object> payload) {
        Map<String, Object> prData = (Map<String, Object>) payload.get("pull_request");
        Map<String, Object> repoData = (Map<String, Object>) payload.get("repository");

        if (prData == null || repoData == null) {
            log.warn("Incomplete PR webhook payload received");
            return;
        }

        int prNumber = ((Number) prData.get("number")).intValue();
        String title = (String) prData.get("title");
        String url = (String) prData.get("html_url");
        String author = extractAuthor(prData);
        String sourceBranch = extractBranch(prData, "head");
        String targetBranch = extractBranch(prData, "base");

        String repoFullName = (String) repoData.get("full_name");
        String[] parts = repoFullName.split("/");
        if (parts.length != 2) return;

        // Find the matching repository in our system
        // In a real implementation, we would look up by owner/repo_name
        log.info("PR webhook - action: {}, repo: {}, PR #{}", action, repoFullName, prNumber);

        // Handle status changes based on action
        switch (action) {
            case "opened", "reopened" -> log.info("PR #{} opened in {}", prNumber, repoFullName);
            case "closed" -> {
                boolean merged = prData.get("merged") != null && (Boolean) prData.get("merged");
                if (merged) {
                    log.info("PR #{} merged in {}", prNumber, repoFullName);
                } else {
                    log.info("PR #{} closed in {}", prNumber, repoFullName);
                }
            }
            default -> log.debug("Unhandled PR action: {}", action);
        }
    }

    @SuppressWarnings("unchecked")
    private void processPipelineWebhook(String action, Map<String, Object> payload) {
        Map<String, Object> checkData = payload.containsKey("check_run")
                ? (Map<String, Object>) payload.get("check_run")
                : (Map<String, Object>) payload.get("check_suite");

        if (checkData == null) {
            log.warn("Incomplete pipeline webhook payload received");
            return;
        }

        String status = (String) checkData.get("status");
        String conclusion = (String) checkData.get("conclusion");
        String detailsUrl = (String) checkData.get("details_url");

        log.info("Pipeline webhook - status: {}, conclusion: {}", status, conclusion);
    }

    @SuppressWarnings("unchecked")
    private String extractAuthor(Map<String, Object> prData) {
        Map<String, Object> user = (Map<String, Object>) prData.get("user");
        return user != null ? (String) user.get("login") : null;
    }

    @SuppressWarnings("unchecked")
    private String extractBranch(Map<String, Object> prData, String key) {
        Map<String, Object> branchData = (Map<String, Object>) prData.get(key);
        return branchData != null ? (String) branchData.get("ref") : null;
    }

    // Mapping methods
    private GitRepositoryResponse toRepositoryResponse(GitRepository repo) {
        return new GitRepositoryResponse(
                repo.getId(), repo.getProjectId(), repo.getProvider(),
                repo.getOwner(), repo.getRepoName(), repo.getDefaultBranch(),
                repo.isActive(), repo.getCreatedAt());
    }

    private GitBranchResponse toBranchResponse(GitBranch branch) {
        return new GitBranchResponse(
                branch.getId(), branch.getRepositoryId(), branch.getTicketId(),
                branch.getBranchName(), branch.getCreatedAt());
    }

    private GitPullRequestResponse toPullRequestResponse(GitPullRequest pr) {
        return new GitPullRequestResponse(
                pr.getId(), pr.getRepositoryId(), pr.getTicketId(), pr.getPrNumber(),
                pr.getTitle(), pr.getStatus() != null ? pr.getStatus().name() : null,
                pr.getAuthor(), pr.getSourceBranch(), pr.getTargetBranch(), pr.getUrl(),
                pr.getCreatedAt(), pr.getMergedAt(), pr.getClosedAt());
    }

    private GitPipelineResponse toPipelineResponse(GitPipeline pipeline) {
        return new GitPipelineResponse(
                pipeline.getId(), pipeline.getRepositoryId(), pipeline.getTicketId(),
                pipeline.getPrId(), pipeline.getPipelineId(),
                pipeline.getStatus() != null ? pipeline.getStatus().name() : null,
                pipeline.getUrl(), pipeline.getStartedAt(), pipeline.getFinishedAt(),
                pipeline.getCreatedAt());
    }
}
