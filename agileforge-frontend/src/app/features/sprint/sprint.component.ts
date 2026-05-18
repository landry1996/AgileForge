import { Component, inject, OnInit, signal } from '@angular/core';
import { SprintService } from '../../core/services/sprint.service';
import { Sprint } from '../../core/models/sprint.model';

@Component({
  selector: 'app-sprint',
  standalone: true,
  template: `
    <div class="sprint-page">
      <div class="sprint-header">
        <h1>Sprints</h1>
      </div>

      <div class="sprint-list">
        @for (sprint of sprints(); track sprint.id) {
          <div class="sprint-card">
            <div class="sprint-info">
              <h3>{{ sprint.name }}</h3>
              <span class="sprint-status status-{{ sprint.status?.toLowerCase() }}">{{ sprint.status }}</span>
            </div>
            @if (sprint.goal) {
              <p class="sprint-goal">{{ sprint.goal }}</p>
            }
            <div class="sprint-meta">
              <span>{{ sprint.doneTickets }}/{{ sprint.totalTickets }} tickets done</span>
              @if (sprint.startDate) {
                <span>{{ sprint.startDate }} → {{ sprint.endDate }}</span>
              }
            </div>
            <div class="sprint-progress">
              <div class="progress-bar"
                   [style.width.%]="sprint.totalTickets > 0 ? (sprint.doneTickets / sprint.totalTickets * 100) : 0">
              </div>
            </div>
          </div>
        } @empty {
          <p class="empty">No sprints yet. Create one to start planning.</p>
        }
      </div>
    </div>
  `,
  styles: [`
    .sprint-page { max-width: 900px; }
    .sprint-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #e1e4e8; font-size: 1.4rem; margin: 0; }
    .sprint-list { display: flex; flex-direction: column; gap: 12px; }
    .sprint-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px;
    }
    .sprint-info { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
    h3 { color: #e1e4e8; margin: 0; font-size: 1.1rem; }
    .sprint-status { font-size: 0.7rem; padding: 3px 8px; border-radius: 12px; font-weight: 600; text-transform: uppercase; }
    .status-active { background: #238636; color: white; }
    .status-planning { background: #1f6feb; color: white; }
    .status-completed { background: #8b949e; color: white; }
    .status-cancelled { background: #f85149; color: white; }
    .sprint-goal { color: #8b949e; font-size: 0.9rem; margin: 0 0 12px; }
    .sprint-meta { display: flex; gap: 16px; color: #8b949e; font-size: 0.8rem; margin-bottom: 12px; }
    .sprint-progress {
      height: 4px; background: #21262d; border-radius: 2px; overflow: hidden;
    }
    .progress-bar { height: 100%; background: #238636; border-radius: 2px; transition: width 0.3s; }
    .empty { color: #484f58; text-align: center; padding: 40px; }
  `]
})
export class SprintComponent implements OnInit {
  private sprintService = inject(SprintService);
  sprints = signal<Sprint[]>([]);

  ngOnInit(): void {
    // Will load when project context is available
  }
}
