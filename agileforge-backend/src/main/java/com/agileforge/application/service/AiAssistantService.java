package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.port.out.AiAssistantPort;
import com.agileforge.domain.port.out.AiAssistantPort.GeneratedTicket;
import com.agileforge.domain.port.out.AiAssistantPort.QualityAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    private final AiAssistantPort aiAssistant;

    public AiAssistantService(AiAssistantPort aiAssistant) {
        this.aiAssistant = aiAssistant;
    }

    public List<GeneratedTicket> generateTickets(String description, String projectContext) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("Description is required to generate tickets");
        }

        log.info("Generating tickets from description ({} chars)", description.length());
        List<GeneratedTicket> tickets = aiAssistant.generateTicketsFromDescription(projectContext, description);
        log.info("Generated {} tickets", tickets.size());
        return tickets;
    }

    public List<GeneratedTicket> generateBacklog(String projectName, String projectDescription,
                                                  String projectType, Integer maxTickets) {
        if (projectDescription == null || projectDescription.isBlank()) {
            throw new BusinessException("Project description is required to generate backlog");
        }

        log.info("Generating backlog for project: {}", projectName);
        List<GeneratedTicket> tickets = aiAssistant.generateBacklog(projectName, projectDescription, projectType);

        if (maxTickets != null && maxTickets > 0 && tickets.size() > maxTickets) {
            tickets = tickets.subList(0, maxTickets);
        }

        log.info("Generated backlog with {} tickets", tickets.size());
        return tickets;
    }

    public QualityAnalysis analyzeQuality(String title, String description, String type) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("Title is required for quality analysis");
        }

        log.info("Analyzing ticket quality: {}", title);
        return aiAssistant.analyzeTicketQuality(title, description, type);
    }

    public List<GeneratedTicket> decomposeTicket(String title, String description, String type) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("Title is required to decompose ticket");
        }

        log.info("Decomposing ticket: {}", title);
        List<GeneratedTicket> subtasks = aiAssistant.decomposeTicket(title, description, type);
        log.info("Decomposed into {} subtasks", subtasks.size());
        return subtasks;
    }

    public String suggestDescription(String title, String type, String projectContext) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("Title is required to suggest description");
        }

        log.info("Suggesting description for: {}", title);
        return aiAssistant.suggestDescription(title, type, projectContext);
    }
}
