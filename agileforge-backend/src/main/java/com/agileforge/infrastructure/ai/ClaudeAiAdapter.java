package com.agileforge.infrastructure.ai;

import com.agileforge.domain.port.out.AiAssistantPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ClaudeAiAdapter implements AiAssistantPort {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAiAdapter.class);
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public ClaudeAiAdapter(@Value("${agileforge.ai.api-key:}") String apiKey,
                           @Value("${agileforge.ai.model:claude-sonnet-4-20250514}") String model,
                           ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(ANTHROPIC_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", ANTHROPIC_VERSION)
                .build();
    }

    @Override
    public List<GeneratedTicket> generateTicketsFromDescription(String projectContext, String description) {
        String systemPrompt = """
                You are an expert agile project manager. Generate well-structured tickets from the user's description.
                Each ticket should have: title, description (with acceptance criteria), type (STORY, TASK, BUG, EPIC, SPIKE), \
                priority (CRITICAL, HIGH, MEDIUM, LOW, TRIVIAL), storyPoints (1, 2, 3, 5, 8, 13), and acceptanceCriteria.

                Respond ONLY with a JSON array of tickets. No markdown, no explanation.""";

        String userPrompt = projectContext != null
                ? "Project context: " + projectContext + "\n\nDescription: " + description
                : "Description: " + description;

        String response = callClaude(systemPrompt, userPrompt);
        return parseTicketList(response);
    }

    @Override
    public List<GeneratedTicket> generateBacklog(String projectName, String projectDescription, String projectType) {
        String systemPrompt = """
                You are an expert agile project manager and product owner. Generate a comprehensive initial backlog \
                for a new project. Include epics, stories, and technical tasks covering:
                - Core features (user-facing)
                - Technical foundation (auth, infra, CI/CD)
                - Non-functional requirements (security, performance, monitoring)
                - Documentation

                Each ticket should have: title, description (detailed with acceptance criteria), \
                type (EPIC, STORY, TASK, SPIKE), priority (CRITICAL, HIGH, MEDIUM, LOW, TRIVIAL), \
                storyPoints (1, 2, 3, 5, 8, 13), and acceptanceCriteria.

                Generate 15-25 tickets ordered by priority. Respond ONLY with a JSON array. No markdown.""";

        String userPrompt = String.format("Project: %s\nType: %s\nDescription: %s",
                projectName, projectType != null ? projectType : "SOFTWARE", projectDescription);

        String response = callClaude(systemPrompt, userPrompt);
        return parseTicketList(response);
    }

    @Override
    public QualityAnalysis analyzeTicketQuality(String title, String description, String type) {
        String systemPrompt = """
                You are a ticket quality expert. Analyze the given ticket and provide:
                - score: 0-100 quality score
                - issues: list of problems (vague title, missing acceptance criteria, too large scope, etc.)
                - suggestions: list of improvement suggestions
                - improvedTitle: a better title if applicable (or same if already good)
                - improvedDescription: an improved description with acceptance criteria

                Scoring guide:
                - 90-100: Excellent (clear, testable, well-scoped)
                - 70-89: Good (minor improvements needed)
                - 50-69: Fair (missing key details)
                - 30-49: Poor (vague, needs rewrite)
                - 0-29: Very poor (unclear intent)

                Respond ONLY with a JSON object. No markdown.""";

        String userPrompt = String.format("Title: %s\nType: %s\nDescription: %s",
                title, type != null ? type : "TASK", description != null ? description : "(no description)");

        String response = callClaude(systemPrompt, userPrompt);
        return parseQualityAnalysis(response);
    }

    @Override
    public List<GeneratedTicket> decomposeTicket(String title, String description, String type) {
        String systemPrompt = """
                You are an expert at breaking down large tickets into smaller, deliverable subtasks.
                Each subtask should be independently testable and completable within 1-3 days.

                Generate subtasks with: title, description, type (TASK or STORY), \
                priority (HIGH, MEDIUM, LOW), storyPoints (1, 2, 3, 5), and acceptanceCriteria.

                Respond ONLY with a JSON array of subtasks. No markdown.""";

        String userPrompt = String.format("Parent ticket to decompose:\nTitle: %s\nType: %s\nDescription: %s",
                title, type != null ? type : "STORY", description != null ? description : "(no description)");

        String response = callClaude(systemPrompt, userPrompt);
        return parseTicketList(response);
    }

    @Override
    public String suggestDescription(String title, String type, String projectContext) {
        String systemPrompt = """
                You are an expert technical writer for agile tickets. Given a ticket title, generate a clear \
                and detailed description including:
                - Summary (1-2 sentences)
                - Acceptance criteria (as a checklist)
                - Technical notes if relevant
                - Edge cases to consider

                Write in clear, concise language. Use markdown formatting.""";

        String userPrompt = projectContext != null
                ? String.format("Title: %s\nType: %s\nProject context: %s", title, type != null ? type : "TASK", projectContext)
                : String.format("Title: %s\nType: %s", title, type != null ? type : "TASK");

        return callClaude(systemPrompt, userPrompt);
    }

    @Override
    public AcceptanceCriteriaResult generateAcceptanceCriteria(String ticketTitle, String ticketDescription, String ticketType) {
        String systemPrompt = """
                You are an expert agile coach. Generate clear, testable acceptance criteria for the given ticket.
                Also suggest relevant test cases.

                Respond ONLY with a JSON object containing:
                - criteria: list of acceptance criteria strings (Given/When/Then or checklist format)
                - testSuggestions: list of test case suggestions

                No markdown, no explanation.""";

        String userPrompt = String.format("Title: %s\nType: %s\nDescription: %s",
                ticketTitle,
                ticketType != null ? ticketType : "STORY",
                ticketDescription != null ? ticketDescription : "(no description)");

        String response = callClaude(systemPrompt, userPrompt);
        return parseAcceptanceCriteria(response);
    }

    @Override
    public SprintReportResult generateSprintReport(String sprintName, String sprintGoal, List<String> completedTickets,
                                                   List<String> inProgressTickets, List<String> blockedTickets,
                                                   int totalPoints, int completedPoints) {
        String systemPrompt = """
                You are an expert scrum master. Generate a concise sprint report based on the sprint data.

                Respond ONLY with a JSON object containing:
                - summary: a brief overall sprint summary (2-3 sentences)
                - completedItems: list of completed work highlights
                - blockers: list of identified blockers or impediments
                - recommendations: list of actionable recommendations for the next sprint
                - velocityAssessment: a brief velocity assessment (1-2 sentences)

                No markdown, no explanation.""";

        String userPrompt = String.format("""
                Sprint: %s
                Goal: %s
                Velocity: %d/%d points completed
                Completed tickets: %s
                In progress: %s
                Blocked: %s""",
                sprintName,
                sprintGoal != null ? sprintGoal : "(no goal set)",
                completedPoints, totalPoints,
                String.join(", ", completedTickets),
                String.join(", ", inProgressTickets),
                String.join(", ", blockedTickets));

        String response = callClaude(systemPrompt, userPrompt);
        return parseSprintReport(response);
    }

    @Override
    public ChangelogResult generateChangelog(String releaseName, String version, List<String> ticketSummaries) {
        String systemPrompt = """
                You are a technical writer. Generate a well-structured changelog from the list of tickets in this release.
                Categorize each item appropriately.

                Respond ONLY with a JSON object containing:
                - version: the version string
                - date: today's date in YYYY-MM-DD format
                - features: list of new features
                - bugFixes: list of bug fixes
                - improvements: list of improvements/enhancements
                - breakingChanges: list of breaking changes (if any)

                No markdown, no explanation.""";

        String userPrompt = String.format("Release: %s\nVersion: %s\nTickets:\n%s",
                releaseName,
                version != null ? version : "unreleased",
                String.join("\n", ticketSummaries));

        String response = callClaude(systemPrompt, userPrompt);
        return parseChangelog(response);
    }

    @Override
    public String suggestAssignee(String ticketTitle, String ticketDescription, String ticketType, List<String> teamMembers) {
        String systemPrompt = """
                You are a project manager helping assign work to team members. Based on the ticket details \
                and available team members, suggest the most appropriate assignee and explain why briefly.

                Respond with a short text (2-3 sentences) suggesting who should work on this and why.""";

        String userPrompt = String.format("Title: %s\nType: %s\nDescription: %s\n\nAvailable team members: %s",
                ticketTitle,
                ticketType != null ? ticketType : "TASK",
                ticketDescription != null ? ticketDescription : "(no description)",
                String.join(", ", teamMembers));

        return callClaude(systemPrompt, userPrompt);
    }

    private AcceptanceCriteriaResult parseAcceptanceCriteria(String json) {
        try {
            String cleaned = cleanJsonResponse(json);
            return objectMapper.readValue(cleaned, AcceptanceCriteriaResult.class);
        } catch (Exception e) {
            log.error("Failed to parse acceptance criteria response: {}", e.getMessage());
            return new AcceptanceCriteriaResult(List.of(), List.of());
        }
    }

    private SprintReportResult parseSprintReport(String json) {
        try {
            String cleaned = cleanJsonResponse(json);
            return objectMapper.readValue(cleaned, SprintReportResult.class);
        } catch (Exception e) {
            log.error("Failed to parse sprint report response: {}", e.getMessage());
            return new SprintReportResult("Unable to generate report", List.of(), List.of(), List.of(), "");
        }
    }

    private ChangelogResult parseChangelog(String json) {
        try {
            String cleaned = cleanJsonResponse(json);
            return objectMapper.readValue(cleaned, ChangelogResult.class);
        } catch (Exception e) {
            log.error("Failed to parse changelog response: {}", e.getMessage());
            return new ChangelogResult("", "", List.of(), List.of(), List.of(), List.of());
        }
    }

    private String callClaude(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("AI API key not configured, returning empty response");
            return "[]";
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", 4096,
                    "system", systemPrompt,
                    "messages", List.of(Map.of("role", "user", "content", userPrompt))
            );

            String body = objectMapper.writeValueAsString(requestBody);

            String response = restClient.post()
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("content");
            if (content.isArray() && !content.isEmpty()) {
                return content.get(0).path("text").asText();
            }

            log.warn("Unexpected Claude API response structure");
            return "[]";
        } catch (Exception e) {
            log.error("Error calling Claude API: {}", e.getMessage(), e);
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    private List<GeneratedTicket> parseTicketList(String json) {
        try {
            String cleaned = cleanJsonResponse(json);
            return objectMapper.readValue(cleaned, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse AI ticket response: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private QualityAnalysis parseQualityAnalysis(String json) {
        try {
            String cleaned = cleanJsonResponse(json);
            return objectMapper.readValue(cleaned, QualityAnalysis.class);
        } catch (Exception e) {
            log.error("Failed to parse AI quality analysis: {}", e.getMessage());
            return new QualityAnalysis(0, List.of("Unable to analyze"), List.of(), null, null);
        }
    }

    private String cleanJsonResponse(String response) {
        String trimmed = response.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
