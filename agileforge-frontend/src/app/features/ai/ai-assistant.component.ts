import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AiService } from '../../core/services/ai.service';
import { GeneratedTicket, QualityAnalysis } from '../../core/models/ai.model';

@Component({
  selector: 'app-ai-assistant',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="ai-page">
      <h1>AI Assistant</h1>
      <p class="subtitle">Use AI to generate tickets, analyze quality, and decompose work</p>

      <div class="ai-tabs">
        <button [class.active]="activeTab() === 'generate'" (click)="activeTab.set('generate')">Generate Tickets</button>
        <button [class.active]="activeTab() === 'backlog'" (click)="activeTab.set('backlog')">Generate Backlog</button>
        <button [class.active]="activeTab() === 'quality'" (click)="activeTab.set('quality')">Analyze Quality</button>
        <button [class.active]="activeTab() === 'decompose'" (click)="activeTab.set('decompose')">Decompose</button>
      </div>

      <div class="ai-content">
        @switch (activeTab()) {
          @case ('generate') {
            <div class="input-section">
              <label>Describe what you want to build:</label>
              <textarea [(ngModel)]="description" rows="5"
                        placeholder="e.g., Build a user authentication system with email/password login, OAuth support, and password reset..."></textarea>
              <button class="btn-ai" [disabled]="!description || loading()" (click)="generateTickets()">
                {{ loading() ? 'Generating...' : 'Generate Tickets' }}
              </button>
            </div>
          }
          @case ('backlog') {
            <div class="input-section">
              <label>Project Name:</label>
              <input [(ngModel)]="projectName" placeholder="My Project">
              <label>Project Description:</label>
              <textarea [(ngModel)]="projectDescription" rows="4"
                        placeholder="Describe your project in detail..."></textarea>
              <button class="btn-ai" [disabled]="!projectName || !projectDescription || loading()" (click)="generateBacklog()">
                {{ loading() ? 'Generating...' : 'Generate Backlog' }}
              </button>
            </div>
          }
          @case ('quality') {
            <div class="input-section">
              <label>Ticket Title:</label>
              <input [(ngModel)]="ticketTitle" placeholder="e.g., Implement login">
              <label>Description (optional):</label>
              <textarea [(ngModel)]="ticketDescription" rows="3" placeholder="Ticket description..."></textarea>
              <button class="btn-ai" [disabled]="!ticketTitle || loading()" (click)="analyzeQuality()">
                {{ loading() ? 'Analyzing...' : 'Analyze Quality' }}
              </button>
            </div>
          }
          @case ('decompose') {
            <div class="input-section">
              <label>Ticket Title to decompose:</label>
              <input [(ngModel)]="ticketTitle" placeholder="e.g., Build REST API for users">
              <label>Description (optional):</label>
              <textarea [(ngModel)]="ticketDescription" rows="3" placeholder="Ticket description..."></textarea>
              <button class="btn-ai" [disabled]="!ticketTitle || loading()" (click)="decompose()">
                {{ loading() ? 'Decomposing...' : 'Decompose into Subtasks' }}
              </button>
            </div>
          }
        }

        @if (generatedTickets().length > 0) {
          <div class="results">
            <h3>Generated Tickets ({{ generatedTickets().length }})</h3>
            @for (ticket of generatedTickets(); track $index) {
              <div class="result-card">
                <div class="result-header">
                  <span class="result-type">{{ ticket.type }}</span>
                  <span class="result-priority">{{ ticket.priority }}</span>
                  @if (ticket.storyPoints) {
                    <span class="result-points">{{ ticket.storyPoints }}sp</span>
                  }
                </div>
                <h4>{{ ticket.title }}</h4>
                <p>{{ ticket.description }}</p>
              </div>
            }
          </div>
        }

        @if (qualityResult()) {
          <div class="results">
            <h3>Quality Analysis</h3>
            <div class="quality-score">
              <span class="score" [class]="getScoreClass(qualityResult()!.score)">{{ qualityResult()!.score }}/100</span>
            </div>
            @if (qualityResult()!.issues.length > 0) {
              <div class="quality-section">
                <h4>Issues</h4>
                <ul>
                  @for (issue of qualityResult()!.issues; track $index) {
                    <li class="issue">{{ issue }}</li>
                  }
                </ul>
              </div>
            }
            @if (qualityResult()!.suggestions.length > 0) {
              <div class="quality-section">
                <h4>Suggestions</h4>
                <ul>
                  @for (s of qualityResult()!.suggestions; track $index) {
                    <li class="suggestion">{{ s }}</li>
                  }
                </ul>
              </div>
            }
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .ai-page { max-width: 900px; }
    h1 { color: #e1e4e8; font-size: 1.4rem; margin: 0 0 4px; }
    .subtitle { color: #8b949e; font-size: 0.9rem; margin-bottom: 24px; }
    .ai-tabs { display: flex; gap: 4px; margin-bottom: 24px; }
    .ai-tabs button {
      padding: 8px 16px; background: #21262d; border: 1px solid #30363d; color: #8b949e;
      border-radius: 6px; cursor: pointer; font-size: 0.85rem;
    }
    .ai-tabs button.active { background: #1f6feb; color: white; border-color: #1f6feb; }
    .input-section { display: flex; flex-direction: column; gap: 12px; }
    label { color: #8b949e; font-size: 0.85rem; }
    input, textarea {
      padding: 10px 12px; background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      color: #e1e4e8; font-size: 0.9rem; font-family: inherit; resize: vertical;
    }
    input:focus, textarea:focus { border-color: #58a6ff; outline: none; }
    .btn-ai {
      align-self: flex-start; padding: 10px 20px; background: #8957e5; color: white;
      border: none; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 0.9rem;
    }
    .btn-ai:hover { background: #a371f7; }
    .btn-ai:disabled { opacity: 0.6; cursor: not-allowed; }
    .results { margin-top: 24px; }
    .results h3 { color: #e1e4e8; font-size: 1.1rem; margin-bottom: 12px; }
    .result-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-bottom: 8px;
    }
    .result-header { display: flex; gap: 8px; margin-bottom: 8px; }
    .result-type { font-size: 0.7rem; padding: 2px 6px; background: #58a6ff22; color: #58a6ff; border-radius: 3px; }
    .result-priority { font-size: 0.7rem; padding: 2px 6px; background: #d2992222; color: #d29922; border-radius: 3px; }
    .result-points { font-size: 0.7rem; padding: 2px 6px; background: #21262d; color: #8b949e; border-radius: 3px; }
    h4 { color: #e1e4e8; margin: 0 0 6px; font-size: 0.95rem; }
    p { color: #8b949e; font-size: 0.85rem; margin: 0; }
    .quality-score { margin-bottom: 16px; }
    .score { font-size: 2rem; font-weight: 700; }
    .score-excellent { color: #3fb950; }
    .score-good { color: #58a6ff; }
    .score-fair { color: #d29922; }
    .score-poor { color: #f85149; }
    .quality-section { margin-bottom: 12px; }
    .quality-section h4 { color: #8b949e; font-size: 0.85rem; margin-bottom: 8px; }
    ul { padding-left: 20px; margin: 0; }
    li { color: #e1e4e8; font-size: 0.85rem; margin-bottom: 4px; }
    .issue { color: #f85149; }
    .suggestion { color: #3fb950; }
  `]
})
export class AiAssistantComponent {
  private aiService = inject(AiService);

  activeTab = signal<'generate' | 'backlog' | 'quality' | 'decompose'>('generate');
  loading = signal(false);
  generatedTickets = signal<GeneratedTicket[]>([]);
  qualityResult = signal<QualityAnalysis | null>(null);

  description = '';
  projectName = '';
  projectDescription = '';
  ticketTitle = '';
  ticketDescription = '';

  generateTickets(): void {
    this.loading.set(true);
    this.generatedTickets.set([]);
    this.aiService.generateTickets({ description: this.description }).subscribe({
      next: (tickets) => { this.generatedTickets.set(tickets); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  generateBacklog(): void {
    this.loading.set(true);
    this.generatedTickets.set([]);
    this.aiService.generateBacklog({
      projectName: this.projectName,
      projectDescription: this.projectDescription
    }).subscribe({
      next: (tickets) => { this.generatedTickets.set(tickets); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  analyzeQuality(): void {
    this.loading.set(true);
    this.qualityResult.set(null);
    this.aiService.analyzeQuality(this.ticketTitle, this.ticketDescription).subscribe({
      next: (result) => { this.qualityResult.set(result); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  decompose(): void {
    this.loading.set(true);
    this.generatedTickets.set([]);
    this.aiService.decomposeTicket(this.ticketTitle, this.ticketDescription).subscribe({
      next: (tickets) => { this.generatedTickets.set(tickets); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  getScoreClass(score: number): string {
    if (score >= 90) return 'score-excellent';
    if (score >= 70) return 'score-good';
    if (score >= 50) return 'score-fair';
    return 'score-poor';
  }
}
