package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.DataExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/export")
@Tag(name = "Data Export", description = "Data export endpoints for compliance and reporting")
public class DataExportController {

    private final DataExportService dataExportService;

    public DataExportController(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @GetMapping("/audit/{orgId}")
    @Operation(summary = "Export audit log as CSV")
    public ResponseEntity<String> exportAuditLog(
            @PathVariable UUID orgId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        String csv = dataExportService.exportAuditCsv(orgId, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-log.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Export project tickets as CSV")
    public ResponseEntity<String> exportProjectTickets(@PathVariable UUID projectId) {
        String csv = dataExportService.exportProjectTicketsCsv(projectId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/user/{userId}/data")
    @Operation(summary = "Export user data (GDPR data portability)")
    public ResponseEntity<Map<String, Object>> exportUserData(@PathVariable UUID userId) {
        Map<String, Object> data = dataExportService.exportUserData(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user-data.json")
                .body(data);
    }
}
