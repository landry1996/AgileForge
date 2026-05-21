import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AiAgentService } from '../../core/services/ai-agent.service';
import { AiAgent, AiAgentTask, SprintPlanSuggestion, RiskReport } from '../../core/models/ecosystem.model';

@Component({
  selector: 'app-ai-agents',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="agents-container">
      <header class="page-header">
        <h1>AI Agents</h1>
        <p class="subtitle">Autonomous intelligent assistants for your project</p>
      </header>

      <div class="tabs">
        <button [class.active]="activeTab() === 'agents'" (click)="activeTab.set('agents')">Agents</button>
        <button [class.active]="activeTab() === 'sprint-plan'" (click)="activeTab.set('sprint-plan')">Sprint Planner</button>
        <button [class.active]="activeTab() === 'risks'" (click)="activeTab.set('risks')">Risk Analysis</button>
        <button [class.active]="activeTab() === 'history'" (click)="activeTab.set('history')">Task History</button>
      </div>

      @if (activeTab() === 'agents') {
        <div class="agents-grid">
          @for (agent of agents(); track agent.id) {
            <div class="agent-card" [class.active]="agent.active">
              <div class="agent-header">
                <span class="agent-icon">{{ getAgentIcon(agent.agentType) }}</span>
                <span class="agent-status" [class.online]="agent.active">{{ agent.active ? 'Active' : 'Inactive' }}</span>
              </div>
              <h3>{{ agent.name }}</h3>
              <p>{{ agent.description }}</p>
              <div class="agent-capabilities">
                @for (cap of agent.capabilities; track cap) {
                  <span class="capability-tag">{{ cap }}</span>
                }
              </div>
              <div class="agent-footer">
                <span class="agent-model">{{ agent.model }}</span>
                <button class="btn-execute" (click)="executeAgent(agent)" [disabled]="!agent.active">
                  Execute
                </button>
              </div>
            </div>
          }
          @empty {
            <p class="empty-state">Loading agents...</p>
          }
        </div>
      }

      @if (activeTab() === 'sprint-plan') {
        <div class="sprint-plan-section">
          <div class="action-bar">
            <button class="btn-primary" (click)="generateSprintPlan()">Generate Sprint Plan</button>
          </div>
          @if (sprintPlan()) {
            <div class="plan-result">
              <div class="plan-header">
                <div class="plan-stat">
                  <span class="stat-value">{{ sprintPlan()!.totalPoints }}</span>
                  <span class="stat-label">Story Points</span>
                </div>
                <div class="plan-stat">
                  <span class="stat-value">{{ (sprintPlan()!.completionProbability * 100).toFixed(0) }}%</span>
                  <span class="stat-label">Completion Probability</span>
                </div>
                <div class="plan-stat">
                  <span class="stat-value">{{ sprintPlan()!.suggestedTickets.length }}</span>
                  <span class="stat-label">Tickets</span>
                </div>
              </div>
              <div class="plan-reasoning">
                <h4>Reasoning</h4>
                <p>{{ sprintPlan()!.reasoning }}</p>
              </div>
              @if (sprintPlan()!.risks.length) {
                <div class="plan-risks">
                  <h4>Identified Risks</h4>
                  <ul>
                    @for (risk of sprintPlan()!.risks; track risk) {
                      <li>{{ risk }}</li>
                    }
                  </ul>
                </div>
              }
            </div>
          }
        </div>
      }

      @if (activeTab() === 'risks') {
        <div class="risk-section">
          <div class="action-bar">
            <button class="btn-primary" (click)="analyzeRisks()">Run Risk Analysis</button>
          </div>
          @if (riskReport()) {
            <div class="risk-result">
              <div class="risk-overall" [class]="'risk-' + riskReport()!.overallRisk.toLowerCase()">
                Overall Risk: {{ riskReport()!.overallRisk }}
              </div>
              @if (riskReport()!.items.length) {
                <h4>Risk Items</h4>
                @for (item of riskReport()!.items; track item.title) {
                  <div class="risk-item" [class]="'severity-' + item.severity.toLowerCase()">
                    <span class="risk-severity">{{ item.severity }}</span>
                    <strong>{{ item.title }}</strong>
                    <p>{{ item.description }}</p>
                  </div>
                }
              }
              @if (riskReport()!.recommendations.length) {
                <h4>Recommendations</h4>
                <ul class="recommendations">
                  @for (rec of riskReport()!.recommendations; track rec) {
                    <li>{{ rec }}</li>
                  }
                </ul>
              }
            </div>
          }
        </div>
      }

      @if (activeTab() === 'history') {
        <div class="history-section">
          <table class="history-table">
            <thead>
              <tr><th>Agent</th><th>Task</th><th>Status</th><th>Duration</th><th>Date</th></tr>
            </thead>
            <tbody>
              @for (task of taskHistory(); track task.id) {
                <tr>
                  <td>{{ task.agentId }}</td>
                  <td>{{ task.taskType }}</td>
                  <td><span class="status-badge" [class]="task.status.toLowerCase()">{{ task.status }}</span></td>
                  <td>{{ task.executionTimeMs }}ms</td>
                  <td>{{ task.createdAt | date:'short' }}</td>
                </tr>
              }
              @empty {
                <tr><td colspan="5" class="empty-state">No task history yet.</td></tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
  styles: [`
    .agents-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
    .page-header h1 { margin: 0; font-size: 24px; }
    .subtitle { color: #6b7280; margin: 4px 0 24px; }
    .tabs { display: flex; gap: 8px; margin-bottom: 24px; border-bottom: 1px solid #e5e7eb; padding-bottom: 8px; }
    .tabs button { padding: 8px 16px; border: none; background: none; cursor: pointer; border-radius: 6px; font-weight: 500; }
    .tabs button.active { background: #4f46e5; color: white; }
    .agents-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; }
    .agent-card { padding: 24px; border: 2px solid #e5e7eb; border-radius: 12px; transition: all 0.2s; }
    .agent-card.active { border-color: #4f46e5; }
    .agent-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
    .agent-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .agent-icon { font-size: 28px; }
    .agent-status { font-size: 12px; padding: 4px 8px; border-radius: 12px; background: #fee2e2; color: #dc2626; }
    .agent-status.online { background: #dcfce7; color: #16a34a; }
    .agent-card h3 { margin: 0 0 8px; }
    .agent-card p { font-size: 13px; color: #6b7280; margin: 0 0 12px; }
    .agent-capabilities { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 16px; }
    .capability-tag { font-size: 11px; padding: 3px 8px; background: #ede9fe; color: #6d28d9; border-radius: 4px; }
    .agent-footer { display: flex; justify-content: space-between; align-items: center; }
    .agent-model { font-size: 11px; color: #9ca3af; }
    .btn-execute { padding: 6px 14px; background: #4f46e5; color: white; border: none; border-radius: 6px; cursor: pointer; }
    .btn-execute:disabled { background: #d1d5db; cursor: not-allowed; }
    .btn-primary { padding: 10px 20px; background: #4f46e5; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 500; }
    .action-bar { margin-bottom: 20px; }
    .plan-header { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; }
    .plan-stat { text-align: center; padding: 16px; background: #f9fafb; border-radius: 8px; }
    .plan-stat .stat-value { display: block; font-size: 24px; font-weight: bold; color: #4f46e5; }
    .plan-stat .stat-label { font-size: 12px; color: #6b7280; }
    .plan-reasoning, .plan-risks { padding: 16px; background: #f9fafb; border-radius: 8px; margin-bottom: 16px; }
    .plan-reasoning h4, .plan-risks h4 { margin: 0 0 8px; font-size: 14px; }
    .risk-overall { padding: 12px 20px; border-radius: 8px; font-weight: 600; text-align: center; margin-bottom: 20px; }
    .risk-overall.risk-low { background: #dcfce7; color: #16a34a; }
    .risk-overall.risk-medium { background: #fef3c7; color: #d97706; }
    .risk-overall.risk-high { background: #fee2e2; color: #dc2626; }
    .risk-overall.risk-unknown { background: #f3f4f6; color: #6b7280; }
    .risk-item { padding: 12px; border-left: 4px solid #e5e7eb; margin-bottom: 8px; }
    .risk-item.severity-high { border-color: #dc2626; }
    .risk-item.severity-medium { border-color: #d97706; }
    .risk-item.severity-low { border-color: #16a34a; }
    .risk-severity { font-size: 11px; font-weight: 600; text-transform: uppercase; }
    .recommendations li { padding: 8px 0; border-bottom: 1px solid #f3f4f6; }
    .history-table { width: 100%; border-collapse: collapse; }
    .history-table th, .history-table td { padding: 12px; text-align: left; border-bottom: 1px solid #e5e7eb; }
    .history-table th { font-size: 12px; text-transform: uppercase; color: #6b7280; }
    .status-badge { font-size: 11px; padding: 3px 8px; border-radius: 4px; }
    .status-badge.completed { background: #dcfce7; color: #16a34a; }
    .status-badge.running { background: #dbeafe; color: #2563eb; }
    .status-badge.failed { background: #fee2e2; color: #dc2626; }
    .status-badge.pending { background: #f3f4f6; color: #6b7280; }
    .empty-state { text-align: center; color: #9ca3af; padding: 32px; }
  `]
})
export class AiAgentsComponent implements OnInit {
  private aiAgentService = inject(AiAgentService);

  activeTab = signal<'agents' | 'sprint-plan' | 'risks' | 'history'>('agents');
  agents = signal<AiAgent[]>([]);
  sprintPlan = signal<SprintPlanSuggestion | null>(null);
  riskReport = signal<RiskReport | null>(null);
  taskHistory = signal<AiAgentTask[]>([]);

  ngOnInit() {
    this.aiAgentService.getAvailableAgents().subscribe(a => this.agents.set(a));
  }

  executeAgent(agent: AiAgent) {
    this.aiAgentService.executeTask(agent.id, 'current-org', 'current-user', agent.agentType).subscribe();
  }

  generateSprintPlan() {
    this.aiAgentService.suggestSprintPlan('current-project').subscribe(p => this.sprintPlan.set(p));
  }

  analyzeRisks() {
    this.aiAgentService.analyzeRisks('current-project').subscribe(r => this.riskReport.set(r));
  }

  getAgentIcon(type: string): string {
    const icons: Record<string, string> = {
      'SPRINT_PLANNING': '📋', 'RISK_DETECTION': '🔍', 'DOCUMENTATION': '📝',
      'CODE_REVIEW': '🔬', 'RETROSPECTIVE': '🔄'
    };
    return icons[type] || '🤖';
  }
}
