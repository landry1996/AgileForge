import { Component, inject, OnInit, signal } from '@angular/core';
import { AnalyticsService } from '../../core/services/analytics.service';
import { ProjectAnalytics, VelocityDataPoint, BurndownDataPoint, TeamWorkload } from '../../core/models/analytics.model';

@Component({
  selector: 'app-analytics',
  standalone: true,
  template: `
    <div class="analytics-page">
      <div class="analytics-header">
        <h1>Analytics</h1>
      </div>

      @if (loading()) {
        <div class="loading">Loading analytics...</div>
      } @else if (!analytics()) {
        <div class="empty-state">
          <h3>No analytics data available</h3>
          <p>Start completing tickets to see project analytics.</p>
        </div>
      } @else {
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ analytics()!.totalTickets }}</div>
            <div class="stat-label">Total Tickets</div>
          </div>
          <div class="stat-card">
            <div class="stat-value stat-open">{{ analytics()!.openTickets }}</div>
            <div class="stat-label">Open</div>
          </div>
          <div class="stat-card">
            <div class="stat-value stat-closed">{{ analytics()!.closedTickets }}</div>
            <div class="stat-label">Closed</div>
          </div>
          <div class="stat-card">
            <div class="stat-value stat-velocity">{{ analytics()!.averageVelocity }}</div>
            <div class="stat-label">Avg Velocity</div>
          </div>
        </div>

        <div class="charts-grid">
          <!-- Velocity Chart -->
          <div class="chart-card">
            <h2>Sprint Velocity</h2>
            @if (velocityData().length > 0) {
              <div class="velocity-chart">
                <svg [attr.viewBox]="'0 0 ' + (velocityData().length * 80 + 40) + ' 200'" class="chart-svg">
                  @for (item of velocityData(); track item.sprintName; let i = $index) {
                    <!-- Committed bar -->
                    <rect
                      [attr.x]="i * 80 + 20"
                      [attr.y]="200 - getBarHeight(item.committedPoints)"
                      width="25"
                      [attr.height]="getBarHeight(item.committedPoints)"
                      fill="#30363d"
                      rx="3"
                    />
                    <!-- Completed bar -->
                    <rect
                      [attr.x]="i * 80 + 48"
                      [attr.y]="200 - getBarHeight(item.completedPoints)"
                      width="25"
                      [attr.height]="getBarHeight(item.completedPoints)"
                      fill="#238636"
                      rx="3"
                    />
                    <!-- Label -->
                    <text [attr.x]="i * 80 + 45" y="198" text-anchor="middle" fill="#8b949e" font-size="9">
                      {{ item.sprintName.length > 8 ? item.sprintName.substring(0, 8) : item.sprintName }}
                    </text>
                  }
                </svg>
                <div class="chart-legend">
                  <span class="legend-item"><span class="legend-color" style="background:#30363d"></span> Committed</span>
                  <span class="legend-item"><span class="legend-color" style="background:#238636"></span> Completed</span>
                </div>
              </div>
            } @else {
              <p class="no-data">No velocity data yet</p>
            }
          </div>

          <!-- Ticket Distribution -->
          <div class="chart-card">
            <h2>Ticket Distribution</h2>
            <div class="distribution-section">
              <h4>By Status</h4>
              @for (item of statusEntries(); track item.key) {
                <div class="dist-row">
                  <span class="dist-label">{{ item.key }}</span>
                  <div class="dist-bar-track">
                    <div class="dist-bar-fill" [style.width.%]="getDistPercent(item.value, maxStatusCount())" [style.background]="getStatusColor(item.key)"></div>
                  </div>
                  <span class="dist-count">{{ item.value }}</span>
                </div>
              }
            </div>
            <div class="distribution-section">
              <h4>By Priority</h4>
              @for (item of priorityEntries(); track item.key) {
                <div class="dist-row">
                  <span class="dist-label">{{ item.key }}</span>
                  <div class="dist-bar-track">
                    <div class="dist-bar-fill" [style.width.%]="getDistPercent(item.value, maxPriorityCount())" [style.background]="getPriorityColor(item.key)"></div>
                  </div>
                  <span class="dist-count">{{ item.value }}</span>
                </div>
              }
            </div>
          </div>

          <!-- Team Workload -->
          <div class="chart-card">
            <h2>Team Workload</h2>
            @if (teamWorkload().length > 0) {
              <div class="workload-table">
                <div class="table-header">
                  <span>Member</span>
                  <span>Assigned</span>
                  <span>In Progress</span>
                  <span>Points</span>
                </div>
                @for (member of teamWorkload(); track member.userId) {
                  <div class="table-row">
                    <span class="member-name">{{ member.userName }}</span>
                    <span>{{ member.assignedTickets }}</span>
                    <span class="in-progress-cell">{{ member.inProgressTickets }}</span>
                    <span>{{ member.totalPointsAssigned }}</span>
                  </div>
                }
              </div>
            } @else {
              <p class="no-data">No workload data available</p>
            }
          </div>

          <!-- Burndown Chart -->
          <div class="chart-card">
            <h2>Burndown</h2>
            @if (burndownData().length > 0) {
              <div class="burndown-chart">
                <svg [attr.viewBox]="'0 0 ' + (burndownData().length * 40 + 20) + ' 160'" class="chart-svg">
                  <!-- Ideal line -->
                  <polyline
                    [attr.points]="getIdealLinePoints()"
                    fill="none"
                    stroke="#30363d"
                    stroke-width="2"
                    stroke-dasharray="4,4"
                  />
                  <!-- Actual line -->
                  <polyline
                    [attr.points]="getBurndownLinePoints()"
                    fill="none"
                    stroke="#58a6ff"
                    stroke-width="2"
                  />
                  <!-- Data points -->
                  @for (point of burndownData(); track point.date; let i = $index) {
                    <circle
                      [attr.cx]="i * 40 + 20"
                      [attr.cy]="getBurndownY(point.remainingPoints)"
                      r="3"
                      fill="#58a6ff"
                    />
                  }
                </svg>
                <div class="chart-legend">
                  <span class="legend-item"><span class="legend-color" style="background:#30363d"></span> Ideal</span>
                  <span class="legend-item"><span class="legend-color" style="background:#58a6ff"></span> Actual</span>
                </div>
              </div>
            } @else {
              <p class="no-data">No burndown data for current sprint</p>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .analytics-page { max-width: 1200px; }
    .analytics-header { margin-bottom: 24px; }
    h1 { color: #e1e4e8; font-size: 1.5rem; margin: 0; }

    .loading { color: #8b949e; text-align: center; padding: 60px; }
    .empty-state { text-align: center; padding: 60px; color: #8b949e; }
    .empty-state h3 { color: #e1e4e8; margin-bottom: 8px; }
    .no-data { color: #484f58; font-size: 0.9rem; text-align: center; padding: 20px; }

    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .stat-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px; text-align: center;
    }
    .stat-value { font-size: 2rem; font-weight: 700; color: #58a6ff; }
    .stat-open { color: #d29922; }
    .stat-closed { color: #3fb950; }
    .stat-velocity { color: #a371f7; }
    .stat-label { font-size: 0.85rem; color: #8b949e; margin-top: 4px; }

    .charts-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
    .chart-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px;
    }
    h2 { color: #e1e4e8; font-size: 1.1rem; margin: 0 0 16px; }
    h4 { color: #8b949e; font-size: 0.85rem; margin: 16px 0 8px; }
    h4:first-of-type { margin-top: 0; }

    .chart-svg { width: 100%; height: auto; max-height: 180px; }
    .chart-legend { display: flex; gap: 16px; margin-top: 12px; justify-content: center; }
    .legend-item { display: flex; align-items: center; gap: 6px; font-size: 0.75rem; color: #8b949e; }
    .legend-color { width: 12px; height: 12px; border-radius: 2px; display: inline-block; }

    .distribution-section { margin-bottom: 12px; }
    .dist-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
    .dist-label { font-size: 0.8rem; color: #c9d1d9; min-width: 90px; text-transform: capitalize; }
    .dist-bar-track { flex: 1; height: 8px; background: #21262d; border-radius: 4px; overflow: hidden; }
    .dist-bar-fill { height: 100%; border-radius: 4px; transition: width 0.3s; }
    .dist-count { font-size: 0.75rem; color: #8b949e; min-width: 30px; text-align: right; }

    .workload-table { font-size: 0.85rem; }
    .table-header {
      display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; padding: 8px 0;
      border-bottom: 1px solid #30363d; color: #8b949e; font-weight: 600;
    }
    .table-row {
      display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; padding: 10px 0;
      border-bottom: 1px solid #21262d; color: #c9d1d9;
    }
    .table-row:last-child { border-bottom: none; }
    .member-name { color: #58a6ff; }
    .in-progress-cell { color: #d29922; }

    .velocity-chart, .burndown-chart { padding: 8px 0; }
  `]
})
export class AnalyticsComponent implements OnInit {
  private analyticsService = inject(AnalyticsService);

