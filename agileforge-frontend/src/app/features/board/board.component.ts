import { Component, inject, OnInit, signal } from '@angular/core';
import { BoardService } from '../../core/services/board.service';
import { Board, BoardColumn } from '../../core/models/board.model';
import { Ticket } from '../../core/models/ticket.model';

@Component({
  selector: 'app-board',
  standalone: true,
  template: `
    <div class="board-page">
      <div class="board-header">
        <h1>{{ board()?.projectName || 'Board' }}</h1>
        @if (board()?.activeSprintName) {
          <span class="sprint-badge">{{ board()?.activeSprintName }}</span>
        }
      </div>

      @if (board()) {
        <div class="board-columns">
          @for (column of board()!.columns; track column.id) {
            <div class="board-column">
              <div class="column-header">
                <span class="column-name">{{ column.name }}</span>
                <span class="column-count">{{ column.tickets.length }}</span>
              </div>
              <div class="column-body"
                   (dragover)="onDragOver($event)"
                   (drop)="onDrop($event, column)">
                @for (ticket of column.tickets; track ticket.id) {
                  <div class="ticket-card"
                       draggable="true"
                       (dragstart)="onDragStart($event, ticket)">
                    <div class="ticket-key">{{ ticket.fullKey }}</div>
                    <div class="ticket-title">{{ ticket.title }}</div>
                    <div class="ticket-meta">
                      <span class="ticket-priority priority-{{ ticket.priority?.toLowerCase() }}">
                        {{ ticket.priority }}
                      </span>
                      @if (ticket.storyPoints) {
                        <span class="ticket-points">{{ ticket.storyPoints }}sp</span>
                      }
                    </div>
                  </div>
                }
              </div>
            </div>
          }
        </div>
      } @else {
        <p class="placeholder">Select a project to view the board. Configure project ID in the URL.</p>
      }
    </div>
  `,
  styles: [`
    .board-page { height: calc(100vh - 48px); display: flex; flex-direction: column; }
    .board-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
    h1 { color: #e1e4e8; font-size: 1.4rem; margin: 0; }
    .sprint-badge { background: #1f6feb; color: white; padding: 4px 10px; border-radius: 12px; font-size: 0.8rem; }
    .board-columns { display: flex; gap: 12px; overflow-x: auto; flex: 1; padding-bottom: 12px; }
    .board-column {
      min-width: 280px; max-width: 280px; background: #161b22; border-radius: 8px;
      display: flex; flex-direction: column; border: 1px solid #21262d;
    }
    .column-header {
      padding: 12px 16px; display: flex; justify-content: space-between; align-items: center;
      border-bottom: 1px solid #21262d;
    }
    .column-name { font-weight: 600; color: #e1e4e8; font-size: 0.9rem; }
    .column-count { background: #30363d; color: #8b949e; padding: 2px 8px; border-radius: 10px; font-size: 0.75rem; }
    .column-body { padding: 8px; flex: 1; min-height: 100px; overflow-y: auto; }
    .ticket-card {
      background: #0d1117; border: 1px solid #30363d; border-radius: 6px; padding: 12px;
      margin-bottom: 8px; cursor: grab; transition: border-color 0.2s;
    }
    .ticket-card:hover { border-color: #58a6ff; }
    .ticket-key { font-size: 0.75rem; color: #58a6ff; font-weight: 600; margin-bottom: 4px; }
    .ticket-title { font-size: 0.85rem; color: #e1e4e8; margin-bottom: 8px; }
    .ticket-meta { display: flex; gap: 8px; align-items: center; }
    .ticket-priority { font-size: 0.7rem; padding: 2px 6px; border-radius: 3px; font-weight: 600; }
    .priority-critical { background: #f8514922; color: #f85149; }
    .priority-high { background: #d2992222; color: #d29922; }
    .priority-medium { background: #58a6ff22; color: #58a6ff; }
    .priority-low { background: #3fb95022; color: #3fb950; }
    .priority-trivial { background: #8b949e22; color: #8b949e; }
    .ticket-points { font-size: 0.7rem; color: #8b949e; background: #21262d; padding: 2px 6px; border-radius: 3px; }
    .placeholder { color: #484f58; }
  `]
})
export class BoardComponent implements OnInit {
  private boardService = inject(BoardService);
  board = signal<Board | null>(null);
  private draggedTicket: Ticket | null = null;

  private projectId = 'demo';

  ngOnInit(): void {
    this.loadBoard();
  }

  loadBoard(): void {
    this.boardService.getBoard(this.projectId).subscribe({
      next: (board) => this.board.set(board),
      error: () => {}
    });
  }

  onDragStart(event: DragEvent, ticket: Ticket): void {
    this.draggedTicket = ticket;
    event.dataTransfer?.setData('text/plain', ticket.id);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  onDrop(event: DragEvent, column: BoardColumn): void {
    event.preventDefault();
    if (this.draggedTicket && this.draggedTicket.status !== column.mappedStatus) {
      this.boardService.moveTicket(this.draggedTicket.id, column.mappedStatus).subscribe({
        next: () => this.loadBoard()
      });
    }
    this.draggedTicket = null;
  }
}
