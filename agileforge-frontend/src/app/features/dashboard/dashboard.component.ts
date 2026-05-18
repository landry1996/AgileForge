import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="dashboard">
      <h1>Welcome back, {{ authService.user()?.firstName }}!</h1>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-value">--</div>
          <div class="stat-label">My Tickets</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">--</div>
          <div class="stat-label">In Progress</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">--</div>
          <div class="stat-label">Done this Sprint</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">--</div>
          <div class="stat-label">Story Points</div>
        </div>
      </div>
      <div class="sections">
        <section class="section">
          <h2>Active Sprint</h2>
          <p class="placeholder">Select a project to view active sprint</p>
        </section>
        <section class="section">
          <h2>Recent Activity</h2>
          <p class="placeholder">No recent activity</p>
        </section>
      </div>
    </div>
  `,
  styles: [`
    .dashboard { max-width: 1200px; }
    h1 { color: #e1e4e8; margin-bottom: 24px; font-size: 1.6rem; }
    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 32px; }
    .stat-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px; text-align: center;
    }
    .stat-value { font-size: 2rem; font-weight: 700; color: #58a6ff; }
    .stat-label { font-size: 0.85rem; color: #8b949e; margin-top: 4px; }
    .sections { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
    .section {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px;
    }
    h2 { color: #e1e4e8; font-size: 1.1rem; margin-bottom: 12px; }
    .placeholder { color: #484f58; font-size: 0.9rem; }
  `]
})
export class DashboardComponent {
  authService = inject(AuthService);
}
