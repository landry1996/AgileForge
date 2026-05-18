package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.model.Sprint;
import com.agileforge.domain.model.Ticket;
import com.agileforge.domain.model.TicketStatus;
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
@DisplayName("SprintService Tests")
class SprintServiceTest {

    @Mock
    private SprintRepositoryPort sprintRepository;

    @Mock
    private TicketRepositoryPort ticketRepository;

    @InjectMocks
    private SprintService sprintService;

    @Nested
    @DisplayName("Create Sprint")
    class CreateTests {

        @Test
        @DisplayName("Should create sprint with provided name")
        void shouldCreateSprintWithName() {
            UUID projectId = UUID.randomUUID();
            when(sprintRepository.countByProjectId(projectId)).thenReturn(2L);
            when(sprintRepository.save(any(Sprint.class))).thenAnswer(i -> {
                Sprint s = i.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });

            Sprint result = sprintService.create(projectId, "Sprint Alpha", "Deliver auth", "2026-06-01", "2026-06-14", 40);

            assertThat(result.getName()).isEqualTo("Sprint Alpha");
            assertThat(result.getGoal()).isEqualTo("Deliver auth");
            assertThat(result.getCapacity()).isEqualTo(40);
            assertThat(result.getStatus()).isEqualTo(Sprint.SprintStatus.PLANNING);
            verify(sprintRepository).save(any(Sprint.class));
        }

        @Test
        @DisplayName("Should auto-generate sprint name if not provided")
        void shouldAutoGenerateName() {
            UUID projectId = UUID.randomUUID();
            when(sprintRepository.countByProjectId(projectId)).thenReturn(5L);
            when(sprintRepository.save(any(Sprint.class))).thenAnswer(i -> i.getArgument(0));

            Sprint result = sprintService.create(projectId, null, null, null, null, null);

            assertThat(result.getName()).isEqualTo("Sprint 6");
        }

