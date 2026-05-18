package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.SearchService;
import com.agileforge.domain.model.*;
import com.agileforge.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
@DisplayName("SearchController Integration Tests")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    @DisplayName("GET /search/tickets - should return search results")
    void shouldReturnSearchResults() throws Exception {
        UUID projectId = UUID.randomUUID();
        Ticket t1 = createTicket("PROJ", 1, "Fix login bug");
        SearchResult<Ticket> result = new SearchResult<>(List.of(t1), 1, 0, 20);

        when(searchService.searchTickets(eq(projectId), eq("login"), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(result);

        mockMvc.perform(get("/search/tickets")
                        .param("projectId", projectId.toString())
                        .param("q", "login")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Fix login bug"))
                .andExpect(jsonPath("$.items[0].fullKey").value("PROJ-1"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /search/tickets - should search with status filter")
    void shouldSearchWithStatusFilter() throws Exception {
        Ticket t1 = createTicket("PROJ", 2, "Bug in progress");
        SearchResult<Ticket> result = new SearchResult<>(List.of(t1), 1, 0, 20);

        when(searchService.searchTickets(isNull(), eq("bug"), eq("IN_PROGRESS"), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(result);

        mockMvc.perform(get("/search/tickets")
                        .param("q", "bug")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Bug in progress"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /search/tickets - should return empty results")
    void shouldReturnEmptyResults() throws Exception {
        SearchResult<Ticket> result = new SearchResult<>(List.of(), 0, 0, 20);

        when(searchService.searchTickets(isNull(), eq("nonexistent"), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(result);

        mockMvc.perform(get("/search/tickets").param("q", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /search/tickets - should handle pagination")
    void shouldHandlePagination() throws Exception {
        Ticket t1 = createTicket("PROJ", 21, "Page 2 item");
        SearchResult<Ticket> result = new SearchResult<>(List.of(t1), 25, 1, 20);

        when(searchService.searchTickets(isNull(), eq("item"), isNull(), isNull(), isNull(), isNull(), eq(1), eq(20)))
                .thenReturn(result);

        mockMvc.perform(get("/search/tickets")
                        .param("q", "item")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("GET /search/tickets - should return 401 when unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/search/tickets").param("q", "test"))
                .andExpect(status().isUnauthorized());
    }

    private Ticket createTicket(String key, long number, String title) {
        Ticket t = new Ticket();
        t.setId(UUID.randomUUID());
        t.setProjectId(UUID.randomUUID());
        t.setKey(key);
        t.setNumber(number);
        t.setTitle(title);
        t.setType(TicketType.BUG);
        t.setStatus(TicketStatus.IN_PROGRESS);
        t.setPriority(TicketPriority.HIGH);
        return t;
    }
}
