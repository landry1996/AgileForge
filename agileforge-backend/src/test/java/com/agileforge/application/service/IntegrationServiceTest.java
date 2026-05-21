package com.agileforge.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IntegrationService Tests")
class IntegrationServiceTest {

    private final IntegrationService service = new IntegrationService();

    @Test
    @DisplayName("Should configure integration")
    void shouldConfigureIntegration() {
        UUID orgId = UUID.randomUUID();
        var config = service.configureIntegration(orgId, "SLACK", Map.of("channel", "#general"));

        assertThat(config).isNotNull();
        assertThat(config.getId()).isNotNull();
        assertThat(config.getOrganizationId()).isEqualTo(orgId);
        assertThat(config.getProvider()).isEqualTo("SLACK");
        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return empty integrations list")
    void shouldReturnEmptyIntegrations() {
        assertThat(service.getByOrganization(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Should return null for unknown integration")
    void shouldReturnNullForUnknown() {
        assertThat(service.getById(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("Should enable integration without error")
    void shouldEnableIntegration() {
        service.enableIntegration(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should disable integration without error")
    void shouldDisableIntegration() {
        service.disableIntegration(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should delete integration without error")
    void shouldDeleteIntegration() {
        service.deleteIntegration(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should test connection without error")
    void shouldTestConnection() {
        service.testConnection(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should send Slack notification without error")
    void shouldSendSlackNotification() {
        var msg = new IntegrationService.SlackMessage("#general", "Hello", null, List.of());
        service.sendSlackNotification(UUID.randomUUID(), msg);
    }

    @Test
    @DisplayName("Should send Teams notification without error")
    void shouldSendTeamsNotification() {
        var msg = new IntegrationService.TeamsMessage("channel-1", "Title", "Body", "0078D4");
        service.sendTeamsNotification(UUID.randomUUID(), msg);
    }

    @Test
    @DisplayName("Should add channel mapping")
    void shouldAddChannelMapping() {
        var mapping = service.addChannelMapping(UUID.randomUUID(), UUID.randomUUID(),
                "C123", "#project", List.of("TICKET_CREATED", "SPRINT_STARTED"));

        assertThat(mapping).isNotNull();
        assertThat(mapping.channelId()).isEqualTo("C123");
        assertThat(mapping.channelName()).isEqualTo("#project");
        assertThat(mapping.events()).hasSize(2);
    }

    @Test
    @DisplayName("Should validate OAuth callback")
    void shouldValidateOAuthCallback() {
        assertThat(service.validateOAuthCallback("SLACK", "code123", "state456")).isTrue();
    }
}
