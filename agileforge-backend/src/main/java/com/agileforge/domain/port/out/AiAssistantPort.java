package com.agileforge.domain.port.out;

import java.util.List;

public interface AiAssistantPort {

    List<GeneratedTicket> generateTicketsFromDescription(String projectContext, String description);

    List<GeneratedTicket> generateBacklog(String projectName, String projectDescription, String projectType);

    QualityAnalysis analyzeTicketQuality(String title, String description, String type);

    List<GeneratedTicket> decomposeTicket(String title, String description, String type);

    String suggestDescription(String title, String type, String projectContext);

    AcceptanceCriteriaResult generateAcceptanceCriteria(String ticketTitle, String ticketDescription, String ticketType);

    SprintReportResult generateSprintReport(String sprintName, String sprintGoal, List<String> completedTickets,
                                            List<String> inProgressTickets, List<String> blockedTickets, int totalPoints, int completedPoints);

    ChangelogResult generateChangelog(String releaseName, String version, List<String> ticketSummaries);

    String suggestAssignee(String ticketTitle, String ticketDescription, String ticketType, List<String> teamMembers);

    record AcceptanceCriteriaResult(
            List<String> criteria,
            List<String> testSuggestions
    ) {}

    record SprintReportResult(
            String summary,
            List<String> completedItems,
            List<String> blockers,
            List<String> recommendations,
            String velocityAssessment
    ) {}

    record ChangelogResult(
            String version,
            String date,
            List<String> features,
            List<String> bugFixes,
            List<String> improvements,
            List<String> breakingChanges
    ) {}

    record GeneratedTicket(
            String title,
            String description,
            String type,
            String priority,
            Integer storyPoints,
            String acceptanceCriteria
    ) {}

    record QualityAnalysis(
            int score,
            List<String> issues,
            List<String> suggestions,
            String improvedTitle,
            String improvedDescription
    ) {}
}
