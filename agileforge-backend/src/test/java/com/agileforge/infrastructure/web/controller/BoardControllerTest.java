package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.BoardService;
import com.agileforge.domain.model.*;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
@DisplayName("BoardController Integration Tests")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @SuppressWarnings("unused")
    @MockBean
    private com.agileforge.application.service.ApiKeyService apiKeyService;

    @Test
    @WithMockUser
    @DisplayName("GET /api/board/project/{id} - should return board view")
    void shouldReturnBoardView() throws Exception {
        UUID projectId = UUID.randomUUID();

        Project project = new Project(UUID.randomUUID(), "My Project", "MP", "desc", Project.ProjectType.SOFTWARE);
        project.setId(projectId);

        BoardColumn col1 = new BoardColumn(projectId, "To Do", TicketStatus.TODO, 0);
        col1.setId(UUID.randomUUID());
        BoardColumn col2 = new BoardColumn(projectId, "Done", TicketStatus.DONE, 1);
        col2.setId(UUID.randomUUID());

        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setProjectId(projectId);
        ticket.setKey("MP");
        ticket.setNumber(1);
        ticket.setTitle("Test ticket");
        ticket.setStatus(TicketStatus.TODO);
        ticket.setType(TicketType.TASK);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setReporterId(UUID.randomUUID());

        Sprint activeSprint = new Sprint(projectId, "Sprint 1", null, null, null);
        activeSprint.setId(UUID.randomUUID());

        BoardService.BoardData boardData = new BoardService.BoardData(
                project, List.of(col1, col2), List.of(ticket), activeSprint);

        when(boardService.getBoard(projectId)).thenReturn(boardData);

        mockMvc.perform(get("/board/project/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("My Project"))
                .andExpect(jsonPath("$.projectKey").value("MP"))
                .andExpect(jsonPath("$.activeSprintName").value("Sprint 1"))
                .andExpect(jsonPath("$.columns").isArray())
                .andExpect(jsonPath("$.columns[0].name").value("To Do"))
                .andExpect(jsonPath("$.columns[0].tickets[0].title").value("Test ticket"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/board/project/{id}/columns - should add column")
    void shouldAddColumn() throws Exception {
        UUID projectId = UUID.randomUUID();
        BoardColumn column = new BoardColumn(projectId, "QA", TicketStatus.QA, 3);
        column.setId(UUID.randomUUID());
        column.setWipLimit(4);

        when(boardService.addColumn(eq(projectId), eq("QA"), eq(TicketStatus.QA), eq(3), eq(4)))
                .thenReturn(column);

        String body = """
                {
                    "name": "QA",
                    "mappedStatus": "QA",
                    "position": 3,
                    "wipLimit": 4
                }
                """;

        mockMvc.perform(post("/board/project/" + projectId + "/columns")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("QA"))
                .andExpect(jsonPath("$.wipLimit").value(4));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/board/columns/{id} - should remove column")
    void shouldRemoveColumn() throws Exception {
        UUID columnId = UUID.randomUUID();

        mockMvc.perform(delete("/board/columns/" + columnId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(boardService).removeColumn(columnId);
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/board/tickets/{id}/move - should move ticket")
    void shouldMoveTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(patch("/board/tickets/" + ticketId + "/move")
                        .with(csrf())
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk());

        verify(boardService).moveTicket(ticketId, TicketStatus.IN_PROGRESS);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/board/project/{id}/backlog - should return backlog")
    void shouldReturnBacklog() throws Exception {
        UUID projectId = UUID.randomUUID();
        Ticket t = new Ticket();
        t.setId(UUID.randomUUID());
        t.setProjectId(projectId);
        t.setKey("MP");
        t.setNumber(1);
        t.setTitle("Backlog item");
        t.setStatus(TicketStatus.BACKLOG);
        t.setType(TicketType.STORY);
        t.setPriority(TicketPriority.LOW);
        t.setReporterId(UUID.randomUUID());

        when(boardService.getBacklog(projectId)).thenReturn(List.of(t));

        mockMvc.perform(get("/board/project/" + projectId + "/backlog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Backlog item"));
    }

    @Test
    @DisplayName("GET /api/board/project/{id} - should return 401 without auth")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/board/project/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
