package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.ConnectRepositoryRequest;
import com.agileforge.application.dto.request.CreateBranchRequest;
import com.agileforge.application.dto.response.*;
import com.agileforge.application.service.GitIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Tag(name = "Git Integration", description = "GitHub integration endpoints for repositories, branches, PRs, and CI/CD")
public class GitIntegrationController {

    private final GitIntegrationService gitIntegrationService;

    public GitIntegrationController(GitIntegrationService gitIntegrationService) {
        this.gitIntegrationService = gitIntegrationService;
    }

    @PostMapping("/projects/{projectId}/repositories")
    @Operation(summary = "Connect a GitHub repository to a project")
    public ResponseEntity<GitRepositoryResponse> connectRepository(
            @PathVariable UUID projectId,
            @Valid @RequestBody ConnectRepositoryRequest request) {
        GitRepositoryResponse response = gitIntegrationService.connectRepository(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/repositories")
    @Operation(summary = "Get all connected repositories for a project")
    public ResponseEntity<List<GitRepositoryResponse>> getRepositories(@PathVariable UUID projectId) {
        List<GitRepositoryResponse> repositories = gitIntegrationService.getRepositories(projectId);
        return ResponseEntity.ok(repositories);
    }

    @DeleteMapping("/repositories/{repoId}")
    @Operation(summary = "Disconnect a repository")
    public ResponseEntity<Void> disconnectRepository(@PathVariable UUID repoId) {
        gitIntegrationService.disconnectRepository(repoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tickets/{ticketId}/dev-info")
    @Operation(summary = "Get all git information for a ticket (branches, PRs, pipelines)")
    public ResponseEntity<TicketDevInfoResponse> getTicketDevInfo(@PathVariable UUID ticketId) {
        TicketDevInfoResponse devInfo = gitIntegrationService.getTicketDevInfo(ticketId);
        return ResponseEntity.ok(devInfo);
    }

    @PostMapping("/repositories/{repoId}/branches")
    @Operation(summary = "Register a branch linked to a repository")
    public ResponseEntity<GitBranchResponse> registerBranch(
            @PathVariable UUID repoId,
            @Valid @RequestBody CreateBranchRequest request) {
        GitBranchResponse response = gitIntegrationService.registerBranch(repoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/repositories/{repoId}/branches")
    @Operation(summary = "Get all branches for a repository")
    public ResponseEntity<List<GitBranchResponse>> getBranches(@PathVariable UUID repoId) {
        List<GitBranchResponse> branches = gitIntegrationService.getBranches(repoId);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/tickets/{ticketId}/suggest-branch")
    @Operation(summary = "Suggest a branch name based on ticket information")
    public ResponseEntity<Map<String, String>> suggestBranchName(
            @RequestParam String ticketKey,
            @RequestParam String ticketTitle,
            @RequestParam(defaultValue = "STORY") String ticketType) {
        String suggested = gitIntegrationService.suggestBranchName(ticketKey, ticketTitle, ticketType);
        return ResponseEntity.ok(Map.of("branchName", suggested));
    }

    @PostMapping("/webhooks/github")
    @Operation(summary = "Receive GitHub webhook events")
    public ResponseEntity<Void> handleGitHubWebhook(@RequestBody Map<String, Object> payload) {
        gitIntegrationService.processWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
