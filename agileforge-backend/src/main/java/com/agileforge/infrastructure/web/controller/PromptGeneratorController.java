package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.CreatePromptTemplateRequest;
import com.agileforge.application.dto.request.GeneratePromptRequest;
import com.agileforge.application.dto.request.RatePromptRequest;
import com.agileforge.application.dto.response.GeneratedPromptResponse;
import com.agileforge.application.dto.response.PromptTemplateResponse;
import com.agileforge.application.service.PromptGeneratorService;
import com.agileforge.domain.model.GeneratedPrompt;
import com.agileforge.domain.model.PromptTemplate;
import com.agileforge.domain.port.out.PromptTemplateRepositoryPort;
import com.agileforge.domain.port.out.UserRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Prompt Generator", description = "AI prompt generation for tickets - generates contextual prompts for Claude Code")
public class PromptGeneratorController {

    private final PromptGeneratorService promptGeneratorService;
    private final PromptTemplateRepositoryPort promptTemplateRepository;
    private final UserRepositoryPort userRepository;

    public PromptGeneratorController(PromptGeneratorService promptGeneratorService,
                                     PromptTemplateRepositoryPort promptTemplateRepository,
                                     UserRepositoryPort userRepository) {
        this.promptGeneratorService = promptGeneratorService;
        this.promptTemplateRepository = promptTemplateRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/tickets/{ticketId}/generate-prompt")
    @Operation(summary = "Generate a contextual AI prompt for a ticket")
    public ResponseEntity<GeneratedPromptResponse> generatePrompt(@PathVariable UUID ticketId,
                                                                   @RequestBody(required = false) GeneratePromptRequest request,
                                                                   Authentication auth) {
        UUID userId = getCurrentUserId(auth);
        GeneratedPrompt prompt = promptGeneratorService.generatePromptForTicket(ticketId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toGeneratedResponse(prompt));
    }

    @GetMapping("/tickets/{ticketId}/prompts")
    @Operation(summary = "Get prompt generation history for a ticket")
    public ResponseEntity<List<GeneratedPromptResponse>> getPromptHistory(@PathVariable UUID ticketId) {
        List<GeneratedPromptResponse> history = promptGeneratorService.getHistoryByTicket(ticketId).stream()
                .map(this::toGeneratedResponse).toList();
        return ResponseEntity.ok(history);
    }

    @PostMapping("/prompts/{promptId}/rate")
    @Operation(summary = "Rate a generated prompt for effectiveness")
    public ResponseEntity<GeneratedPromptResponse> ratePrompt(@PathVariable UUID promptId,
                                                               @Valid @RequestBody RatePromptRequest request) {
        GeneratedPrompt prompt = promptGeneratorService.ratePrompt(promptId, request.rating());
        return ResponseEntity.ok(toGeneratedResponse(prompt));
    }

    @GetMapping("/projects/{projectId}/prompt-templates")
    @Operation(summary = "Get all prompt templates for a project")
    public ResponseEntity<List<PromptTemplateResponse>> getProjectTemplates(@PathVariable UUID projectId) {
        List<PromptTemplateResponse> templates = promptGeneratorService.getTemplatesByProject(projectId).stream()
                .map(this::toTemplateResponse).toList();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/prompt-templates/global")
    @Operation(summary = "Get all global prompt templates")
    public ResponseEntity<List<PromptTemplateResponse>> getGlobalTemplates() {
        List<PromptTemplateResponse> templates = promptGeneratorService.getGlobalTemplates().stream()
                .map(this::toTemplateResponse).toList();
        return ResponseEntity.ok(templates);
    }

    @PostMapping("/projects/{projectId}/prompt-templates")
    @Operation(summary = "Create a custom prompt template for a project")
    public ResponseEntity<PromptTemplateResponse> createTemplate(@PathVariable UUID projectId,
                                                                  @Valid @RequestBody CreatePromptTemplateRequest request) {
        PromptTemplate template = promptGeneratorService.createTemplate(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toTemplateResponse(template));
    }

    @PutMapping("/prompt-templates/{templateId}")
    @Operation(summary = "Update an existing prompt template")
    public ResponseEntity<PromptTemplateResponse> updateTemplate(@PathVariable UUID templateId,
                                                                  @Valid @RequestBody CreatePromptTemplateRequest request) {
        PromptTemplate template = promptGeneratorService.updateTemplate(templateId, request);
        return ResponseEntity.ok(toTemplateResponse(template));
    }

    @DeleteMapping("/prompt-templates/{templateId}")
    @Operation(summary = "Delete a prompt template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        promptGeneratorService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private GeneratedPromptResponse toGeneratedResponse(GeneratedPrompt prompt) {
        String templateName = null;
        if (prompt.getTemplateId() != null) {
            templateName = promptTemplateRepository.findById(prompt.getTemplateId())
                    .map(PromptTemplate::getName)
                    .orElse(null);
        }
        return new GeneratedPromptResponse(
                prompt.getId(),
                prompt.getTicketId(),
                prompt.getPromptText(),
                templateName,
                prompt.getCreatedAt()
        );
    }

    private PromptTemplateResponse toTemplateResponse(PromptTemplate template) {
        return new PromptTemplateResponse(
                template.getId(),
                template.getProjectId(),
                template.getName(),
                template.getCategory().name(),
                template.getTemplate(),
                template.getVariables(),
                template.isGlobal(),
                template.getUsageCount(),
                template.getRating(),
                template.getCreatedAt()
        );
    }
}
