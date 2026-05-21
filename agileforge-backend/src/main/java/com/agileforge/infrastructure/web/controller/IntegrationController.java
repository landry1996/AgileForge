package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.IntegrationService;
import com.agileforge.application.service.JiraImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/integrations")
public class IntegrationController {

    private final IntegrationService integrationService;
    private final JiraImportService jiraImportService;

    public IntegrationController(IntegrationService integrationService, JiraImportService jiraImportService) {
        this.integrationService = integrationService;
        this.jiraImportService = jiraImportService;
    }

    @GetMapping("/organization/{orgId}")
    public ResponseEntity<?> getIntegrations(@PathVariable UUID orgId) {
        return ResponseEntity.ok(integrationService.getByOrganization(orgId));
    }

    @PostMapping("/configure")
    public ResponseEntity<?> configureIntegration(@RequestBody Map<String, Object> request) {
        UUID orgId = UUID.fromString((String) request.get("organizationId"));
        String provider = (String) request.get("provider");
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) request.getOrDefault("config", Map.of());
        return ResponseEntity.ok(integrationService.configureIntegration(orgId, provider, config));
    }

    @PostMapping("/{integrationId}/enable")
    public ResponseEntity<Void> enable(@PathVariable UUID integrationId) {
        integrationService.enableIntegration(integrationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{integrationId}/disable")
    public ResponseEntity<Void> disable(@PathVariable UUID integrationId) {
        integrationService.disableIntegration(integrationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{integrationId}")
    public ResponseEntity<Void> delete(@PathVariable UUID integrationId) {
        integrationService.deleteIntegration(integrationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{integrationId}/test")
    public ResponseEntity<?> testConnection(@PathVariable UUID integrationId) {
        integrationService.testConnection(integrationId);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @GetMapping("/{integrationId}/channels")
    public ResponseEntity<?> getChannelMappings(@PathVariable UUID integrationId) {
        return ResponseEntity.ok(integrationService.getChannelMappings(integrationId));
    }

    @PostMapping("/{integrationId}/channels")
    public ResponseEntity<?> addChannelMapping(@PathVariable UUID integrationId, @RequestBody Map<String, Object> request) {
        UUID projectId = UUID.fromString((String) request.get("projectId"));
        String channelId = (String) request.get("channelId");
        String channelName = (String) request.get("channelName");
        @SuppressWarnings("unchecked")
        List<String> events = (List<String>) request.getOrDefault("events", List.of("ALL"));
        return ResponseEntity.ok(integrationService.addChannelMapping(integrationId, projectId, channelId, channelName, events));
    }

    // Jira Import
    @PostMapping("/jira/preview")
    public ResponseEntity<?> previewJiraImport(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(jiraImportService.previewImport(
                request.get("jiraUrl"), request.get("projectKey"),
                request.get("apiToken"), request.get("email")));
    }

    @PostMapping("/jira/import")
    public ResponseEntity<?> startJiraImport(@RequestBody Map<String, Object> request) {
        UUID orgId = UUID.fromString((String) request.get("organizationId"));
        UUID projectId = UUID.fromString((String) request.get("targetProjectId"));
        String jiraUrl = (String) request.get("jiraUrl");
        String projectKey = (String) request.get("projectKey");
        String apiToken = (String) request.get("apiToken");
        String email = (String) request.get("email");

        jiraImportService.startImport(orgId, projectId, jiraUrl, projectKey, apiToken, email,
                jiraImportService.getDefaultMapping());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/jira/status/{jobId}")
    public ResponseEntity<?> getImportStatus(@PathVariable UUID jobId) {
        var job = jiraImportService.getImportStatus(jobId);
        return job != null ? ResponseEntity.ok(job) : ResponseEntity.notFound().build();
    }

    @GetMapping("/jira/history/{orgId}")
    public ResponseEntity<?> getImportHistory(@PathVariable UUID orgId) {
        return ResponseEntity.ok(jiraImportService.getImportHistory(orgId));
    }

    @GetMapping("/jira/mapping")
    public ResponseEntity<?> getDefaultMapping() {
        return ResponseEntity.ok(jiraImportService.getDefaultMapping());
    }
}
