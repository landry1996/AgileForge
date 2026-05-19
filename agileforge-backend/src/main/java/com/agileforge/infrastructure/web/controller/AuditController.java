package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.AuditFilterRequest;
import com.agileforge.application.dto.request.CreateAuditAlertRuleRequest;
import com.agileforge.application.dto.response.AuditAlertRuleResponse;
import com.agileforge.application.dto.response.AuditEventResponse;
import com.agileforge.application.dto.response.AuditSummaryResponse;
import com.agileforge.application.service.AuditService;
import com.agileforge.domain.model.AuditAlertRule;
import com.agileforge.domain.model.AuditEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@Tag(name = "Audit Trail", description = "Audit trail and compliance endpoints")
public class AuditController {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditController(AuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/organizations/{orgId}/audit")
    @Operation(summary = "Get paginated audit log for an organization")
    public ResponseEntity<List<AuditEventResponse>> getAuditLog(
            @PathVariable UUID orgId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        AuditFilterRequest filters = new AuditFilterRequest(userId, action, entityType, severity, fromDate, toDate, page, size);
        List<AuditEventResponse> events = auditService.getByOrganization(orgId, filters).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/organizations/{orgId}/audit/summary")
    @Operation(summary = "Get audit summary statistics")
    public ResponseEntity<AuditSummaryResponse> getSummary(@PathVariable UUID orgId) {
        AuditSummaryResponse summary = auditService.getSummary(orgId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/audit/user/{userId}")
    @Operation(summary = "Get audit events for a specific user")
    public ResponseEntity<List<AuditEventResponse>> getUserActivity(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<AuditEventResponse> events = auditService.getByUser(userId, page, size).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/audit/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit events for a specific entity")
    public ResponseEntity<List<AuditEventResponse>> getEntityHistory(
            @PathVariable String entityType,
            @PathVariable UUID entityId) {
        List<AuditEventResponse> events = auditService.getByEntity(entityType, entityId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/organizations/{orgId}/audit/export")
    @Operation(summary = "Export audit log for a date range")
    public ResponseEntity<List<AuditEventResponse>> exportAuditLog(
            @PathVariable UUID orgId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        List<AuditEventResponse> events = auditService.exportAuditLog(orgId, from, to).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(events);
    }

    @PostMapping("/organizations/{orgId}/audit/alert-rules")
    @Operation(summary = "Create an audit alert rule")
    public ResponseEntity<AuditAlertRuleResponse> createAlertRule(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateAuditAlertRuleRequest request) {
        AuditAlertRule rule = auditService.createAlertRule(orgId, request.name(),
                request.actionPattern(), request.severity(), request.notifyEmails());
        return ResponseEntity.status(HttpStatus.CREATED).body(toAlertRuleResponse(rule));
    }

    @GetMapping("/organizations/{orgId}/audit/alert-rules")
    @Operation(summary = "Get audit alert rules for an organization")
    public ResponseEntity<List<AuditAlertRuleResponse>> getAlertRules(@PathVariable UUID orgId) {
        List<AuditAlertRuleResponse> rules = auditService.getAlertRules(orgId).stream()
                .map(this::toAlertRuleResponse)
                .toList();
        return ResponseEntity.ok(rules);
    }

    @DeleteMapping("/audit/alert-rules/{ruleId}")
    @Operation(summary = "Delete an audit alert rule")
    public ResponseEntity<Void> deleteAlertRule(@PathVariable UUID ruleId) {
        auditService.deleteAlertRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    private AuditEventResponse toResponse(AuditEvent event) {
        Map<String, Object> detailsMap = null;
        if (event.getDetails() != null && !event.getDetails().isBlank()) {
            try {
                detailsMap = objectMapper.readValue(event.getDetails(), new TypeReference<>() {});
            } catch (Exception e) {
                detailsMap = Map.of("raw", event.getDetails());
            }
        }

        return new AuditEventResponse(
                event.getId(),
                event.getOrganizationId(),
                event.getProjectId(),
                event.getUserId(),
                event.getAction() != null ? event.getAction().name() : null,
                event.getEntityType(),
                event.getEntityId(),
                detailsMap,
                event.getIpAddress(),
                event.getSeverity() != null ? event.getSeverity().name() : null,
                event.getCreatedAt()
        );
    }

    private AuditAlertRuleResponse toAlertRuleResponse(AuditAlertRule rule) {
        return new AuditAlertRuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getActionPattern(),
                rule.getSeverity() != null ? rule.getSeverity().name() : null,
                rule.getNotifyEmails(),
                rule.isActive(),
                rule.getCreatedAt()
        );
    }
}