        @Test
        @DisplayName("Should throw when end date is before start date")
        void shouldThrowWhenEndBeforeStart() {
            UUID projectId = UUID.randomUUID();

            assertThatThrownBy(() -> sprintService.create(projectId, "Sprint", null, "2026-06-14", "2026-06-01", null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("End date cannot be before start date");
        }
    }

    @Nested
    @DisplayName("Start Sprint")
    class StartTests {

        @Test
        @DisplayName("Should start a planning sprint")
        void shouldStartSprint() {
            UUID sprintId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            Sprint sprint = new Sprint(projectId, "Sprint 1", null, null, null);
            sprint.setId(sprintId);

            when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
            when(sprintRepository.findActiveByProjectId(projectId)).thenReturn(Optional.empty());
            when(sprintRepository.save(any(Sprint.class))).thenAnswer(i -> i.getArgument(0));

            Sprint result = sprintService.start(sprintId);

            assertThat(result.getStatus()).isEqualTo(Sprint.SprintStatus.ACTIVE);
            assertThat(result.getStartDate()).isNotNull();
            assertThat(result.getEndDate()).isNotNull();
        }

        @Test
        @DisplayName("Should throw when another sprint is active")
        void shouldThrowWhenAnotherActive() {
            UUID sprintId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            Sprint sprint = new Sprint(projectId, "Sprint 2", null, null, null);
            sprint.setId(sprintId);

            Sprint activeSprint = new Sprint(projectId, "Sprint 1", null, null, null);
            activeSprint.setStatus(Sprint.SprintStatus.ACTIVE);

            when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
            when(sprintRepository.findActiveByProjectId(projectId)).thenReturn(Optional.of(activeSprint));

            assertThatThrownBy(() -> sprintService.start(sprintId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already has an active sprint");
        }

        @Test
        @DisplayName("Should throw when sprint is not in planning status")
        void shouldThrowWhenNotPlanning() {
            UUID sprintId = UUID.randomUUID();
            Sprint sprint = new Sprint(UUID.randomUUID(), "Sprint 1", null, null, null);
            sprint.setId(sprintId);
            sprint.setStatus(Sprint.SprintStatus.COMPLETED);

            when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

            assertThatThrownBy(() -> sprintService.start(sprintId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cannot be started");
        }
    }

    @Nested
    @DisplayName("Complete Sprint")
    class CompleteTests {

        @Test
        @DisplayName("Should complete sprint and move unfinished tickets to backlog")
        void shouldCompleteAndMoveUnfinished() {
            UUID sprintId = UUID.randomUUID();
            Sprint sprint = new Sprint(UUID.randomUUID(), "Sprint 1", null, null, null);
            sprint.setId(sprintId);
            sprint.setStatus(Sprint.SprintStatus.ACTIVE);

            Ticket doneTicket = createTicket(sprintId, TicketStatus.DONE);
            Ticket inProgressTicket = createTicket(sprintId, TicketStatus.IN_PROGRESS);

            when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
            when(sprintRepository.save(any(Sprint.class))).thenAnswer(i -> i.getArgument(0));
            when(ticketRepository.findBySprintId(sprintId)).thenReturn(List.of(doneTicket, inProgressTicket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

            Sprint result = sprintService.complete(sprintId);

            assertThat(result.getStatus()).isEqualTo(Sprint.SprintStatus.COMPLETED);
            verify(ticketRepository).save(inProgressTicket);
            assertThat(inProgressTicket.getSprintId()).isNull();
            assertThat(inProgressTicket.getStatus()).isEqualTo(TicketStatus.BACKLOG);
        }

        @Test
        @DisplayName("Should throw when sprint is not active")
        void shouldThrowWhenNotActive() {
            UUID sprintId = UUID.randomUUID();
            Sprint sprint = new Sprint(UUID.randomUUID(), "Sprint 1", null, null, null);
            sprint.setId(sprintId);

            when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

            assertThatThrownBy(() -> sprintService.complete(sprintId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("cannot be completed");
        }
    }

    @Nested
    @DisplayName("Add/Remove Tickets")
    class TicketManagementTests {

        @Test
        @DisplayName("Should add ticket to sprint and transition from BACKLOG to TODO")
        void shouldAddTicketAndTransition() {
            UUID sprintId = UUID.randomUUID();
            UUID ticketId = UUID.randomUUID();
            Sprint sprint = new Sprint(UUID.randomUUID(), "Sprint 1", null, null, null);
            sprint.setId(sprintId);
            sprint.setStatus(Sprint.SprintStatus.ACTIVE);

            Ticket ticket = createTicket(null, TicketStatus.BACKLOG);
            ticket.setId(ticketId);

            when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));
            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

            sprintService.addTicketToSprint(sprintId, ticketId);

            assertThat(ticket.getSprintId()).isEqualTo(sprintId);
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.TODO);
        }

        @Test
        @DisplayName("Should throw when adding ticket to completed sprint")
        void shouldThrowWhenAddingToCompleted() {
            UUID sprintId = UUID.randomUUID();
            Sprint sprint = new Sprint(UUID.randomUUID(), "Sprint 1", null, null, null);
            sprint.setId(sprintId);
            sprint.setStatus(Sprint.SprintStatus.COMPLETED);

            when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(sprint));

            assertThatThrownBy(() -> sprintService.addTicketToSprint(sprintId, UUID.randomUUID()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot add tickets");
        }

        @Test
        @DisplayName("Should remove ticket from sprint")
        void shouldRemoveTicketFromSprint() {
            UUID sprintId = UUID.randomUUID();
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = createTicket(sprintId, TicketStatus.TODO);
            ticket.setId(ticketId);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

            sprintService.removeTicketFromSprint(sprintId, ticketId);

            assertThat(ticket.getSprintId()).isNull();
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.BACKLOG);
        }

        @Test
        @DisplayName("Should throw when ticket not in specified sprint")
        void shouldThrowWhenTicketNotInSprint() {
            UUID sprintId = UUID.randomUUID();
            UUID otherSprintId = UUID.randomUUID();
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = createTicket(otherSprintId, TicketStatus.TODO);
            ticket.setId(ticketId);

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> sprintService.removeTicketFromSprint(sprintId, ticketId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Ticket is not in this sprint");
        }
    }

    @Nested
    @DisplayName("Metrics")
    class MetricsTests {

        @Test
        @DisplayName("Should compute sprint metrics correctly")
        void shouldComputeMetrics() {
            UUID sprintId = UUID.randomUUID();
            Ticket t1 = createTicket(sprintId, TicketStatus.DONE);
            t1.setStoryPoints(5);
            Ticket t2 = createTicket(sprintId, TicketStatus.IN_PROGRESS);
            t2.setStoryPoints(3);
            Ticket t3 = createTicket(sprintId, TicketStatus.DONE);
            t3.setStoryPoints(8);

            when(ticketRepository.findBySprintId(sprintId)).thenReturn(List.of(t1, t2, t3));

            SprintService.SprintMetrics metrics = sprintService.getMetrics(sprintId);

            assertThat(metrics.totalTickets()).isEqualTo(3);
            assertThat(metrics.doneTickets()).isEqualTo(2);
            assertThat(metrics.totalPoints()).isEqualTo(16);
            assertThat(metrics.donePoints()).isEqualTo(13);
        }
    }

    private Ticket createTicket(UUID sprintId, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setSprintId(sprintId);
        ticket.setStatus(status);
        return ticket;
    }
}
