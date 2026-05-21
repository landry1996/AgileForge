package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.ApiKeyService;
import com.agileforge.application.service.SprintService;
import com.agileforge.domain.model.Sprint;
import com.agileforge.domain.port.out.TicketRepositoryPort;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SprintController.class)
@DisplayName("SprintController Integration Tests")
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SprintService sprintService;

    @MockBean
    private TicketRepositoryPort ticketRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private ApiKeyService apiKeyService;

    @Test
    @WithMockUser
    @DisplayName("POST /api/sprints/project/{id} - should create sprint")
    void shouldCreateSprint() throws Exception {
        UUID projectId = UUID.randomUUID();
        Sprint sprint = new Sprint(projectId, "Sprint 1", "Deliver MVP", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 14));
        sprint.setId(UUID.randomUUID());

        when(sprintService.create(eq(projectId), eq("Sprint 1"), eq("Deliver MVP"), eq("2026-06-01"), eq("2026-06-14"), eq(40)))
                .thenReturn(sprint);
        when(ticketRepository.findBySprintId(any())).thenReturn(List.of());

        String body = """
                {
                    "name": "Sprint 1",
                    "goal": "Deliver MVP",
                    "startDate": "2026-06-01",
                    "endDate": "2026-06-14",
                    "capacity": 40
                }
                """;

        mockMvc.perform(post("/sprints/project/" + projectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andExpect(jsonPath("$.goal").value("Deliver MVP"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/sprints/{id} - should return sprint by ID")
    void shouldGetSprintById() throws Exception {
        UUID sprintId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Sprint sprint = new Sprint(projectId, "Sprint 1", "Goal", null, null);
        sprint.setId(sprintId);

        when(sprintService.getById(sprintId)).thenReturn(sprint);
        when(ticketRepository.findBySprintId(sprintId)).thenReturn(List.of());

        mockMvc.perform(get("/sprints/" + sprintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sprint 1"))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/sprints/{id}/start - should start sprint")
    void shouldStartSprint() throws Exception {
        UUID sprintId = UUID.randomUUID();
        Sprint sprint = new Sprint(UUID.randomUUID(), "Sprint 1", null, LocalDate.now(), LocalDate.now().plusDays(14));
        sprint.setId(sprintId);
        sprint.setStatus(Sprint.SprintStatus.ACTIVE);

        when(sprintService.start(sprintId)).thenReturn(sprint);
        when(ticketRepository.findBySprintId(sprintId)).thenReturn(List.of());

        mockMvc.perform(post("/sprints/" + sprintId + "/start").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/sprints/{id}/complete - should complete sprint")
    void shouldCompleteSprint() throws Exception {
        UUID sprintId = UUID.randomUUID();
        Sprint sprint = new Sprint(UUID.randomUUID(), "Sprint 1", null, null, null);
        sprint.setId(sprintId);
        sprint.setStatus(Sprint.SprintStatus.COMPLETED);

        when(sprintService.complete(sprintId)).thenReturn(sprint);
        when(ticketRepository.findBySprintId(sprintId)).thenReturn(List.of());

        mockMvc.perform(post("/sprints/" + sprintId + "/complete").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/sprints/{id}/metrics - should return metrics")
    void shouldReturnMetrics() throws Exception {
        UUID sprintId = UUID.randomUUID();
        when(sprintService.getMetrics(sprintId))
                .thenReturn(new SprintService.SprintMetrics(10, 7, 40, 28));

        mockMvc.perform(get("/sprints/" + sprintId + "/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTickets").value(10))
                .andExpect(jsonPath("$.doneTickets").value(7))
                .andExpect(jsonPath("$.totalPoints").value(40))
                .andExpect(jsonPath("$.donePoints").value(28));
    }

    @Test
    @DisplayName("GET /api/sprints/{id} - should return 401 without auth")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/sprints/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
