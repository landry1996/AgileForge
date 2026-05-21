package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ResponseEntity<?> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/industry/{industry}")
    public ResponseEntity<?> getByIndustry(@PathVariable String industry) {
        return ResponseEntity.ok(templateService.getByIndustry(industry));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<?> getTemplate(@PathVariable UUID templateId) {
        var template = templateService.getById(templateId);
        return template != null ? ResponseEntity.ok(template) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{templateId}/apply/{projectId}")
    public ResponseEntity<?> applyTemplate(@PathVariable UUID templateId, @PathVariable UUID projectId) {
        return ResponseEntity.ok(templateService.applyTemplate(projectId, templateId));
    }

    @PostMapping
    public ResponseEntity<?> createTemplate(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(templateService.createTemplate(
                request.get("name"), request.get("description"), request.get("industry"),
                request.get("category"), request.get("workflowDefinition"), request.get("icon")));
    }

    // Marketplace
    @GetMapping("/marketplace")
    public ResponseEntity<?> getMarketplace(@RequestParam(required = false) String category,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(templateService.getPublishedExtensions(category, page, size));
    }

    @GetMapping("/marketplace/{extensionId}")
    public ResponseEntity<?> getExtension(@PathVariable UUID extensionId) {
        var ext = templateService.getExtensionById(extensionId);
        return ext != null ? ResponseEntity.ok(ext) : ResponseEntity.notFound().build();
    }

    @PostMapping("/marketplace/publish")
    public ResponseEntity<?> publishExtension(@RequestBody Map<String, String> request) {
        UUID authorId = UUID.fromString(request.get("authorId"));
        return ResponseEntity.ok(templateService.publishExtension(
                authorId, request.get("name"), request.get("description"),
                request.get("category"), request.get("manifest")));
    }

    @PostMapping("/marketplace/{extensionId}/install")
    public ResponseEntity<Void> installExtension(@PathVariable UUID extensionId,
                                                 @RequestBody Map<String, Object> request) {
        UUID orgId = UUID.fromString((String) request.get("organizationId"));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) request.getOrDefault("config", Map.of());
        templateService.installExtension(orgId, extensionId, config);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/marketplace/{extensionId}/uninstall/{orgId}")
    public ResponseEntity<Void> uninstallExtension(@PathVariable UUID extensionId, @PathVariable UUID orgId) {
        templateService.uninstallExtension(orgId, extensionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/marketplace/installed/{orgId}")
    public ResponseEntity<?> getInstalled(@PathVariable UUID orgId) {
        return ResponseEntity.ok(templateService.getInstalledExtensions(orgId));
    }
}
