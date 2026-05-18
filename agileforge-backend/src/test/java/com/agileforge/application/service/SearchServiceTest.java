package com.agileforge.application.service;

import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.SearchPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchService Tests")
class SearchServiceTest {

    @Mock
    private SearchPort searchPort;

    @InjectMocks
    private SearchService searchService;

    @Test
    @DisplayName("Should search tickets with query and return results")
    void shouldSearchWithQuery() {
        UUID projectId = UUID.randomUUID();
        Ticket t1 = createTicket("PROJ-1", "Fix login bug");
        Ticket t2 = createTicket("PROJ-2", "Login page redesign");
        SearchResult<Ticket> expected = new SearchResult<>(List.of(t1, t2), 2, 0, 20);

        when(searchPort.searchTickets(projectId, "login", null, null, null, null, 0, 20))
                .thenReturn(expected);

        SearchResult<Ticket> result = searchService.searchTickets(projectId, "login", null, null, null, null, 0, 20);

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getTotalCount()).isEqualTo(2);
        verify(searchPort).searchTickets(projectId, "login", null, null, null, null, 0, 20);
    }

    @Test
    @DisplayName("Should search with all filters")
    void shouldSearchWithAllFilters() {
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        Ticket t1 = createTicket("PROJ-3", "Critical bug");
        SearchResult<Ticket> expected = new SearchResult<>(List.of(t1), 1, 0, 20);

        when(searchPort.searchTickets(projectId, "bug", "IN_PROGRESS", "BUG", "HIGH", assigneeId, 0, 20))
                .thenReturn(expected);

        SearchResult<Ticket> result = searchService.searchTickets(projectId, "bug", "IN_PROGRESS", "BUG", "HIGH", assigneeId, 0, 20);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should cap page size to 100")
    void shouldCapPageSize() {
        UUID projectId = UUID.randomUUID();
        SearchResult<Ticket> expected = new SearchResult<>(List.of(), 0, 0, 100);

        when(searchPort.searchTickets(projectId, "test", null, null, null, null, 0, 100))
                .thenReturn(expected);

        SearchResult<Ticket> result = searchService.searchTickets(projectId, "test", null, null, null, null, 0, 500);

        verify(searchPort).searchTickets(projectId, "test", null, null, null, null, 0, 100);
    }

    @Test
    @DisplayName("Should default page size to 20 when invalid")
    void shouldDefaultPageSize() {
        UUID projectId = UUID.randomUUID();
        SearchResult<Ticket> expected = new SearchResult<>(List.of(), 0, 0, 20);

        when(searchPort.searchTickets(projectId, "test", null, null, null, null, 0, 20))
                .thenReturn(expected);

        SearchResult<Ticket> result = searchService.searchTickets(projectId, "test", null, null, null, null, 0, -1);

        verify(searchPort).searchTickets(projectId, "test", null, null, null, null, 0, 20);
    }

    @Test
    @DisplayName("Should return empty result when no matches")
    void shouldReturnEmpty() {
        UUID projectId = UUID.randomUUID();
        SearchResult<Ticket> expected = new SearchResult<>(List.of(), 0, 0, 20);

        when(searchPort.searchTickets(projectId, "nonexistent", null, null, null, null, 0, 20))
                .thenReturn(expected);

        SearchResult<Ticket> result = searchService.searchTickets(projectId, "nonexistent", null, null, null, null, 0, 20);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalCount()).isZero();
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePagination() {
        UUID projectId = UUID.randomUUID();
        Ticket t1 = createTicket("PROJ-21", "Page 2 result");
        SearchResult<Ticket> expected = new SearchResult<>(List.of(t1), 25, 1, 20);

        when(searchPort.searchTickets(projectId, "result", null, null, null, null, 1, 20))
                .thenReturn(expected);

        SearchResult<Ticket> result = searchService.searchTickets(projectId, "result", null, null, null, null, 1, 20);

        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getTotalCount()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    private Ticket createTicket(String key, String title) {
        Ticket t = new Ticket();
        t.setId(UUID.randomUUID());
        t.setKey(key.split("-")[0]);
        t.setNumber(Long.parseLong(key.split("-")[1]));
        t.setTitle(title);
        t.setType(TicketType.BUG);
        t.setStatus(TicketStatus.IN_PROGRESS);
        t.setPriority(TicketPriority.HIGH);
        return t;
    }
}
