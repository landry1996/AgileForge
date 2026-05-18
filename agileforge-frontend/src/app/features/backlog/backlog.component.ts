import { Component, inject, OnInit, signal } from '@angular/core';
import { TicketService } from '../../core/services/ticket.service';
import { Ticket } from '../../core/models/ticket.model';

@Component({
  selector: 'app-backlog',
  standalone: true,
  template: `
    <div class="backlog-page">
      <div class="backlog-header">
        <h1>Backlog</h1>
        <button class="btn-create" (click)="showCreateForm = true">+ Create Ticket</button>
      </div>

      <div class="ticket-list">
        @for (ticket of tickets(); track ticket.id) {
          <div class="ticket-row">
            <span class="ticket-type type-{{ ticket.type?.toLowerCase() }}">{{ ticket.type }}</span>
            <span class="ticket-key">{{ ticket.fullKey }}</span>
            <span class="ticket-title">{{ ticket.title }}</span>
            <span class="ticket-priority priority-{{ ticket.priority?.toLowerCase() }}">{{ ticket.priority }}</span>
            <span class="ticket-status">{{ ticket.status }}</span>
            @if (ticket.storyPoints) {
              <span class="ticket-points">{{ ticket.storyPoints }}</span>
            }
          </div>
        } @empty {
          <p class="empty">No tickets in backlog. Create one or use AI to generate.</p>
        }
      </div>
    </div>
  `,
  styles: [`
    .backlog-page { max-width: 1200px; }
    .backlog-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #e1e4e8; font-size: 1.4rem; margin: 0; }
    .btn-create {
      background: #238636; color: white; border: none; padding: 8px 16px;
      border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 600;
    }
    .btn-create:hover { background: #2ea043; }
    .ticket-list { display: flex; flex-direction: column; gap: 2px; }
    .ticket-row {
      display: flex; align-items: center; gap: 12px; padding: 12px 16px;
      background: #161b22; border: 1px solid #21262d; border-radius: 6px;
    }
    .ticket-row:hover { border-color: #30363d; }
    .ticket-type { font-size: 0.7rem; padding: 2px 6px; border-radius: 3px; font-weight: 600; min-width: 60px; text-align: center; }
    .type-bug { background: #f8514922; color: #f85149; }
    .type-story { background: #3fb95022; color: #3fb950; }
    .type-task { background: #58a6ff22; color: #58a6ff; }
    .type-epic { background: #a371f722; color: #a371f7; }
    .type-spike { background: #d2992222; color: #d29922; }
    .ticket-key { font-size: 0.8rem; color: #58a6ff; font-weight: 600; min-width: 70px; }
    .ticket-title { flex: 1; color: #e1e4e8; font-size: 0.9rem; }
    .ticket-priority { font-size: 0.7rem; padding: 2px 6px; border-radius: 3px; }
    .priority-critical { background: #f8514922; color: #f85149; }
    .priority-high { background: #d2992222; color: #d29922; }
    .priority-medium { background: #58a6ff22; color: #58a6ff; }
    .priority-low { background: #3fb95022; color: #3fb950; }
    .priority-trivial { background: #8b949e22; color: #8b949e; }
    .ticket-status { font-size: 0.75rem; color: #8b949e; min-width: 80px; }
    .ticket-points { background: #21262d; color: #8b949e; padding: 2px 8px; border-radius: 10px; font-size: 0.75rem; }
    .empty { color: #484f58; font-size: 0.9rem; text-align: center; padding: 40px; }
  `]
})
export class BacklogComponent implements OnInit {
  private ticketService = inject(TicketService);
  tickets = signal<Ticket[]>([]);
  showCreateForm = false;

  ngOnInit(): void {
    this.ticketService.getMyTickets().subscribe({
      next: (tickets) => this.tickets.set(tickets),
      error: () => {}
    });
  }
}
