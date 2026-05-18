package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.port.out.AiAssistantPort;
import com.agileforge.domain.port.out.AiAssistantPort.GeneratedTicket;
import com.agileforge.domain.port.out.AiAssistantPort.QualityAnalysis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiAssistantService Tests")
class AiAssistantServiceTest {

    @Mock
    private AiAssistantPort aiAssistant;

    @InjectMocks
    private AiAssistantService aiAssistantService;

    @Nested
    @DisplayName("Generate Tickets")
    class GenerateTicketsTests {

        @Test
        @DisplayName("Should generate tickets from description")
        void shouldGenerateTickets() {
            List<GeneratedTicket> expected = List.of(
                    new GeneratedTicket("Setup CI/CD pipeline", "Configure GitHub Actions", "TASK", "HIGH", 5, "- Pipeline runs on push"),
                    new GeneratedTicket("User login", "Implement JWT auth", "STORY", "CRITICAL", 8, "- User can login with email/password")
            );
            when(aiAssistant.generateTicketsFromDescription("context", "Build an auth system"))
                    .thenReturn(expected);

            List<GeneratedTicket> result = aiAssistantService.generateTickets("Build an auth system", "context");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).title()).isEqualTo("Setup CI/CD pipeline");
            verify(aiAssistant).generateTicketsFromDescription("context", "Build an auth system");
        }

        @Test
        @DisplayName("Should throw when description is blank")
        void shouldThrowWhenDescriptionBlank() {
            assertThatThrownBy(() -> aiAssistantService.generateTickets("", null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Description is required to generate tickets");

            verify(aiAssistant, never()).generateTicketsFromDescription(any(), any());
        }

        @Test
        @DisplayName("Should throw when description is null")
        void shouldThrowWhenDescriptionNull() {
            assertThatThrownBy(() -> aiAssistantService.generateTickets(null, null))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("Generate Backlog")
    class GenerateBacklogTests {

        @Test
        @DisplayName("Should generate backlog for project")
        void shouldGenerateBacklog() {
            List<GeneratedTicket> expected = List.of(
                    new GeneratedTicket("Epic: Authentication", "Full auth system", "EPIC", "CRITICAL", 13, "- Auth works"),
                    new GeneratedTicket("Setup database", "Configure PostgreSQL", "TASK", "HIGH", 3, "- DB accessible")
            );
            when(aiAssistant.generateBacklog("MyApp", "A todo app", "SOFTWARE"))
                    .thenReturn(expected);

            List<GeneratedTicket> result = aiAssistantService.generateBacklog("MyApp", "A todo app", "SOFTWARE", null);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should limit tickets when maxTickets specified")
        void shouldLimitTickets() {
            List<GeneratedTicket> generated = List.of(
                    new GeneratedTicket("T1", "D1", "TASK", "HIGH", 3, "AC1"),
                    new GeneratedTicket("T2", "D2", "TASK", "MEDIUM", 2, "AC2"),
                    new GeneratedTicket("T3", "D3", "STORY", "LOW", 5, "AC3")
            );
            when(aiAssistant.generateBacklog("App", "Description", "SOFTWARE"))
                    .thenReturn(generated);

            List<GeneratedTicket> result = aiAssistantService.generateBacklog("App", "Description", "SOFTWARE", 2);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should throw when description is blank")
        void shouldThrowWhenDescBlank() {
            assertThatThrownBy(() -> aiAssistantService.generateBacklog("App", "", "SOFTWARE", null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Project description is required to generate backlog");
        }
    }

    @Nested
    @DisplayName("Analyze Quality")
    class AnalyzeQualityTests {

        @Test
        @DisplayName("Should analyze ticket quality")
        void shouldAnalyzeQuality() {
            QualityAnalysis expected = new QualityAnalysis(
                    75,
                    List.of("Missing acceptance criteria"),
                    List.of("Add specific test cases"),
                    "Implement user registration with email validation",
                    "As a user, I want to register..."
            );
            when(aiAssistant.analyzeTicketQuality("Implement registration", "Basic registration", "STORY"))
                    .thenReturn(expected);

            QualityAnalysis result = aiAssistantService.analyzeQuality("Implement registration", "Basic registration", "STORY");

            assertThat(result.score()).isEqualTo(75);
            assertThat(result.issues()).hasSize(1);
            assertThat(result.suggestions()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw when title is blank")
        void shouldThrowWhenTitleBlank() {
            assertThatThrownBy(() -> aiAssistantService.analyzeQuality("", null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Title is required for quality analysis");
        }
    }

    @Nested
    @DisplayName("Decompose Ticket")
    class DecomposeTests {

        @Test
        @DisplayName("Should decompose ticket into subtasks")
        void shouldDecompose() {
            List<GeneratedTicket> subtasks = List.of(
                    new GeneratedTicket("Design API schema", "Design REST endpoints", "TASK", "HIGH", 2, "- Endpoints documented"),
                    new GeneratedTicket("Implement backend", "Code the endpoints", "TASK", "HIGH", 5, "- Tests pass"),
                    new GeneratedTicket("Write tests", "Unit + integration tests", "TASK", "MEDIUM", 3, "- 80% coverage")
            );
            when(aiAssistant.decomposeTicket("Build REST API", "Full API for users", "STORY"))
                    .thenReturn(subtasks);

            List<GeneratedTicket> result = aiAssistantService.decomposeTicket("Build REST API", "Full API for users", "STORY");

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should throw when title is blank")
        void shouldThrowWhenTitleBlank() {
            assertThatThrownBy(() -> aiAssistantService.decomposeTicket("  ", null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Title is required to decompose ticket");
        }
    }

    @Nested
    @DisplayName("Suggest Description")
    class SuggestDescriptionTests {

        @Test
        @DisplayName("Should suggest description from title")
        void shouldSuggestDescription() {
            when(aiAssistant.suggestDescription("Add password reset", "STORY", "Auth module"))
                    .thenReturn("## Summary\nImplement password reset flow...");

            String result = aiAssistantService.suggestDescription("Add password reset", "STORY", "Auth module");

            assertThat(result).contains("password reset");
        }

        @Test
        @DisplayName("Should throw when title is blank")
        void shouldThrowWhenTitleBlank() {
            assertThatThrownBy(() -> aiAssistantService.suggestDescription("", null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Title is required to suggest description");
        }
    }
}
