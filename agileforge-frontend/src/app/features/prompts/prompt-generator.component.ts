import { Component, inject, OnInit, signal } from '@angular/core';
import { PromptGeneratorService } from '../../core/services/prompt-generator.service';
import { TicketService } from '../../core/services/ticket.service';
import { Ticket } from '../../core/models/ticket.model';
import { GeneratedPrompt, GeneratePromptRequest, PromptTemplate, PromptCategory } from '../../core/models/prompt.model';

@Component({
  selector: 'app-prompt-generator',
  standalone: true,
  template: `
    <div class="prompt-page">
      <div class="prompt-header">
        <h1>Claude Code Prompt Generator</h1>
        <p class="subtitle">Generate optimized prompts for Claude Code from your tickets</p>
      </div>

      <div class="prompt-layout">
        <!-- Generator Panel -->
        <div class="generator-panel">
          <div class="panel-card">
            <h2>Generate Prompt</h2>

            <div class="form-group">
              <label>Select Ticket</label>
              <div class="search-input-wrapper">
                <input
                  type="text"
                  class="search-input"
                  placeholder="Search tickets..."
                  [value]="ticketSearch()"
                  (input)="onTicketSearch($event)"
                  (focus)="showTicketDropdown.set(true)"
                />
                @if (showTicketDropdown() && filteredTickets().length > 0) {
                  <div class="dropdown">
                    @for (ticket of filteredTickets(); track ticket.id) {
                      <div class="dropdown-item" (click)="selectTicket(ticket)">
                        <span class="ticket-key">{{ ticket.fullKey }}</span>
                        <span class="ticket-title">{{ ticket.title }}</span>
                      </div>
                    }
                  </div>
                }
              </div>
              @if (selectedTicket()) {
                <div class="selected-ticket">
                  <span class="ticket-key">{{ selectedTicket()!.fullKey }}</span>
                  <span>{{ selectedTicket()!.title }}</span>
                </div>
              }
            </div>

            <div class="form-group">
              <label>Template Category</label>
              <div class="category-grid">
                @for (cat of allCategories; track cat) {
                  <button
                    class="category-btn"
                    [class.selected]="selectedCategory() === cat"
                    (click)="selectedCategory.set(cat)">
                    {{ formatCategory(cat) }}
                  </button>
                }
              </div>
            </div>

            <div class="form-group">
              <label>Custom Instructions (optional)</label>
              <textarea
                class="custom-input"
                placeholder="Add any custom instructions or context..."
                rows="3"
                [value]="customInstructions()"
                (input)="customInstructions.set(asTextareaValue($event))"
              ></textarea>
            </div>

            <button
              class="btn-generate"
              [disabled]="!selectedTicket() || !selectedCategory()"
              (click)="generatePrompt()">
              @if (generating()) {
                Generating...
              } @else {
                Generate Prompt
              }
            </button>
          </div>

          <!-- Generated Result -->
          @if (generatedPrompt()) {
            <div class="panel-card result-card">
              <div class="result-header">
                <h2>Generated Prompt</h2>
                <button class="btn-copy" (click)="copyToClipboard()">
                  {{ copied() ? 'Copied!' : 'Copy to Clipboard' }}
                </button>
              </div>
              <div class="prompt-output">
                <pre class="prompt-code">{{ generatedPrompt()!.promptText }}</pre>
              </div>
              <div class="rating-section">
                <span class="rating-label">Rate this prompt:</span>
                <div class="stars">
                  @for (star of [1,2,3,4,5]; track star) {
                    <button
                      class="star-btn"
                      [class.filled]="star <= currentRating()"
                      (click)="ratePrompt(star)">
                      &#9733;
                    </button>
                  }
                </div>
              </div>
            </div>
          }
        </div>

        <!-- History Panel -->
        <div class="history-panel">
          <div class="panel-card">
            <h2>Prompt History</h2>
            @if (history().length === 0) {
              <p class="no-history">No prompts generated yet. Select a ticket to get started.</p>
            } @else {
              <div class="history-list">
                @for (item of history(); track item.id) {
                  <div class="history-item" (click)="viewHistoryItem(item)">
                    <div class="history-item-header">
                      <span class="history-template">{{ item.templateName || 'Custom' }}</span>
                    </div>
                    <div class="history-preview">{{ getPromptPreview(item.promptText) }}</div>
                    <div class="history-meta">
                      <span class="history-date">{{ item.createdAt }}</span>
                    </div>
                  </div>
                }
              </div>
            }
          </div>

          <!-- Templates Panel -->
          <div class="panel-card">
            <h2>Available Templates</h2>
            @if (templates().length === 0) {
              <p class="no-history">No templates available.</p>
            } @else {
              <div class="template-list">
                @for (tmpl of templates(); track tmpl.id) {
                  <div class="template-item">
                    <span class="template-name">{{ tmpl.name }}</span>
                    <span class="template-category">{{ formatCategory(tmpl.category) }}</span>
                    <span class="template-usage">Used {{ tmpl.usageCount }}x</span>
                  </div>
                }
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .prompt-page { max-width: 1200px; }
    .prompt-header { margin-bottom: 24px; }
    h1 { color: #e1e4e8; font-size: 1.5rem; margin: 0 0 4px; }
    .subtitle { color: #8b949e; font-size: 0.9rem; margin: 0; }
    h2 { color: #e1e4e8; font-size: 1.1rem; margin: 0 0 16px; }

    .prompt-layout { display: grid; grid-template-columns: 1fr 340px; gap: 16px; align-items: start; }

    .panel-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px; margin-bottom: 16px;
    }

    .form-group { margin-bottom: 20px; }
    .form-group label { display: block; color: #c9d1d9; font-size: 0.85rem; margin-bottom: 8px; font-weight: 600; }

    .search-input-wrapper { position: relative; }
    .search-input, .custom-input {
      width: 100%; background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      padding: 10px 12px; color: #c9d1d9; font-size: 0.9rem; box-sizing: border-box;
    }
    .custom-input { resize: vertical; font-family: inherit; }
    .search-input:focus, .custom-input:focus { border-color: #58a6ff; outline: none; }
    .dropdown {
      position: absolute; top: 100%; left: 0; right: 0; background: #161b22;
      border: 1px solid #30363d; border-radius: 6px; max-height: 200px; overflow-y: auto; z-index: 10;
      margin-top: 4px;
    }
    .dropdown-item {
      padding: 10px 12px; cursor: pointer; display: flex; gap: 8px; align-items: center;
      font-size: 0.85rem; color: #c9d1d9; border-bottom: 1px solid #21262d;
    }
    .dropdown-item:hover { background: #21262d; }
    .dropdown-item:last-child { border-bottom: none; }
    .ticket-key { color: #58a6ff; font-weight: 600; font-size: 0.8rem; white-space: nowrap; }
    .ticket-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

    .selected-ticket {
      display: flex; gap: 8px; align-items: center; margin-top: 8px; padding: 8px 12px;
      background: #1f6feb22; border: 1px solid #1f6feb44; border-radius: 6px; font-size: 0.85rem; color: #c9d1d9;
    }

    .category-grid { display: flex; flex-wrap: wrap; gap: 8px; }
    .category-btn {
      background: #21262d; color: #c9d1d9; border: 1px solid #30363d; padding: 8px 14px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer; transition: all 0.15s;
    }
    .category-btn:hover { border-color: #58a6ff; }
    .category-btn.selected { background: #1f6feb; color: white; border-color: #1f6feb; }

    .btn-generate {
      width: 100%; background: #238636; color: white; border: none; padding: 12px;
      border-radius: 6px; font-size: 0.95rem; cursor: pointer; font-weight: 600;
    }
    .btn-generate:hover:not(:disabled) { background: #2ea043; }
    .btn-generate:disabled { opacity: 0.5; cursor: not-allowed; }

    .result-card { border-color: #238636; }
    .result-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .btn-copy {
      background: #21262d; color: #c9d1d9; border: 1px solid #30363d; padding: 6px 12px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer;
    }
    .btn-copy:hover { background: #30363d; }
    .prompt-output { margin-bottom: 16px; }
    .prompt-code {
      background: #0d1117; border: 1px solid #30363d; border-radius: 6px; padding: 16px;
      color: #c9d1d9; font-size: 0.85rem; line-height: 1.6; white-space: pre-wrap; word-wrap: break-word;
      font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Fira Code', monospace;
      max-height: 400px; overflow-y: auto; margin: 0;
    }

    .rating-section { display: flex; align-items: center; gap: 12px; }
    .rating-label { color: #8b949e; font-size: 0.85rem; }
    .stars { display: flex; gap: 4px; }
    .star-btn {
      background: none; border: none; font-size: 1.4rem; cursor: pointer; color: #30363d;
      transition: color 0.15s; padding: 0;
    }
    .star-btn.filled { color: #d29922; }
    .star-btn:hover { color: #d29922; }

    .history-panel { position: sticky; top: 16px; }
    .no-history { color: #484f58; font-size: 0.85rem; text-align: center; padding: 20px; }
    .history-list { display: flex; flex-direction: column; gap: 8px; max-height: 300px; overflow-y: auto; }
    .history-item {
      padding: 12px; background: #0d1117; border: 1px solid #21262d; border-radius: 6px;
      cursor: pointer; transition: border-color 0.15s;
    }
    .history-item:hover { border-color: #58a6ff; }
    .history-item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
    .history-template { color: #a371f7; font-size: 0.75rem; background: #8957e522; padding: 2px 6px; border-radius: 4px; }
    .history-preview { color: #c9d1d9; font-size: 0.8rem; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .history-meta { display: flex; justify-content: space-between; align-items: center; }
    .history-date { color: #484f58; font-size: 0.7rem; }

    .template-list { display: flex; flex-direction: column; gap: 6px; }
    .template-item {
      display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: #0d1117;
      border: 1px solid #21262d; border-radius: 6px; font-size: 0.8rem;
    }
    .template-name { color: #c9d1d9; flex: 1; }
    .template-category { color: #58a6ff; font-size: 0.7rem; }
    .template-usage { color: #484f58; font-size: 0.7rem; }
  `]
})
export class PromptGeneratorComponent implements OnInit {
  private promptService = inject(PromptGeneratorService);
  private ticketService = inject(TicketService);

