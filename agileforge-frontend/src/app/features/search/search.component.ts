import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { SearchService } from '../../core/services/search.service';
import { Ticket, TicketStatus, TicketType, TicketPriority } from '../../core/models/ticket.model';
import { SearchFilters } from '../../core/models/notification.model';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [FormsModule, SlicePipe],
  template: `
    <div class="search-page">
      <h2 class="page-title">Search Tickets</h2>

      <div class="search-controls">
        <div class="search-bar">
          <input
            type="text"
            class="search-main-input"
            placeholder="Search by title, description, labels..."
            [(ngModel)]="filters.q"
            (keydown.enter)="search()"
          />
          <button class="search-btn" (click)="search()">Search</button>
        </div>

        <div class="filters-row">
          <select [(ngModel)]="filters.status" (change)="search()" class="filter-select">
            <option value="">All Statuses</option>
            @for (s of statuses; track s) {
              <option [value]="s">{{ s }}</option>
            }
          </select>
          <select [(ngModel)]="filters.type" (change)="search()" class="filter-select">
            <option value="">All Types</option>
            @for (t of types; track t) {
              <option [value]="t">{{ t }}</option>
            }
          </select>
          <select [(ngModel)]="filters.priority" (change)="search()" class="filter-select">
            <option value="">All Priorities</option>
            @for (p of priorities; track p) {
              <option [value]="p">{{ p }}</option>
            }
          </select>
          <button class="clear-btn" (click)="clearFilters()">Clear</button>
        </div>
      </div>

      @if (loading()) {
        <div class="loading">Searching...</div>
      }

      @if (!loading() && results().length > 0) {
        <div class="results-info">
          Found {{ totalCount() }} result{{ totalCount() > 1 ? 's' : '' }}
        </div>
        <div class="results-list">
          @for (ticket of results(); track ticket.id) {
            <div class="result-card">
              <div class="result-card-header">
                <span class="ticket-key">{{ ticket.fullKey }}</span>
                <span class="ticket-priority" [attr.data-priority]="ticket.priority">{{ ticket.priority }}</span>
              </div>
              <div class="ticket-title">{{ ticket.title }}</div>
              @if (ticket.description) {
                <div class="ticket-desc">{{ ticket.description | slice:0:150 }}{{ ticket.description.length > 150 ? '...' : '' }}</div>
              }
              <div class="result-card-footer">
                <span class="ticket-type" [attr.data-type]="ticket.type">{{ ticket.type }}</span>
                <span class="ticket-status" [attr.data-status]="ticket.status">{{ ticket.status }}</span>
                @if (ticket.labels) {
                  <span class="ticket-labels">{{ ticket.labels }}</span>
                }
              </div>
            </div>
          }
        </div>

        @if (totalPages() > 1) {
          <div class="pagination">
            <button [disabled]="currentPage() === 0" (click)="goToPage(currentPage() - 1)">Previous</button>
            <span>Page {{ currentPage() + 1 }} of {{ totalPages() }}</span>
            <button [disabled]="currentPage() >= totalPages() - 1" (click)="goToPage(currentPage() + 1)">Next</button>
          </div>
        }
      }

      @if (!loading() && results().length === 0 && hasSearched()) {
        <div class="no-results">
          <p>No tickets found matching your criteria.</p>
        </div>
      }
    </div>
  `,
  styles: [`
    .search-page { max-width: 900px; }
    .page-title { color: #e1e4e8; margin-bottom: 24px; }
    .search-controls { margin-bottom: 24px; }
    .search-bar { display: flex; gap: 8px; margin-bottom: 12px; }
    .search-main-input {
      flex: 1;
      padding: 10px 16px;
      background: #0d1117;
      border: 1px solid #30363d;
      border-radius: 6px;
      color: #e1e4e8;
      font-size: 0.95rem;
    }
    .search-main-input:focus { border-color: #58a6ff; outline: none; }
    .search-btn {
      padding: 10px 20px;
      background: #238636;
      border: none;
      border-radius: 6px;
      color: white;
      font-weight: 500;
      cursor: pointer;
    }
    .search-btn:hover { background: #2ea043; }
    .filters-row { display: flex; gap: 8px; flex-wrap: wrap; }
    .filter-select {
      padding: 8px 12px;
      background: #0d1117;
      border: 1px solid #30363d;
      border-radius: 6px;
      color: #e1e4e8;
      font-size: 0.85rem;
    }
    .clear-btn {
      padding: 8px 12px;
      background: none;
      border: 1px solid #30363d;
      border-radius: 6px;
      color: #8b949e;
      cursor: pointer;
    }
    .clear-btn:hover { border-color: #8b949e; color: #e1e4e8; }
    .loading { padding: 32px; text-align: center; color: #8b949e; }
    .results-info { color: #8b949e; font-size: 0.85rem; margin-bottom: 16px; }
    .results-list { display: flex; flex-direction: column; gap: 8px; }
    .result-card {
      background: #161b22;
      border: 1px solid #21262d;
      border-radius: 8px;
      padding: 16px;
    }
    .result-card:hover { border-color: #30363d; }
    .result-card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
    .ticket-key { color: #58a6ff; font-weight: 600; font-size: 0.85rem; }
    .ticket-priority {
      font-size: 0.7rem;
      padding: 2px 8px;
      border-radius: 10px;
      background: #21262d;
      color: #8b949e;
    }
    .ticket-priority[data-priority="CRITICAL"] { background: #f8514920; color: #f85149; }
    .ticket-priority[data-priority="HIGH"] { background: #d2992220; color: #d29922; }
    .ticket-title { color: #e1e4e8; font-size: 0.95rem; font-weight: 500; margin-bottom: 4px; }
    .ticket-desc { color: #8b949e; font-size: 0.8rem; margin-bottom: 8px; line-height: 1.4; }
    .result-card-footer { display: flex; gap: 8px; align-items: center; }
    .ticket-type, .ticket-status {
      font-size: 0.7rem;
      padding: 2px 8px;
      border-radius: 10px;
      background: #21262d;
      color: #8b949e;
    }
    .ticket-status[data-status="DONE"] { background: #23863620; color: #3fb950; }
    .ticket-status[data-status="IN_PROGRESS"] { background: #58a6ff20; color: #58a6ff; }
    .ticket-status[data-status="BLOCKED"] { background: #f8514920; color: #f85149; }
    .ticket-labels { font-size: 0.7rem; color: #a371f7; }
    .pagination {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      margin-top: 24px;
      padding: 16px;
    }
    .pagination button {
      padding: 8px 16px;
      background: #21262d;
      border: 1px solid #30363d;
      border-radius: 6px;
      color: #e1e4e8;
      cursor: pointer;
    }
    .pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
    .pagination button:not(:disabled):hover { border-color: #58a6ff; }
    .pagination span { color: #8b949e; font-size: 0.85rem; }
    .no-results { padding: 48px; text-align: center; color: #484f58; }
  `]
})
export class SearchComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private searchService = inject(SearchService);

  filters: SearchFilters = {};
  results = signal<Ticket[]>([]);
  loading = signal(false);
  totalCount = signal(0);
  totalPages = signal(0);
  currentPage = signal(0);
  hasSearched = signal(false);

  statuses: TicketStatus[] = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'CODE_REVIEW', 'QA', 'DONE', 'BLOCKED', 'CANCELLED'];
  types: TicketType[] = ['EPIC', 'STORY', 'TASK', 'BUG', 'SPIKE', 'SUBTASK', 'IMPROVEMENT', 'FEATURE'];
  priorities: TicketPriority[] = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'TRIVIAL'];

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['q']) {
        this.filters.q = params['q'];
        this.search();
      }
    });
  }

  search(): void {
    this.loading.set(true);
    this.hasSearched.set(true);
    this.filters.page = this.currentPage();

    this.searchService.searchTickets(this.filters).subscribe({
      next: (response) => {
        this.results.set(response.items);
        this.totalCount.set(response.totalCount);
        this.totalPages.set(response.totalPages);
        this.loading.set(false);
      },
      error: () => {
        this.results.set([]);
        this.loading.set(false);
      }
    });
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.search();
  }

  clearFilters(): void {
    this.filters = {};
    this.currentPage.set(0);
    this.results.set([]);
    this.hasSearched.set(false);
  }
}
