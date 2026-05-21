package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.AiAssistantService;
import com.agileforge.domain.port.out.AiAssistantPort.GeneratedTicket;
import com.agileforge.domain.port.out.AiAssistantPort.QualityAnalysis;
import com.agileforge.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiAssistantController.class)
@DisplayName("AiAssistantController Integration Tests")
class AiAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiAssistantService aiService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @SuppressWarnings("unused")
    @MockBean
    private com.agileforge.application.service.ApiKeyService apiKeyService;

    @Test
    @WithMockUser
    @DisplayName("POST /api/ai/generate-tickets - should generate tickets")
    void shouldGenerateTickets() throws Exception {
        List<GeneratedTicket> tickets = List.of(
                new GeneratedTicket("Setup auth", "Configure JWT", "TASK", "HIGH", 5, "- JWT works")
        );
        when(aiService.generateTickets(eq("Build authentication"), isNull()))
                .thenReturn(tickets);

        String body = """
                {
                    "description": "Build authentication"
                }
                """;

        mockMvc.perform(post("/ai/generate-tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Setup auth"))
                .andExpect(jsonPath("$[0].type").value("TASK"))
                .andExpect(jsonPath("$[0].storyPoints").value(5));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/ai/generate-backlog - should generate backlog")
    void shouldGenerateBacklog() throws Exception {
        List<GeneratedTicket> tickets = List.of(
                new GeneratedTicket("Epic: Core features", "Main features", "EPIC", "CRITICAL", 13, "- Features work"),
                new GeneratedTicket("Setup CI/CD", "GitHub Actions", "TASK", "HIGH", 3, "- Pipeline green")
        );
        when(aiService.generateBacklog(eq("MyApp"), eq("A project management tool for teams"), eq("SOFTWARE"), isNull()))
                .thenReturn(tickets);

        String body = """
                {
                    "projectName": "MyApp",
                    "projectDescription": "A project management tool for teams",
                    "projectType": "SOFTWARE"
                }
                """;

        mockMvc.perform(post("/ai/generate-backlog")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Epic: Core features"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/ai/analyze-quality - should analyze quality")
    void shouldAnalyzeQuality() throws Exception {
        QualityAnalysis analysis = new QualityAnalysis(
                82,
                List.of("Could be more specific"),
                List.of("Add acceptance criteria"),
                "Implement user login with email and password",
                "As a user, I want to login so that I can access my account..."
        );
        when(aiService.analyzeQuality(eq("Login feature"), eq("User can login"), eq("STORY")))
                .thenReturn(analysis);

        String body = """
                {
                    "title": "Login feature",
                    "description": "User can login",
                    "type": "STORY"
                }
                """;

        mockMvc.perform(post("/ai/analyze-quality")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(82))
                .andExpect(jsonPath("$.issues[0]").value("Could be more specific"))
                .andExpect(jsonPath("$.improvedTitle").value("Implement user login with email and password"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/ai/decompose - should decompose ticket")
    void shouldDecomposeTicket() throws Exception {
        List<GeneratedTicket> subtasks = List.of(
                new GeneratedTicket("Design API", "Design endpoints", "TASK", "HIGH", 2, "- API designed"),
                new GeneratedTicket("Implement logic", "Backend code", "TASK", "HIGH", 5, "- Tests pass")
        );
        when(aiService.decomposeTicket(eq("Build user management"), eq("Full CRUD for users"), eq("STORY")))
                .thenReturn(subtasks);

        String body = """
                {
                    "title": "Build user management",
                    "description": "Full CRUD for users",
                    "type": "STORY"
                }
                """;

        mockMvc.perform(post("/ai/decompose")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Design API"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/ai/suggest-description - should suggest description")
    void shouldSuggestDescription() throws Exception {
        when(aiService.suggestDescription(eq("Add email notifications"), eq("TASK"), isNull()))
                .thenReturn("## Summary\nImplement email notification system...");

        String body = """
                {
                    "title": "Add email notifications",
                    "type": "TASK"
                }
                """;

        mockMvc.perform(post("/ai/suggest-description")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedDescription").value("## Summary\nImplement email notification system..."));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/ai/generate-tickets - should return 400 when description too short")
    void shouldReturn400WhenDescriptionTooShort() throws Exception {
        String body = """
                {
                    "description": "short"
                }
                """;

        mockMvc.perform(post("/ai/generate-tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/ai/generate-tickets - should return 401 without auth")
    void shouldReturn401WithoutAuth() throws Exception {
        String body = """
                {
                    "description": "Build authentication system"
                }
                """;

        mockMvc.perform(post("/ai/generate-tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
