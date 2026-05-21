package com.agileforge.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiAgentService Tests")
class AiAgentServiceTest {

    @Mock
    private AiAssistantService aiAssistantService;

    @InjectMocks
    private AiAgentService service;

    @Test
    @DisplayName("Should return empty agents list")
    void shouldReturnEmptyAgents() {
        assertThat(service.getAvailableAgents()).isEmpty();
    }

    @Test
    @DisplayName("Should return null for unknown agent")
    void shouldReturnNullForUnknownAgent() {
        assertThat(service.getAgentById(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("Should suggest sprint plan with defaults")
    void shouldSuggestSprintPlan() {
        var plan = service.suggestSprintPlan(UUID.randomUUID(), UUID.randomUUID());

        assertThat(plan).isNotNull();
        assertThat(plan.suggestedTickets()).isEmpty();
        assertThat(plan.reasoning()).isEqualTo("Analysis pending");
    }

    @Test
    @DisplayName("Should analyze project risks")
    void shouldAnalyzeProjectRisks() {
        var report = service.analyzeProjectRisks(UUID.randomUUID());

        assertThat(report).isNotNull();
        assertThat(report.overallRisk()).isEqualTo("UNKNOWN");
        assertThat(report.recommendations()).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate documentation")
    void shouldGenerateDocumentation() {
        var doc = service.generateDocumentation(UUID.randomUUID(), "GENERAL");
        assertThat(doc).contains("pending");
    }

    @Test
    @DisplayName("Should generate retrospective")
    void shouldGenerateRetrospective() {
        var retro = service.generateRetrospective(UUID.randomUUID());
        assertThat(retro).contains("pending");
    }

    @Test
    @DisplayName("Should return empty task history")
    void shouldReturnEmptyTaskHistory() {
        assertThat(service.getTaskHistory(UUID.randomUUID(), 0, 20)).isEmpty();
    }

    @Test
    @DisplayName("Should return null for unknown task result")
    void shouldReturnNullForUnknownTask() {
        assertThat(service.getTaskResult(UUID.randomUUID())).isNull();
    }

    @Test
    @DisplayName("Should execute agent task without error")
    void shouldExecuteAgentTask() {
        service.executeAgentTask(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "SPRINT_PLANNING", Map.of());
    }
}
