package com.agileforge.application.service;

import com.agileforge.application.dto.request.CreatePromptTemplateRequest;
import com.agileforge.application.dto.request.GeneratePromptRequest;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.GeneratedPrompt;
import com.agileforge.domain.model.Project;
import com.agileforge.domain.model.PromptCategory;
import com.agileforge.domain.model.PromptTemplate;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketLink;
import com.agileforge.domain.model.TicketType;
import com.agileforge.domain.port.out.GeneratedPromptRepositoryPort;
import com.agileforge.domain.port.out.ProjectRepositoryPort;
import com.agileforge.domain.port.out.PromptTemplateRepositoryPort;
import com.agileforge.domain.port.out.TicketLinkRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromptGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PromptGeneratorService.class);

    private final PromptTemplateRepositoryPort promptTemplateRepository;
    private final GeneratedPromptRepositoryPort generatedPromptRepository;
    private final TicketRepositoryPort ticketRepository;
    private final ProjectRepositoryPort projectRepository;
    private final TicketLinkRepositoryPort ticketLinkRepository;

    public PromptGeneratorService(PromptTemplateRepositoryPort promptTemplateRepository,
                                  GeneratedPromptRepositoryPort generatedPromptRepository,
                                  TicketRepositoryPort ticketRepository,
                                  ProjectRepositoryPort projectRepository,
                                  TicketLinkRepositoryPort ticketLinkRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
        this.generatedPromptRepository = generatedPromptRepository;
        this.ticketRepository = ticketRepository;
        this.projectRepository = projectRepository;
        this.ticketLinkRepository = ticketLinkRepository;
    }

    /**
     * Main method: generates a contextual prompt for a ticket ready to use with Claude Code.
     */
    public GeneratedPrompt generatePromptForTicket(UUID ticketId, GeneratePromptRequest request, UUID userId) {
        // 1. Get the ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket", ticketId));

        // 2. Get project context
        Project project = projectRepository.findById(ticket.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project", ticket.getProjectId()));

        // 3. Get linked tickets (dependencies, parent epic)
        List<TicketLink> links = ticketLinkRepository.findAllByTicketId(ticketId);
        Ticket epicTicket = null;
        if (ticket.getEpicId() != null) {
            epicTicket = ticketRepository.findById(ticket.getEpicId()).orElse(null);
        }

        // 4. Choose or use provided template
        PromptTemplate template = null;
        if (request != null && request.templateId() != null) {
            template = promptTemplateRepository.findById(request.templateId())
                    .orElseThrow(() -> new EntityNotFoundException("PromptTemplate", request.templateId()));
        } else {
            template = findBestTemplate(ticket, project.getId());
        }

        // 5. Build the prompt
        String promptText = buildPrompt(ticket, project, links, epicTicket, template,
                request != null ? request.customInstructions() : null);

        // 6. Increment usage count if template was used
        if (template != null && template.getId() != null) {
            promptTemplateRepository.incrementUsageCount(template.getId());
        }

        // 7. Save to history
        GeneratedPrompt generatedPrompt = new GeneratedPrompt(
                ticketId,
                template != null ? template.getId() : null,
                promptText,
                userId
        );
        GeneratedPrompt saved = generatedPromptRepository.save(generatedPrompt);

        log.info("Prompt generated for ticket {} by user {}", ticket.getFullKey(), userId);
        return saved;
    }

    /**
     * Generate a prompt using a specific template.
     */
    public GeneratedPrompt generateFromTemplate(UUID ticketId, UUID templateId, UUID userId) {
        GeneratePromptRequest request = new GeneratePromptRequest(templateId, null);
        return generatePromptForTicket(ticketId, request, userId);
    }

    /**
     * Get all templates for a project.
     */
    @Transactional(readOnly = true)
    public List<PromptTemplate> getTemplatesByProject(UUID projectId) {
        return promptTemplateRepository.findByProjectId(projectId);
    }

    /**
     * Get all global templates.
     */
    @Transactional(readOnly = true)
    public List<PromptTemplate> getGlobalTemplates() {
        return promptTemplateRepository.findGlobal();
    }

    /**
     * Create a custom template for a project.
     */
    public PromptTemplate createTemplate(UUID projectId, CreatePromptTemplateRequest request) {
        PromptCategory category = PromptCategory.valueOf(request.category().toUpperCase());

        PromptTemplate template = new PromptTemplate(
                projectId,
                request.name(),
                category,
                request.template(),
                request.variables()
        );
        return promptTemplateRepository.save(template);
    }

    /**
     * Update an existing template.
     */
    public PromptTemplate updateTemplate(UUID templateId, CreatePromptTemplateRequest request) {
        PromptTemplate template = promptTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("PromptTemplate", templateId));

        template.setName(request.name());
        template.setCategory(PromptCategory.valueOf(request.category().toUpperCase()));
        template.setTemplate(request.template());
        template.setVariables(request.variables());

        return promptTemplateRepository.save(template);
    }

    /**
     * Delete a template.
     */
    public void deleteTemplate(UUID templateId) {
        promptTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("PromptTemplate", templateId));
        promptTemplateRepository.delete(templateId);
    }

    /**
     * Get prompt generation history for a ticket.
     */
    @Transactional(readOnly = true)
    public List<GeneratedPrompt> getHistoryByTicket(UUID ticketId) {
        return generatedPromptRepository.findByTicketId(ticketId);
    }

    /**
     * Rate a generated prompt for effectiveness feedback.
     */
    public GeneratedPrompt ratePrompt(UUID promptId, int rating) {
        GeneratedPrompt prompt = generatedPromptRepository.findById(promptId)
                .orElseThrow(() -> new EntityNotFoundException("GeneratedPrompt", promptId));
        prompt.setRating(rating);
        return generatedPromptRepository.save(prompt);
    }

    /**
     * Returns built-in default templates for each category.
     * Used when no project-specific template exists.
     */
    public List<PromptTemplate> getDefaultTemplates() {
        List<PromptTemplate> defaults = new ArrayList<>();

        defaults.add(createDefaultTemplate("Spring Boot Backend Implementation", PromptCategory.BACKEND,
                """
                You are a Senior Backend Developer specialized in Spring Boot and Java.
                Implement ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Architecture: Hexagonal Architecture (Ports & Adapters)
                - Tech stack: Spring Boot 3.x, Java 21, PostgreSQL, Flyway

                ## Task Description
                {description}

                ## Acceptance Criteria
                {criteria}

                ## Constraints
                - Follow hexagonal architecture: domain models, ports (in/out), adapters
                - Domain models use explicit getters/setters (no Lombok)
                - JPA entities use Lombok (@Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor)
                - DTOs are Java records
                - Use constructor injection (no @Autowired)
                - Include proper validation with Jakarta Bean Validation
                - Write Flyway migration if DB changes are needed

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Implementation code following the architecture
                - Unit tests with JUnit 5 and Mockito
                - Integration test if applicable
                - Flyway migration script if needed
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("Angular Frontend Implementation", PromptCategory.FRONTEND,
                """
                You are a Senior Frontend Developer specialized in Angular and TypeScript.
                Implement ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Framework: Angular 17+ with standalone components
                - Tech stack: TypeScript, RxJS, Angular Material, SCSS

                ## Task Description
                {description}

                ## Acceptance Criteria
                {criteria}

                ## Constraints
                - Use standalone components (no NgModules)
                - Follow reactive patterns with RxJS
                - Implement proper error handling
                - Use Angular signals where appropriate
                - Follow accessibility best practices (WCAG 2.1 AA)
                - Responsive design (mobile-first)

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Component implementation with template and styles
                - Service for API communication if needed
                - Unit tests with Jasmine/Karma
                - Proper TypeScript typing (no 'any')
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("JUnit Testing Implementation", PromptCategory.TESTING,
                """
                You are a Senior QA Engineer specialized in automated testing with JUnit 5.
                Write tests for ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Test framework: JUnit 5, Mockito, AssertJ
                - Architecture: Hexagonal (test each layer independently)

                ## What to Test
                {description}

                ## Acceptance Criteria to Verify
                {criteria}

                ## Testing Strategy
                - Unit tests for domain logic (no mocking needed)
                - Unit tests for services (mock ports/adapters)
                - Integration tests for adapters (use @DataJpaTest, TestContainers)
                - API tests for controllers (use @WebMvcTest)

                ## Constraints
                - Use AssertJ for assertions
                - Use @DisplayName for readable test names
                - Follow Given/When/Then pattern
                - Cover edge cases and error scenarios
                - Aim for >80% line coverage on new code

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Unit test classes
                - Integration test classes if applicable
                - Test fixtures/builders if complex objects are needed
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("Bug Fix Implementation", PromptCategory.BUG_FIX,
                """
                You are a Senior Developer tasked with fixing a bug.
                Fix ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Architecture: Hexagonal Architecture

                ## Bug Description
                {description}

                ## Steps to Reproduce / Expected vs Actual
                {criteria}

                ## Debugging Strategy
                1. Identify the root cause (not just symptoms)
                2. Write a failing test that reproduces the bug
                3. Implement the minimal fix
                4. Verify the test passes
                5. Check for regression in related areas

                ## Constraints
                - Minimal change principle: fix the bug without refactoring unrelated code
                - Add regression test(s)
                - Document the root cause in a code comment if non-obvious

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Root cause analysis (brief comment)
                - Bug fix implementation
                - Regression test(s)
                - Verify no side effects on linked tickets
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("Code Refactoring", PromptCategory.REFACTORING,
                """
                You are a Senior Developer performing a refactoring.
                Refactor for ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Architecture: Hexagonal Architecture

                ## Refactoring Objective
                {description}

                ## Success Criteria
                {criteria}

                ## Refactoring Principles
                - Preserve existing behavior (all tests must still pass)
                - Apply SOLID principles
                - Reduce complexity and improve readability
                - Extract reusable abstractions where appropriate
                - Improve testability

                ## Constraints
                - Do NOT change public API contracts unless explicitly stated
                - Commit in small incremental steps
                - Run tests after each step

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Refactored code
                - Updated/new unit tests if structure changed
                - Brief explanation of refactoring decisions
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("DevOps Docker/K8s Implementation", PromptCategory.DEVOPS,
                """
                You are a Senior DevOps Engineer.
                Implement ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Infrastructure: Docker, Kubernetes, CI/CD pipelines
                - Cloud: AWS/GCP/Azure (adapt as needed)

                ## Task Description
                {description}

                ## Acceptance Criteria
                {criteria}

                ## Constraints
                - Follow 12-factor app principles
                - Use multi-stage Docker builds for minimal images
                - Kubernetes manifests should be production-ready (resource limits, health checks, security contexts)
                - Include proper logging and monitoring configuration
                - Secrets management via K8s secrets or external vault

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Dockerfile / docker-compose changes
                - Kubernetes manifests (deployment, service, configmap, etc.)
                - CI/CD pipeline updates if needed
                - Documentation for deployment procedures
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("Security Implementation", PromptCategory.SECURITY,
                """
                You are a Senior Security Engineer.
                Implement security task {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Security framework: Spring Security, JWT

                ## Task Description
                {description}

                ## Acceptance Criteria
                {criteria}

                ## Security Constraints
                - Follow OWASP Top 10 guidelines
                - Input validation on all endpoints
                - Proper authentication and authorization checks
                - No sensitive data in logs
                - Use parameterized queries (prevent SQL injection)
                - Apply principle of least privilege

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Security implementation
                - Security-focused tests
                - Updated security documentation if needed
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("Performance Optimization", PromptCategory.PERFORMANCE,
                """
                You are a Senior Performance Engineer.
                Optimize for ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Stack: Spring Boot, PostgreSQL, JPA/Hibernate

                ## Performance Issue
                {description}

                ## Target Metrics
                {criteria}

                ## Optimization Strategy
                1. Measure current performance (baseline)
                2. Identify bottlenecks (profiling, query analysis)
                3. Apply targeted optimizations
                4. Measure improvement

                ## Common Areas to Check
                - N+1 queries (use JOIN FETCH or @EntityGraph)
                - Missing database indexes
                - Unnecessary data loading (projections, pagination)
                - Caching opportunities (Spring Cache, Redis)
                - Connection pool sizing

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Performance fix implementation
                - Before/after metrics
                - Load test if applicable
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("Documentation", PromptCategory.DOCUMENTATION,
                """
                You are a Senior Technical Writer.
                Document for ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}

                ## Documentation Task
                {description}

                ## Requirements
                {criteria}

                ## Documentation Standards
                - Clear and concise language
                - Include code examples where appropriate
                - Use diagrams for architecture/flow explanations
                - Follow existing documentation structure
                - Include API examples with request/response samples

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Documentation files (Markdown)
                - API documentation updates (OpenAPI/Swagger annotations)
                - Architecture Decision Records (ADRs) if applicable
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("Code Review Checklist", PromptCategory.CODE_REVIEW,
                """
                You are a Senior Developer performing a code review.
                Review changes for ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Architecture: Hexagonal Architecture

                ## What Changed
                {description}

                ## Review Checklist
                {criteria}

                ## Review Focus Areas
                - Architecture compliance (hexagonal layers respected)
                - SOLID principles adherence
                - Error handling completeness
                - Security implications
                - Performance considerations
                - Test coverage and quality
                - Code readability and naming
                - API contract consistency

                ## Related Context
                {relatedContext}

                ## Expected Output
                - List of findings (critical, major, minor, suggestion)
                - Specific improvement recommendations with code examples
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        defaults.add(createDefaultTemplate("Data Migration", PromptCategory.MIGRATION,
                """
                You are a Senior Database Engineer.
                Implement migration for ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}
                - Database: PostgreSQL
                - Migration tool: Flyway

                ## Migration Task
                {description}

                ## Requirements
                {criteria}

                ## Migration Constraints
                - Migrations must be backward-compatible (zero-downtime deployment)
                - Use transactions for data migrations
                - Include rollback strategy
                - Test with production-like data volumes
                - Add appropriate indexes for new columns/tables

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Flyway migration script (V{version}__{description}.sql)
                - Data migration script if needed
                - Updated JPA entities
                - Rollback script
                """,
                "ticketKey,title,projectName,description,criteria,relatedContext"));

        return defaults;
    }

    // --- Private helper methods ---

    private PromptTemplate findBestTemplate(Ticket ticket, UUID projectId) {
        PromptCategory category = mapTicketTypeToCategory(ticket.getType());

        // First, try project-specific templates for the category
        List<PromptTemplate> projectTemplates = promptTemplateRepository.findByProjectId(projectId);
        for (PromptTemplate t : projectTemplates) {
            if (t.getCategory() == category) {
                return t;
            }
        }

        // Then, try global templates for the category
        List<PromptTemplate> globalTemplates = promptTemplateRepository.findGlobal();
        for (PromptTemplate t : globalTemplates) {
            if (t.getCategory() == category) {
                return t;
            }
        }

        // Fall back to built-in default templates
        List<PromptTemplate> defaults = getDefaultTemplates();
        for (PromptTemplate t : defaults) {
            if (t.getCategory() == category) {
                return t;
            }
        }

        // Ultimate fallback: use backend template
        return defaults.get(0);
    }

    private PromptCategory mapTicketTypeToCategory(TicketType type) {
        return switch (type) {
            case BUG -> PromptCategory.BUG_FIX;
            case SECURITY_ISSUE -> PromptCategory.SECURITY;
            case PERFORMANCE_ISSUE -> PromptCategory.PERFORMANCE;
            case DOCUMENTATION -> PromptCategory.DOCUMENTATION;
            case TECHNICAL_DEBT -> PromptCategory.REFACTORING;
            case DATA_MIGRATION -> PromptCategory.MIGRATION;
            case UX_IMPROVEMENT -> PromptCategory.FRONTEND;
            default -> PromptCategory.BACKEND;
        };
    }

    private String buildPrompt(Ticket ticket, Project project, List<TicketLink> links,
                               Ticket epicTicket, PromptTemplate template, String customInstructions) {
        String templateText;
        if (template != null && template.getTemplate() != null) {
            templateText = template.getTemplate();
        } else {
            templateText = buildDefaultPromptStructure();
        }

        // Build related context
        String relatedContext = buildRelatedContext(ticket, links, epicTicket);

        // Replace variables
        String prompt = templateText
                .replace("{ticketKey}", ticket.getFullKey())
                .replace("{title}", ticket.getTitle() != null ? ticket.getTitle() : "")
                .replace("{projectName}", project.getName() != null ? project.getName() : "")
                .replace("{description}", ticket.getDescription() != null ? ticket.getDescription() : "No description provided")
                .replace("{criteria}", extractAcceptanceCriteria(ticket))
                .replace("{relatedContext}", relatedContext);

        // Append custom instructions if provided
        if (customInstructions != null && !customInstructions.isBlank()) {
            prompt += "\n\n## Additional Instructions\n" + customInstructions;
        }

        return prompt;
    }

    private String buildDefaultPromptStructure() {
        return """
                You are a Senior Developer.
                Implement ticket {ticketKey}: {title}

                ## Context
                - Project: {projectName}

                ## Task Description
                {description}

                ## Acceptance Criteria
                {criteria}

                ## Related Context
                {relatedContext}

                ## Expected Deliverables
                - Implementation code
                - Unit tests
                - Updated documentation if needed
                """;
    }

    private String buildRelatedContext(Ticket ticket, List<TicketLink> links, Ticket epicTicket) {
        StringBuilder context = new StringBuilder();

        if (epicTicket != null) {
            context.append("- Epic: ").append(epicTicket.getFullKey())
                    .append(" - ").append(epicTicket.getTitle()).append("\n");
        }

        if (!links.isEmpty()) {
            Map<String, List<TicketLink>> groupedLinks = links.stream()
                    .collect(Collectors.groupingBy(l -> l.getLinkType().name()));

            for (Map.Entry<String, List<TicketLink>> entry : groupedLinks.entrySet()) {
                context.append("- ").append(formatLinkType(entry.getKey())).append(": ");
                List<String> linkedKeys = entry.getValue().stream()
                        .map(link -> {
                            UUID linkedId = link.getSourceTicketId().equals(ticket.getId())
                                    ? link.getTargetTicketId() : link.getSourceTicketId();
                            return ticketRepository.findById(linkedId)
                                    .map(Ticket::getFullKey)
                                    .orElse(linkedId.toString());
                        })
                        .toList();
                context.append(String.join(", ", linkedKeys)).append("\n");
            }
        }

        if (ticket.getComponent() != null && !ticket.getComponent().isEmpty()) {
            context.append("- Component: ").append(ticket.getComponent()).append("\n");
        }

        if (ticket.getLabels() != null && !ticket.getLabels().isEmpty()) {
            context.append("- Labels: ").append(ticket.getLabels()).append("\n");
        }

        if (ticket.getEnvironment() != null && !ticket.getEnvironment().isEmpty()) {
            context.append("- Environment: ").append(ticket.getEnvironment()).append("\n");
        }

        return context.length() > 0 ? context.toString() : "No additional context available.";
    }

    private String extractAcceptanceCriteria(Ticket ticket) {
        // Acceptance criteria is typically embedded in the description
        // Try to extract it from the description if formatted
        String desc = ticket.getDescription();
        if (desc == null || desc.isEmpty()) {
            return "Not specified - implement based on the title and description.";
        }

        // Look for common patterns like "Acceptance Criteria:", "AC:", "Given/When/Then"
        String lowerDesc = desc.toLowerCase();
        int acIndex = lowerDesc.indexOf("acceptance criteria");
        if (acIndex == -1) acIndex = lowerDesc.indexOf("criteria:");
        if (acIndex == -1) acIndex = lowerDesc.indexOf("given ");

        if (acIndex >= 0) {
            return desc.substring(acIndex);
        }

        return "Derive acceptance criteria from the task description above.";
    }

    private String formatLinkType(String linkType) {
        return switch (linkType) {
            case "BLOCKS" -> "Blocks";
            case "IS_BLOCKED_BY" -> "Blocked by";
            case "RELATES_TO" -> "Related to";
            case "DUPLICATES" -> "Duplicates";
            case "IS_DUPLICATED_BY" -> "Duplicated by";
            case "IS_PARENT_OF" -> "Parent of";
            case "IS_CHILD_OF" -> "Child of";
            default -> linkType.replace("_", " ").toLowerCase();
        };
    }

    private PromptTemplate createDefaultTemplate(String name, PromptCategory category,
                                                  String template, String variables) {
        PromptTemplate t = new PromptTemplate();
        t.setName(name);
        t.setCategory(category);
        t.setTemplate(template);
        t.setVariables(variables);
        t.setGlobal(true);
        t.setUsageCount(0);
        t.setRating(0);
        return t;
    }
}
