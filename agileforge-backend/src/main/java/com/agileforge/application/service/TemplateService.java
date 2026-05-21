package com.agileforge.application.service;

import com.agileforge.domain.model.IndustryTemplate;
import com.agileforge.domain.model.MarketplaceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    public record TemplateApplication(UUID projectId, String templateName, int columnsCreated,
                                      int workflowStepsCreated, int labelsCreated) {}

    public List<IndustryTemplate> getAllTemplates() {
        return List.of();
    }

    public List<IndustryTemplate> getByIndustry(String industry) {
        return List.of();
    }

    public IndustryTemplate getById(UUID templateId) {
        return null;
    }

    public TemplateApplication applyTemplate(UUID projectId, UUID templateId) {
        log.info("Applying template {} to project {}", templateId, projectId);
        return new TemplateApplication(projectId, "Template", 0, 0, 0);
    }

    public IndustryTemplate createTemplate(String name, String description, String industry,
                                           String category, String workflowDef, String icon) {
        IndustryTemplate template = new IndustryTemplate();
        template.setId(UUID.randomUUID());
        template.setName(name);
        template.setDescription(description);
        template.setIndustry(industry);
        template.setCategory(category);
        template.setWorkflowDefinition(workflowDef);
        template.setIcon(icon);
        return template;
    }

    // Marketplace
    public List<MarketplaceExtension> getPublishedExtensions(String category, int page, int size) {
        return List.of();
    }

    public MarketplaceExtension getExtensionById(UUID extensionId) {
        return null;
    }

    public MarketplaceExtension publishExtension(UUID authorId, String name, String description,
                                                 String category, String manifest) {
        log.info("Publishing extension: {}", name);
        MarketplaceExtension ext = new MarketplaceExtension();
        ext.setId(UUID.randomUUID());
        ext.setName(name);
        ext.setDescription(description);
        ext.setAuthorId(authorId);
        ext.setCategory(category);
        ext.setManifest(manifest);
        ext.setStatus("PENDING");
        return ext;
    }

    public void installExtension(UUID organizationId, UUID extensionId, Map<String, Object> config) {
        log.info("Installing extension {} for org {}", extensionId, organizationId);
    }

    public void uninstallExtension(UUID organizationId, UUID extensionId) {
        log.info("Uninstalling extension {} for org {}", extensionId, organizationId);
    }

    public List<MarketplaceExtension> getInstalledExtensions(UUID organizationId) {
        return List.of();
    }
}