  tickets = signal<Ticket[]>([]);
  templates = signal<PromptTemplate[]>([]);
  history = signal<GeneratedPrompt[]>([]);
  filteredTickets = signal<Ticket[]>([]);
  selectedTicket = signal<Ticket | null>(null);
  selectedCategory = signal<PromptCategory | ''>('');
  customInstructions = signal('');
  ticketSearch = signal('');
  showTicketDropdown = signal(false);
  generatedPrompt = signal<GeneratedPrompt | null>(null);
  generating = signal(false);
  copied = signal(false);
  currentRating = signal<number>(0);

  private projectId = 'demo';

  allCategories: PromptCategory[] = [
    'BACKEND', 'FRONTEND', 'TESTING', 'BUG_FIX', 'REFACTORING',
    'DEVOPS', 'DOCUMENTATION', 'SECURITY', 'PERFORMANCE', 'CODE_REVIEW', 'MIGRATION'
  ];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.ticketService.getByProject(this.projectId).subscribe({
      next: (tickets) => {
        this.tickets.set(tickets);
        this.filteredTickets.set(tickets.slice(0, 10));
      }
    });

    this.promptService.getTemplates(this.projectId).subscribe({
      next: (templates) => this.templates.set(templates),
      error: () => {}
    });

    this.promptService.getGlobalTemplates().subscribe({
      next: (templates) => {
        const current = this.templates();
        this.templates.set([...current, ...templates]);
      },
      error: () => {}
    });
  }

  onTicketSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value.toLowerCase();
    this.ticketSearch.set(query);
    this.showTicketDropdown.set(true);
    const filtered = this.tickets().filter(t =>
      t.title.toLowerCase().includes(query) ||
      t.fullKey.toLowerCase().includes(query)
    ).slice(0, 10);
    this.filteredTickets.set(filtered);
  }

  selectTicket(ticket: Ticket): void {
    this.selectedTicket.set(ticket);
    this.ticketSearch.set(ticket.fullKey + ' - ' + ticket.title);
    this.showTicketDropdown.set(false);
    this.loadHistory(ticket.id);
  }

  loadHistory(ticketId: string): void {
    this.promptService.getHistory(ticketId).subscribe({
      next: (history) => this.history.set(history),
      error: () => {}
    });
  }

  generatePrompt(): void {
    const ticket = this.selectedTicket();
    const category = this.selectedCategory();
    if (!ticket || !category) return;

    this.generating.set(true);
    const request: GeneratePromptRequest = {};
    if (this.customInstructions()) {
      request.customInstructions = this.customInstructions();
    }

    this.promptService.generateForTicket(ticket.id, request).subscribe({
      next: (result) => {
        this.generatedPrompt.set(result);
        this.currentRating.set(0);
        this.generating.set(false);
        this.loadHistory(ticket.id);
      },
      error: () => this.generating.set(false)
    });
  }

  copyToClipboard(): void {
    const prompt = this.generatedPrompt();
    if (!prompt) return;
    navigator.clipboard.writeText(prompt.promptText).then(() => {
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    });
  }

  ratePrompt(rating: number): void {
    const prompt = this.generatedPrompt();
    if (!prompt) return;
    this.currentRating.set(rating);
    this.promptService.ratePrompt(prompt.id, rating).subscribe();
  }

  viewHistoryItem(item: GeneratedPrompt): void {
    this.generatedPrompt.set(item);
    this.currentRating.set(0);
  }

  formatCategory(category: string): string {
    return category.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  }

  getPromptPreview(text: string): string {
    return text.length > 80 ? text.substring(0, 80) + '...' : text;
  }

  asTextareaValue(event: Event): string {
    return (event.target as HTMLTextAreaElement).value;
  }
}
