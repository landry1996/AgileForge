package com.agileforge.application.service;

import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.*;
import com.agileforge.domain.port.out.BoardColumnRepositoryPort;
import com.agileforge.domain.port.out.ProjectRepositoryPort;
import com.agileforge.domain.port.out.SprintRepositoryPort;
import com.agileforge.domain.port.out.TicketRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService Tests")
class BoardServiceTest {

    @Mock
    private BoardColumnRepositoryPort columnRepository;

    @Mock
    private TicketRepositoryPort ticketRepository;

    @Mock
    private SprintRepositoryPort sprintRepository;

    @Mock
    private ProjectRepositoryPort projectRepository;

    @InjectMocks
    private BoardService boardService;

    @Nested
    @DisplayName("Get Board")
    class GetBoardTests {

        @Test
        @DisplayName("Should return board with columns and tickets from active sprint")
        void shouldReturnBoardWithActiveSprint() {
            UUID projectId = UUID.randomUUID();
            UUID sprintId = UUID.randomUUID();

            Project project = new Project(UUID.randomUUID(), "Test Project", "TP", "desc", Project.ProjectType.SOFTWARE);
            project.setId(projectId);

            Sprint activeSprint = new Sprint(projectId, "Sprint 1", null, null, null);
            activeSprint.setId(sprintId);
            activeSprint.setStatus(Sprint.SprintStatus.ACTIVE);

            BoardColumn col1 = new BoardColumn(projectId, "To Do", TicketStatus.TODO, 0);
            BoardColumn col2 = new BoardColumn(projectId, "Done", TicketStatus.DONE, 1);

            Ticket ticket = new Ticket();
            ticket.setStatus(TicketStatus.TODO);
            ticket.setSprintId(sprintId);

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(columnRepository.findByProjectIdOrderByPosition(projectId)).thenReturn(List.of(col1, col2));
            when(sprintRepository.findActiveByProjectId(projectId)).thenReturn(Optional.of(activeSprint));
            when(ticketRepository.findBySprintId(sprintId)).thenReturn(List.of(ticket));

            BoardService.BoardData data = boardService.getBoard(projectId);

            assertThat(data.project()).isEqualTo(project);
            assertThat(data.columns()).hasSize(2);
            assertThat(data.tickets()).hasSize(1);
            assertThat(data.activeSprint()).isEqualTo(activeSprint);
        }

        @Test
        @DisplayName("Should return non-done tickets when no active sprint")
        void shouldReturnNonDoneTicketsWhenNoSprint() {
            UUID projectId = UUID.randomUUID();

            Project project = new Project(UUID.randomUUID(), "Test Project", "TP", "desc", Project.ProjectType.SOFTWARE);
            project.setId(projectId);

            BoardColumn col = new BoardColumn(projectId, "In Progress", TicketStatus.IN_PROGRESS, 0);

            Ticket doneTicket = new Ticket();
            doneTicket.setStatus(TicketStatus.DONE);

            Ticket activeTicket = new Ticket();
            activeTicket.setStatus(TicketStatus.IN_PROGRESS);

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(columnRepository.findByProjectIdOrderByPosition(projectId)).thenReturn(List.of(col));
            when(sprintRepository.findActiveByProjectId(projectId)).thenReturn(Optional.empty());
            when(ticketRepository.findByProjectId(projectId)).thenReturn(List.of(doneTicket, activeTicket));

            BoardService.BoardData data = boardService.getBoard(projectId);

            assertThat(data.tickets()).hasSize(1);
            assertThat(data.tickets().get(0).getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
            assertThat(data.activeSprint()).isNull();
        }

        @Test
        @DisplayName("Should create default columns if none exist")
        void shouldCreateDefaultColumnsIfEmpty() {
            UUID projectId = UUID.randomUUID();

            Project project = new Project(UUID.randomUUID(), "Test Project", "TP", "desc", Project.ProjectType.SOFTWARE);
            project.setId(projectId);

            List<BoardColumn> defaultColumns = List.of(
                    new BoardColumn(projectId, "Backlog", TicketStatus.BACKLOG, 0),
                    new BoardColumn(projectId, "To Do", TicketStatus.TODO, 1),
                    new BoardColumn(projectId, "In Progress", TicketStatus.IN_PROGRESS, 2),
                    new BoardColumn(projectId, "Code Review", TicketStatus.CODE_REVIEW, 3),
                    new BoardColumn(projectId, "QA", TicketStatus.QA, 4),
                    new BoardColumn(projectId, "Done", TicketStatus.DONE, 5)
            );

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(columnRepository.findByProjectIdOrderByPosition(projectId))
                    .thenReturn(List.of())
                    .thenReturn(defaultColumns);
            when(columnRepository.save(any(BoardColumn.class))).thenAnswer(i -> i.getArgument(0));
            when(sprintRepository.findActiveByProjectId(projectId)).thenReturn(Optional.empty());
            when(ticketRepository.findByProjectId(projectId)).thenReturn(List.of());

            BoardService.BoardData data = boardService.getBoard(projectId);

            assertThat(data.columns()).hasSize(6);
            verify(columnRepository, times(6)).save(any(BoardColumn.class));
        }

        @Test
        @DisplayName("Should throw when project not found")
        void shouldThrowWhenProjectNotFound() {
            UUID projectId = UUID.randomUUID();
            when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> boardService.getBoard(projectId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Column Management")
    class ColumnTests {

        @Test
        @DisplayName("Should add a column with WIP limit")
        void shouldAddColumn() {
            UUID projectId = UUID.randomUUID();
            when(columnRepository.save(any(BoardColumn.class))).thenAnswer(i -> {
                BoardColumn col = i.getArgument(0);
                col.setId(UUID.randomUUID());
                return col;
            });

            BoardColumn result = boardService.addColumn(projectId, "Testing", TicketStatus.QA, 3, 5);

            assertThat(result.getName()).isEqualTo("Testing");
            assertThat(result.getMappedStatus()).isEqualTo(TicketStatus.QA);
            assertThat(result.getPosition()).isEqualTo(3);
            assertThat(result.getWipLimit()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should remove a column")
        void shouldRemoveColumn() {
            UUID columnId = UUID.randomUUID();
            boardService.removeColumn(columnId);
            verify(columnRepository).delete(columnId);
        }
    }

    @Nested
    @DisplayName("Move Ticket")
    class MoveTicketTests {

        @Test
        @DisplayName("Should move ticket to new status")
        void shouldMoveTicket() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = new Ticket();
            ticket.setId(ticketId);
            ticket.setStatus(TicketStatus.TODO);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

            boardService.moveTicket(ticketId, TicketStatus.IN_PROGRESS);

            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("Should throw when ticket not found")
        void shouldThrowWhenTicketNotFound() {
            UUID ticketId = UUID.randomUUID();
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> boardService.moveTicket(ticketId, TicketStatus.DONE))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Backlog")
    class BacklogTests {

        @Test
        @DisplayName("Should return backlog tickets")
        void shouldReturnBacklog() {
            UUID projectId = UUID.randomUUID();
            Ticket t1 = new Ticket();
            t1.setStatus(TicketStatus.BACKLOG);
            Ticket t2 = new Ticket();
            t2.setStatus(TicketStatus.BACKLOG);

            when(ticketRepository.findByProjectIdAndStatus(projectId, TicketStatus.BACKLOG))
                    .thenReturn(List.of(t1, t2));

            List<Ticket> result = boardService.getBacklog(projectId);

            assertThat(result).hasSize(2);
        }
    }
}