  analytics = signal<ProjectAnalytics | null>(null);
  velocityData = signal<VelocityDataPoint[]>([]);
  teamWorkload = signal<TeamWorkload[]>([]);
  burndownData = signal<BurndownDataPoint[]>([]);
  statusEntries = signal<{ key: string; value: number }[]>([]);
  priorityEntries = signal<{ key: string; value: number }[]>([]);
  maxStatusCount = signal(1);
  maxPriorityCount = signal(1);
  loading = signal(true);

  private projectId = 'demo';
  private maxVelocity = 1;
  private maxBurndown = 1;

  ngOnInit(): void {
    this.loadAnalytics();
  }

  loadAnalytics(): void {
    this.loading.set(true);
    this.analyticsService.getProjectAnalytics(this.projectId).subscribe({
      next: (data) => {
        this.analytics.set(data);
        this.velocityData.set(data.velocityHistory || []);

        const statusArr = Object.entries(data.ticketsByStatus || {}).map(([key, value]) => ({ key, value }));
        this.statusEntries.set(statusArr);
        this.maxStatusCount.set(Math.max(...statusArr.map(s => s.value), 1));

        const priorityArr = Object.entries(data.ticketsByPriority || {}).map(([key, value]) => ({ key, value }));
        this.priorityEntries.set(priorityArr);
        this.maxPriorityCount.set(Math.max(...priorityArr.map(p => p.value), 1));

        this.maxVelocity = Math.max(...(data.velocityHistory || []).map(v => Math.max(v.committedPoints, v.completedPoints)), 1);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });

    this.analyticsService.getTeamWorkload(this.projectId).subscribe({
      next: (workload) => this.teamWorkload.set(workload),
      error: () => {}
    });
  }

  getBarHeight(value: number): number {
    return (value / this.maxVelocity) * 160;
  }

  getDistPercent(value: number, max: number): number {
    return (value / max) * 100;
  }

  getBurndownY(value: number): number {
    return 150 - (value / this.maxBurndown) * 140;
  }

  getIdealLinePoints(): string {
    return this.burndownData().map((point, i) => `${i * 40 + 20},${this.getBurndownY(point.idealPoints)}`).join(' ');
  }

  getBurndownLinePoints(): string {
    return this.burndownData().map((point, i) => `${i * 40 + 20},${this.getBurndownY(point.remainingPoints)}`).join(' ');
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'BACKLOG': '#8b949e', 'TODO': '#58a6ff', 'IN_PROGRESS': '#d29922',
      'CODE_REVIEW': '#a371f7', 'QA': '#f778ba', 'DONE': '#3fb950',
      'BLOCKED': '#f85149', 'CANCELLED': '#484f58', 'IN_REVIEW': '#a371f7', 'DEPLOYED': '#238636'
    };
    return colors[status] || '#8b949e';
  }

  getPriorityColor(priority: string): string {
    const colors: Record<string, string> = {
      'CRITICAL': '#f85149', 'HIGH': '#d29922', 'MEDIUM': '#58a6ff',
      'LOW': '#3fb950', 'TRIVIAL': '#8b949e'
    };
    return colors[priority] || '#8b949e';
  }
}
