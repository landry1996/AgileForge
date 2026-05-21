package com.agileforge.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TemplateService Tests")
class TemplateServiceTest {

    private final TemplateService service = new TemplateService();

    @Test
    @DisplayName("Should return empty templates list")
    void shouldReturnEmptyTemplates() {
        assertThat(service.getAllTemplates()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty templates by industry")
    void shouldReturnEmptyByIndustry() {
        assertThat(service.getByIndustry("SOFTWARE")).isEmpty();
    }

    @Test
    @DisplayName("Should return null for unknown template")
    void shouldReturnNullForUnknown() {
        assertThat(service.getById(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("Should apply template")
    void shouldApplyTemplate() {
        UUID projectId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        var result = service.applyTemplate(projectId, templateId);

        assertThat(result).isNotNull();
        assertThat(result.projectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("Should create template")
    void shouldCreateTemplate() {
        var template = service.createTemplate("Test Template", "Description",
                "SOFTWARE", "Agile", "{}", "code");

        assertThat(template).isNotNull();
        assertThat(template.getId()).isNotNull();
        assertThat(template.getName()).isEqualTo("Test Template");
        assertThat(template.getIndustry()).isEqualTo("SOFTWARE");
    }

    @Test
    @DisplayName("Should return empty marketplace extensions")
    void shouldReturnEmptyExtensions() {
        assertThat(service.getPublishedExtensions(null, 0, 20)).isEmpty();
    }

    @Test
    @DisplayName("Should return null for unknown extension")
    void shouldReturnNullForUnknownExtension() {
        assertThat(service.getExtensionById(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("Should publish extension")
    void shouldPublishExtension() {
        UUID authorId = UUID.randomUUID();
        var ext = service.publishExtension(authorId, "My Plugin", "Description",
                "INTEGRATION", "{}");

        assertThat(ext).isNotNull();
        assertThat(ext.getId()).isNotNull();
        assertThat(ext.getName()).isEqualTo("My Plugin");
        assertThat(ext.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Should install extension without error")
    void shouldInstallExtension() {
        service.installExtension(UUID.randomUUID(), UUID.randomUUID(), Map.of());
    }

    @Test
    @DisplayName("Should uninstall extension without error")
    void shouldUninstallExtension() {
        service.uninstallExtension(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    @DisplayName("Should return empty installed extensions")
    void shouldReturnEmptyInstalled() {
        assertThat(service.getInstalledExtensions(UUID.randomUUID())).isEmpty();
    }
}
