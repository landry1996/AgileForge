package com.agileforge.domain.port.out;

import java.util.List;

public interface AiAssistantPort {

    List<GeneratedTicket> generateTicketsFromDescription(String projectContext, String description);

    List<GeneratedTicket> generateBacklog(String projectName, String projectDescription, String projectType);

    QualityAnalysis analyzeTicketQuality(String title, String description, String type);

    List<GeneratedTicket> decomposeTicket(String title, String description, String type);

    String suggestDescription(String title, String type, String projectContext);

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
