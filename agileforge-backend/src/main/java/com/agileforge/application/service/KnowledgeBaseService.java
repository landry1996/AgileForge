package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreateKnowledgeEntryRequest;
import com.agileforge.application.dto.request.UpdateKnowledgeEntryRequest;
import com.agileforge.application.dto.response.ProjectContextResponse;
import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.model.KnowledgeCategory;
import com.agileforge.domain.model.KnowledgeEntry;
import com.agileforge.domain.model.Project;
import com.agileforge.domain.port.out.KnowledgeEntryRepositoryPort;
import com.agileforge.domain.port.out.ProjectRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);

    private final KnowledgeEntryRepositoryPort knowledgeRepository;
    private final ProjectRepositoryPort projectRepository;

    public KnowledgeBaseService(KnowledgeEntryRepositoryPort knowledgeRepository,
                                ProjectRepositoryPort projectRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.projectRepository = projectRepository;
    }

    public KnowledgeEntry create(UUID projectId, UUID userId, CreateKnowledgeEntryRequest request) {
        KnowledgeCategory category;
        try {
            category = KnowledgeCategory.valueOf(request.category());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid knowledge category: " + request.category());
        }

        KnowledgeEntry entry = new KnowledgeEntry(
                projectId, category, request.title(), request.content(), request.tags(), userId
        );

        log.info("Creating knowledge entry '{}' for project {}", request.title(), projectId);
        return knowledgeRepository.save(entry);
    }

    public List<KnowledgeEntry> getByProject(UUID projectId) {
        return knowledgeRepository.findByProjectId(projectId);
    }

    public List<KnowledgeEntry> getByCategory(UUID projectId, KnowledgeCategory category) {
        return knowledgeRepository.findByProjectIdAndCategory(projectId, category);
    }

    public KnowledgeEntry update(UUID entryId, UpdateKnowledgeEntryRequest request) {
        KnowledgeEntry entry = knowledgeRepository.findById(entryId)
                .orElseThrow(() -> new BusinessException("Knowledge entry not found: " + entryId));

        if (request.title() != null) {
            entry.setTitle(request.title());
        }
        if (request.content() != null) {
            entry.setContent(request.content());
        }
        if (request.tags() != null) {
            entry.setTags(request.tags());
        }
        if (request.isActive() != null) {
            entry.setActive(request.isActive());
        }
        entry.setUpdatedAt(Instant.now());

        log.info("Updating knowledge entry {}", entryId);
        return knowledgeRepository.save(entry);
    }

    public void delete(UUID entryId) {
        log.info("Deleting knowledge entry {}", entryId);
        knowledgeRepository.delete(entryId);
    }

    public ProjectContextResponse getProjectContext(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("Project not found: " + projectId));

        List<KnowledgeEntry> entries = knowledgeRepository.findByProjectId(projectId);

        List<String> techStack = entries.stream()
                .filter(e -> e.getCategory() == KnowledgeCategory.TECH_STACK)
                .map(KnowledgeEntry::getContent)
                .toList();

        String architecture = entries.stream()
                .filter(e -> e.getCategory() == KnowledgeCategory.ARCHITECTURE)
                .map(KnowledgeEntry::getContent)
                .collect(Collectors.joining("\n"));

        List<String> conventions = entries.stream()
                .filter(e -> e.getCategory() == KnowledgeCategory.CONVENTIONS)
                .map(KnowledgeEntry::getContent)
                .toList();

        List<String> decisions = entries.stream()
                .filter(e -> e.getCategory() == KnowledgeCategory.DECISIONS)
                .map(KnowledgeEntry::getContent)
                .toList();

        List<String> knownIssues = entries.stream()
                .filter(e -> e.getCategory() == KnowledgeCategory.KNOWN_ISSUES)
                .map(KnowledgeEntry::getContent)
                .toList();

        return new ProjectContextResponse(
                projectId, project.getName(), techStack, architecture, conventions, decisions, knownIssues
        );
    }

    public String getContextForPrompt(UUID projectId) {
        List<KnowledgeEntry> entries = knowledgeRepository.findByProjectId(projectId);

        if (entries.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Project Knowledge Base ===\n\n");

        for (KnowledgeCategory category : KnowledgeCategory.values()) {
            List<KnowledgeEntry> categoryEntries = entries.stream()
                    .filter(e -> e.getCategory() == category)
                    .toList();

            if (!categoryEntries.isEmpty()) {
                sb.append("## ").append(category.name().replace("_", " ")).append("\n");
                for (KnowledgeEntry entry : categoryEntries) {
                    sb.append("- ").append(entry.getTitle()).append(": ").append(entry.getContent()).append("\n");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
