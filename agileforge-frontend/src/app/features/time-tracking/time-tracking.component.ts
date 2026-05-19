import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TimeTrackingService } from '../../core/services/time-tracking.service';
import { TimeEntry, CreateTimeEntryRequest } from '../../core/models/time-tracking.model';
import { TicketService } from '../../core/services/ticket.service';
import { Ticket } from '../../core/models/ticket.model';

interface GroupedEntries {
  date: string;
  entries: TimeEntry[];
  totalHours: number;
}

interface TicketTimeSummary {
  ticketId: string;
  ticketKey: string;
  ticketTitle: string;
  totalLogged: number;
  estimatedHours: number;
}

@Component({
  selector: 'app-time-tracking',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="time-tracking-page">
      <div class="page-header">
        <h1>Time Tracking</h1>
      </div>

      <!-- Weekly Summary -->
      <div class="weekly-summary-card">
        <div class="weekly-total">
          <span class="total-hours">{{ weeklyTotalHours() }}</span>
          <span class="total-label">hours this week</span>
        </div>
        <div class="weekly-meta">
          <span class="week-range">Week of {{ currentWeekStart() }}</span>
        </div>
      </div>

      <!-- Log Time Form -->
      <div class="log-form-card">
        <h2>Log Time</h2>
        <form class="log-form" (submit)="logTime($event)">
          <div class="form-row">
            <div class="form-group">
              <label for="ticket">Ticket</label>
              <select id="ticket" [(ngModel)]="formTicketId" name="ticketId" required>
                <option value="">Select a ticket...</option>
                @for (ticket of tickets(); track ticket.id) {
                  <option [value]="ticket.id">{{ ticket.fullKey }} - {{ ticket.title }}</option>
                }
              </select>
            </div>
            <div class="form-group form-group-small">
              <label for="hours">Hours</label>
              <input id="hours" type="number" [(ngModel)]="formHours" name="hours"
                     min="0.25" max="24" step="0.25" required placeholder="0.0">
            </div>
            <div class="form-group form-group-small">
              <label for="date">Date</label>
              <input id="date" type="date" [(ngModel)]="formDate" name="date" required>
            </div>
          </div>
          <div class="form-group">
            <label for="description">Description</label>
            <input id="description" type="text" [(ngModel)]="formDescription" name="description"
                   placeholder="What did you work on?">
          </div>
          <div class="form-actions">
            <button type="submit" class="btn-primary" [disabled]="submitting()">
              @if (submitting()) { Logging... } @else { Log Time }
            </button>
          </div>
        </form>
      </div>

      <!-- Per-ticket Summary -->
      @if (ticketSummaries().length > 0) {
        <div class="section-card">
          <h2>Per-Ticket Summary</h2>
          <div class="ticket-summaries">
            @for (summary of ticketSummaries(); track summary.ticketId) {
              <div class="summary-row">
                <div class="summary-ticket">
                  <span class="ticket-key">{{ summary.ticketKey }}</span>
                  <span class="ticket-title">{{ summary.ticketTitle }}</span>
                </div>
                <div class="summary-progress">
                  <div class="progress-bar">
                    <div class="progress-fill" [style.width.%]="getProgressPercent(summary)"></div>
                  </div>
                  <span class="progress-text">
                    {{ summary.totalLogged }}h / {{ summary.estimatedHours || '?' }}h
                  </span>
                </div>
              </div>
            }
          </div>
        </div>
      }

      <!-- Recent Time Entries -->
      @if (loading()) {
        <div class="loading-state">Loading time entries...</div>
      } @else if (error()) {
        <div class="error-state">
          <p>{{ error() }}</p>
          <button class="btn-secondary" (click)="loadData()">Retry</button>
        </div>
      } @else {
        <div class="section-card">
          <h2>Recent Entries</h2>
          @if (groupedEntries().length === 0) {
            <p class="placeholder">No time entries logged yet.</p>
          } @else {
            @for (group of groupedEntries(); track group.date) {
              <div class="date-group">
                <div class="date-header">
                  <span class="date-label">{{ group.date }}</span>
                  <span class="date-total">{{ group.totalHours }}h</span>
                </div>
                <table class="entries-table">
                  <tbody>
                    @for (entry of group.entries; track entry.id) {
                      <tr class="entry-row">
                        <td class="entry-ticket">{{ getTicketKey(entry.ticketId) }}</td>
                        <td class="entry-desc">{{ entry.description || '-' }}</td>
                        <td class="entry-hours">{{ entry.hours }}h</td>
                        <td class="entry-actions">
                          <button class="btn-delete" (click)="confirmDelete(entry)"
                                  title="Delete entry">
                            x
                          </button>
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            }
          }
        </div>
      }

      <!-- Delete Confirmation Modal -->
      @if (entryToDelete()) {
        <div class="modal-overlay" (click)="cancelDelete()">
          <div class="modal-content" (click)="$event.stopPropagation()">
            <h3>Delete Time Entry</h3>
            <p>Are you sure you want to delete this time entry?</p>
            <p class="delete-detail">
              {{ getTicketKey(entryToDelete()!.ticketId) }} - {{ entryToDelete()!.hours }}h on {{ entryToDelete()!.workDate }}
            </p>
            <div class="modal-actions">
              <button class="btn-secondary" (click)="cancelDelete()">Cancel</button>
              <button class="btn-danger" (click)="deleteEntry()">Delete</button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .time-tracking-page { max-width: 1000px; }
    .page-header { margin-bottom: 24px; }
    .page-header h1 { color: #e1e4e8; font-size: 1.6rem; margin: 0; }

    .weekly-summary-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px;
      padding: 24px; margin-bottom: 24px; text-align: center;
    }
    .weekly-total { display: flex; align-items: baseline; justify-content: center; gap: 8px; }
    .total-hours { font-size: 3rem; font-weight: 700; color: #58a6ff; }
    .total-label { font-size: 1rem; color: #8b949e; }
    .weekly-meta { margin-top: 8px; }
    .week-range { font-size: 0.85rem; color: #484f58; }

    .log-form-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px;
      padding: 20px; margin-bottom: 24px;
    }
    .log-form-card h2 { color: #e1e4e8; font-size: 1.1rem; margin: 0 0 16px 0; }
    .log-form .form-row { display: flex; gap: 12px; margin-bottom: 12px; }
    .form-group { flex: 1; display: flex; flex-direction: column; gap: 4px; }
    .form-group-small { max-width: 140px; }
    .form-group label { font-size: 0.8rem; color: #8b949e; }
    .form-group input, .form-group select {
      background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      color: #c9d1d9; padding: 8px 12px; font-size: 0.9rem;
    }
    .form-group input:focus, .form-group select:focus {
      outline: none; border-color: #58a6ff;
    }
    .form-actions { display: flex; justify-content: flex-end; margin-top: 12px; }
    .btn-primary {
      background: #238636; border: none; color: white; padding: 8px 16px;
      border-radius: 6px; cursor: pointer; font-size: 0.9rem; font-weight: 500;
    }
    .btn-primary:hover { background: #2ea043; }
    .btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
    .btn-secondary {
      background: #21262d; border: 1px solid #30363d; color: #c9d1d9;
      padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 0.9rem;
    }
    .btn-secondary:hover { background: #30363d; }
    .btn-danger {
      background: #da3633; border: none; color: white; padding: 8px 16px;
      border-radius: 6px; cursor: pointer; font-size: 0.9rem;
    }
    .btn-danger:hover { background: #f85149; }

    .section-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px;
      padding: 20px; margin-bottom: 24px;
    }
    .section-card h2 { color: #e1e4e8; font-size: 1.1rem; margin: 0 0 16px 0; }

    .ticket-summaries { display: flex; flex-direction: column; gap: 12px; }
    .summary-row { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
    .summary-ticket { display: flex; align-items: center; gap: 8px; flex: 1; min-width: 0; }
    .ticket-key { font-size: 0.8rem; color: #58a6ff; font-weight: 600; white-space: nowrap; }
    .ticket-title { font-size: 0.85rem; color: #c9d1d9; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .summary-progress { display: flex; align-items: center; gap: 8px; min-width: 200px; }
    .progress-bar {
      flex: 1; height: 6px; background: #21262d; border-radius: 3px; overflow: hidden;
    }
    .progress-fill { height: 100%; background: #238636; border-radius: 3px; transition: width 0.3s; }
    .progress-text { font-size: 0.75rem; color: #8b949e; white-space: nowrap; }

    .date-group { margin-bottom: 16px; }
    .date-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 8px 0; border-bottom: 1px solid #21262d; margin-bottom: 8px;
    }
    .date-label { font-size: 0.85rem; color: #e1e4e8; font-weight: 600; }
    .date-total { font-size: 0.8rem; color: #58a6ff; font-weight: 600; }
    .entries-table { width: 100%; border-collapse: collapse; }
    .entry-row td { padding: 8px 8px; font-size: 0.85rem; color: #c9d1d9; }
    .entry-ticket { color: #58a6ff !important; font-weight: 500; width: 100px; }
    .entry-desc { color: #8b949e !important; }
    .entry-hours { text-align: right; font-weight: 600; width: 60px; }
    .entry-actions { width: 40px; text-align: right; }
    .btn-delete {
      background: none; border: 1px solid #30363d; color: #8b949e;
      width: 24px; height: 24px; border-radius: 4px; cursor: pointer;
      font-size: 0.75rem; display: inline-flex; align-items: center; justify-content: center;
    }
    .btn-delete:hover { color: #f85149; border-color: #f85149; }

    .modal-overlay {
      position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex;
      align-items: center; justify-content: center; z-index: 1000;
    }
    .modal-content {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px;
      padding: 24px; max-width: 400px; width: 100%;
    }
    .modal-content h3 { color: #e1e4e8; margin: 0 0 12px 0; }
    .modal-content p { color: #8b949e; font-size: 0.9rem; }
    .delete-detail { color: #c9d1d9 !important; font-weight: 500; }
    .modal-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 20px; }

    .loading-state, .error-state { color: #8b949e; padding: 40px; text-align: center; }
    .error-state p { color: #f85149; }
    .placeholder { color: #484f58; font-size: 0.9rem; }
  `]
})
export class TimeTrackingComponent implements OnInit {
  private timeTrackingService = inject(TimeTrackingService);
  private ticketService = inject(TicketService);

  // State
  loading = signal(false);
  error = signal<string | null>(null);
  submitting = signal(false);
  entries = signal<TimeEntry[]>([]);
  tickets = signal<Ticket[]>([]);
  ticketSummaries = signal<TicketTimeSummary[]>([]);
  entryToDelete = signal<TimeEntry | null>(null);

  // Ticket lookup map
  private ticketMap = new Map<string, Ticket>();

  // Form state
  formTicketId = '';
  formHours = 0;
  formDescription = '';
  formDate = new Date().toISOString().split('T')[0];

  // Computed
  weeklyTotalHours = computed(() => {
    const now = new Date();
    const weekStart = new Date(now);
    weekStart.setDate(now.getDate() - now.getDay());
    weekStart.setHours(0, 0, 0, 0);

    return this.entries()
      .filter(e => new Date(e.workDate) >= weekStart)
      .reduce((sum, e) => sum + e.hours, 0)
      .toFixed(1);
  });

  currentWeekStart = computed(() => {
    const now = new Date();
    const weekStart = new Date(now);
    weekStart.setDate(now.getDate() - now.getDay() + 1);
    return weekStart.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  });

  groupedEntries = computed(() => {
    const groups = new Map<string, TimeEntry[]>();
    for (const entry of this.entries()) {
      const date = entry.workDate;
      if (!groups.has(date)) groups.set(date, []);
      groups.get(date)!.push(entry);
    }
    const result: GroupedEntries[] = [];
    for (const [date, entries] of groups) {
      result.push({
        date,
        entries,
        totalHours: entries.reduce((sum, e) => sum + e.hours, 0)
      });
    }
    return result.sort((a, b) => b.date.localeCompare(a.date));
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.error.set(null);

    this.ticketService.getMyTickets().subscribe({
      next: (tickets) => {
        this.tickets.set(tickets);
        this.ticketMap.clear();
        for (const t of tickets) this.ticketMap.set(t.id, t);
      },
      error: () => {}
    });

    this.timeTrackingService.getMyEntries().subscribe({
      next: (entries) => {
        this.entries.set(entries);
        this.computeTicketSummaries(entries);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load time entries. Please try again.');
        this.loading.set(false);
      }
    });
  }

  getTicketKey(ticketId: string): string {
    return this.ticketMap.get(ticketId)?.fullKey || ticketId.substring(0, 8);
  }

  private computeTicketSummaries(entries: TimeEntry[]): void {
    const map = new Map<string, TicketTimeSummary>();
    for (const entry of entries) {
      if (!map.has(entry.ticketId)) {
        const ticket = this.ticketMap.get(entry.ticketId);
        map.set(entry.ticketId, {
          ticketId: entry.ticketId,
          ticketKey: ticket?.fullKey || entry.ticketId.substring(0, 8),
          ticketTitle: ticket?.title || 'Unknown',
          totalLogged: 0,
          estimatedHours: ticket?.estimatedHours || 0
        });
      }
      map.get(entry.ticketId)!.totalLogged += entry.hours;
    }
    this.ticketSummaries.set(Array.from(map.values()));
  }

  getProgressPercent(summary: TicketTimeSummary): number {
    if (!summary.estimatedHours) return 0;
    return Math.min(100, (summary.totalLogged / summary.estimatedHours) * 100);
  }

  logTime(event: Event): void {
    event.preventDefault();
    if (!this.formTicketId || !this.formHours || !this.formDate) return;

    this.submitting.set(true);
    const request: CreateTimeEntryRequest = {
      hours: this.formHours,
      description: this.formDescription || undefined,
      workDate: this.formDate
    };

    this.timeTrackingService.logTime(this.formTicketId, request).subscribe({
      next: (entry) => {
        this.entries.update(entries => [entry, ...entries]);
        this.computeTicketSummaries([entry, ...this.entries()]);
        this.formTicketId = '';
        this.formHours = 0;
        this.formDescription = '';
        this.submitting.set(false);
      },
      error: () => {
        this.submitting.set(false);
      }
    });
  }

  confirmDelete(entry: TimeEntry): void {
    this.entryToDelete.set(entry);
  }

  cancelDelete(): void {
    this.entryToDelete.set(null);
  }

  deleteEntry(): void {
    const entry = this.entryToDelete();
    if (!entry) return;

    this.timeTrackingService.deleteEntry(entry.id).subscribe({
      next: () => {
        this.entries.update(entries => entries.filter(e => e.id !== entry.id));
        this.computeTicketSummaries(this.entries());
        this.entryToDelete.set(null);
      },
      error: () => {
        this.entryToDelete.set(null);
      }
    });
  }
}
