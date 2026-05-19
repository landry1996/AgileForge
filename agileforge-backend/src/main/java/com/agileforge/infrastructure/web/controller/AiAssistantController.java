package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.*;
import com.agileforge.application.dto.response.*;
import com.agileforge.application.service.AiAssistantService;
import com.agileforge.domain.port.out.AiAssistantPort.AcceptanceCriteriaResult;
import com.agileforge.domain.port.out.AiAssistantPort.ChangelogResult;
import com.agileforge.domain.port.out.AiAssistantPort.GeneratedTicket;
import com.agileforge.domain.port.out.AiAssistantPort.QualityAnalysis;
import com.agileforge.domain.port.out.AiAssistantPort.SprintReportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI Assistant", description = "AI-powered ticket generation and analysis")
public class AiAssistantController {

    private final AiAssistantService aiService;

    public AiAssistantController(AiAssistantService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate-tickets")
    @Operation(summary = "Generate tickets from a natural language description")
    public ResponseEntity<List<GeneratedTicketResponse>> generateTickets(
            @Valid @RequestBody GenerateTicketsRequest request) {
        List<GeneratedTicket> tickets = aiService.generateTickets(request.description(), request.projectContext());
        return ResponseEntity.ok(tickets.stream().map(this::toResponse).toList());
    }

    @PostMapping("/generate-backlog")
    @Operation(summary = "Generate a complete initial backlog for a project")
    public ResponseEntity<List<GeneratedTicketResponse>> generateBacklog(
            @Valid @RequestBody GenerateBacklogRequest request) {
        List<GeneratedTicket> tickets = aiService.generateBacklog(
                request.projectName(), request.projectDescription(),
                request.projectType(), request.maxTickets());
        return ResponseEntity.ok(tickets.stream().map(this::toResponse).toList());
    }

    @PostMapping("/analyze-quality")
    @Operation(summary = "Analyze ticket quality and get improvement suggestions")
    public ResponseEntity<QualityAnalysisResponse> analyzeQuality(
            @Valid @RequestBody AnalyzeQualityRequest request) {
        QualityAnalysis analysis = aiService.analyzeQuality(
                request.title(), request.description(), request.type());
        return ResponseEntity.ok(toQualityResponse(analysis));
    }

    @PostMapping("/decompose")
    @Operation(summary = "Decompose a large ticket into smaller subtasks")
    public ResponseEntity<List<GeneratedTicketResponse>> decomposeTicket(
            @Valid @RequestBody DecomposeTicketRequest request) {
        List<GeneratedTicket> subtasks = aiService.decomposeTicket(
                request.title(), request.description(), request.type());
        return ResponseEntity.ok(subtasks.stream().map(this::toResponse).toList());
    }

    @PostMapping("/suggest-description")
    @Operation(summary = "Generate a detailed description from a ticket title")
    public ResponseEntity<SuggestDescriptionResponse> suggestDescription(
            @Valid @RequestBody SuggestDescriptionRequest request) {
        String description = aiService.suggestDescription(
                request.title(), request.type(), request.projectContext());
        return ResponseEntity.ok(new SuggestDescriptionResponse(description));
    }

    @PostMapping("/generate-acceptance-criteria")
    @Operation(summary = "Generate acceptance criteria for a ticket")
    public ResponseEntity<AcceptanceCriteriaResponse> generateAcceptanceCriteria(
            @Valid @RequestBody GenerateAcceptanceCriteriaRequest request) {
        AcceptanceCriteriaResult result = aiService.generateAcceptanceCriteria(
                request.ticketTitle(), request.ticketDescription(), request.ticketType());
        return ResponseEntity.ok(new AcceptanceCriteriaResponse(result.criteria(), result.testSuggestions()));
    }

    @PostMapping("/sprint-report")
    @Operation(summary = "Generate a sprint report summary")
    public ResponseEntity<SprintReportResponse> generateSprintReport(
            @Valid @RequestBody SprintReportRequest request) {
        SprintReportResult result = aiService.generateSprintReport(request.sprintId());
        return ResponseEntity.ok(new SprintReportResponse(
                result.summary(), result.completedItems(), result.blockers(),
                result.recommendations(), result.velocityAssessment()));
    }

    @PostMapping("/changelog")
    @Operation(summary = "Generate a changelog for a release")
    public ResponseEntity<ChangelogResponse> generateChangelog(
            @Valid @RequestBody ChangelogRequest request) {
        ChangelogResult result = aiService.generateChangelog(request.releaseId());
        return ResponseEntity.ok(new ChangelogResponse(
                result.version(), result.date(), result.features(),
                result.bugFixes(), result.improvements(), result.breakingChanges()));
    }

    @PostMapping("/suggest-assignee")
    @Operation(summary = "Suggest the best assignee for a ticket")
    public ResponseEntity<SuggestDescriptionResponse> suggestAssignee(
            @RequestParam UUID ticketId,
            @RequestParam UUID projectId) {
        String suggestion = aiService.suggestAssignee(ticketId, projectId);
        return ResponseEntity.ok(new SuggestDescriptionResponse(suggestion));
    }

    private GeneratedTicketResponse toResponse(GeneratedTicket t) {
        return new GeneratedTicketResponse(
                t.title(), t.description(), t.type(),
                t.priority(), t.storyPoints(), t.acceptanceCriteria());
    }

    private QualityAnalysisResponse toQualityResponse(QualityAnalysis a) {
        return new QualityAnalysisResponse(
                a.score(), a.issues(), a.suggestions(),
                a.improvedTitle(), a.improvedDescription());
    }
}
