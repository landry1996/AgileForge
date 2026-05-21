package com.agileforge.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JiraImportService Tests")
class JiraImportServiceTest {

    private final JiraImportService service = new JiraImportService();

    @Test
    @DisplayName("Should preview Jira import")
    void shouldPreviewImport() {
        var preview = service.previewImport("https://test.atlassian.net", "PROJ", "token", "user@test.com");

        assertThat(preview).isNotNull();
        assertThat(preview.totalIssues()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return null for unknown import status")
    void shouldReturnNullForUnknownStatus() {
        assertThat(service.getImportStatus(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("Should return empty import history")
    void shouldReturnEmptyHistory() {
        assertThat(service.getImportHistory(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Should cancel import without error")
    void shouldCancelImport() {
        service.cancelImport(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should return default mapping")
    void shouldReturnDefaultMapping() {
        var mapping = service.getDefaultMapping();

        assertThat(mapping).isNotNull();
        assertThat(mapping.statusMapping()).containsEntry("To Do", "TODO");
        assertThat(mapping.typeMapping()).containsEntry("Story", "STORY");
        assertThat(mapping.priorityMapping()).containsEntry("High", "HIGH");
        assertThat(mapping.fieldMapping()).containsEntry("summary", "title");
    }

    @Test
    @DisplayName("Should start import without error")
    void shouldStartImport() {
        service.startImport(UUID.randomUUID(), UUID.randomUUID(),
                "https://test.atlassian.net", "PROJ", "token", "user@test.com",
                service.getDefaultMapping());
    }
}
