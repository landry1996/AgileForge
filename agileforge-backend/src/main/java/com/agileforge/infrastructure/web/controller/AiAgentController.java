package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.AiAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ai-agents")
public class AiAgentController {

    private final AiAgentService aiAgentService;

    public AiAgentController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @GetMapping
    public ResponseEntity<?> getAvailableAgents() {
        return ResponseEntity.ok(aiAgentService.getAvailableAgents());
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<?> getAgent(@PathVariable UUID agentId) {
        var agent = aiAgentService.getAgentById(agentId);
        return agent != null ? ResponseEntity.ok(agent) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{agentId}/execute")
    public ResponseEntity<Void> executeTask(@PathVariable UUID agentId, @RequestBody Map<String, Object> request) {
        UUID projectId = request.containsKey("projectId") ? UUID.fromString((String) request.get("projectId")) : null;
        UUID orgId = UUID.fromString((String) request.get("organizationId"));
        UUID triggeredBy = UUID.fromString((String) request.get("triggeredBy"));
        String taskType = (String) request.get("taskType");
        @SuppressWarnings("unchecked")
        Map<String, Object> input = (Map<String, Object>) request.getOrDefault("input", Map.of());

        aiAgentService.executeAgentTask(agentId, projectId, orgId, triggeredBy, taskType, input);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/sprint-plan/{projectId}")
    public ResponseEntity<?> suggestSprintPlan(@PathVariable UUID projectId,
                                               @RequestParam(required = false) UUID sprintId) {
        return ResponseEntity.ok(aiAgentService.suggestSprintPlan(projectId, sprintId));
    }

    @GetMapping("/risks/{projectId}")
    public ResponseEntity<?> analyzeRisks(@PathVariable UUID projectId) {
        return ResponseEntity.ok(aiAgentService.analyzeProjectRisks(projectId));
    }

    @PostMapping("/documentation/{projectId}")
    public ResponseEntity<?> generateDocumentation(@PathVariable UUID projectId,
                                                   @RequestBody Map<String, String> request) {
        String docType = request.getOrDefault("docType", "GENERAL");
        return ResponseEntity.ok(Map.of("content", aiAgentService.generateDocumentation(projectId, docType)));
    }

    @GetMapping("/retrospective/{sprintId}")
    public ResponseEntity<?> generateRetrospective(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(Map.of("content", aiAgentService.generateRetrospective(sprintId)));
    }

    @GetMapping("/tasks/{projectId}")
    public ResponseEntity<?> getTaskHistory(@PathVariable UUID projectId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(aiAgentService.getTaskHistory(projectId, page, size));
    }

    @GetMapping("/tasks/result/{taskId}")
    public ResponseEntity<?> getTaskResult(@PathVariable UUID taskId) {
        var result = aiAgentService.getTaskResult(taskId);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }
}
