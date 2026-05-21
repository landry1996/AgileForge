import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PortfolioService } from '../../core/services/portfolio.service';
import { Portfolio, PortfolioDashboard, RiskHeatMapEntry } from '../../core/models/enterprise.model';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="portfolio-container">
      <div class="page-header">
        <h1>Portfolio Management</h1>
        <button class="btn-primary" (click)="showCreate = !showCreate">+ New Portfolio</button>
      </div>

      @if (showCreate) {
        <div class="card create-form">
          <input [(ngModel)]="newName" placeholder="Portfolio name" class="input">
          <input [(ngModel)]="newDescription" placeholder="Description" class="input">
          <button class="btn-primary" (click)="create()">Create</button>
        </div>
      }

      @if (selectedDashboard()) {
        <div class="dashboard-section">
          <div class="dashboard-header">
            <h2>{{ selectedDashboard()!.name }}</h2>
            <span class="health-badge" [class]="getHealthClass(selectedDashboard()!.overallHealth)">
              Health: {{ selectedDashboard()!.overallHealth }}%
            </span>
            <button class="btn-back" (click)="selectedDashboard.set(null)">Back to list</button>
          </div>

          <div class="summary-cards">
            <div class="card stat-card">
              <span class="stat-value">{{ selectedDashboard()!.totalProjects }}</span>
              <span class="stat-label">Projects</span>
            </div>
            <div class="card stat-card">
              <span class="stat-value">{{ selectedDashboard()!.overallHealth }}%</span>
              <span class="stat-label">Overall Health</span>
            </div>
          </div>

          <div class="card">
            <h3>Project Summaries</h3>
            <div class="project-grid">
              @for (proj of selectedDashboard()!.projectSummaries; track proj.projectId) {
                <div class="project-card">
                  <div class="project-name">{{ proj.name }} <span class="key">({{ proj.key }})</span></div>
                  <div class="progress-bar">
                    <div class="progress-fill" [style.width.%]="proj.activeSprintProgress"></div>
                  </div>
                  <div class="project-stats">
                    <span>Health: {{ proj.healthScore }}%</span>
                    <span>Open: {{ proj.openTickets }}/{{ proj.totalTickets }}</span>
                  </div>
                </div>
              }
            </div>
          </div>

          <div class="card">
            <h3>Risk Heat Map</h3>
            <div class="risk-grid">
              @for (entry of selectedDashboard()!.riskHeatMap; track entry.projectId) {
                <div class="risk-card" [class]="entry.riskLevel.toLowerCase()">
                  <span class="risk-project">{{ entry.projectName }}</span>
                  <span class="risk-badge">{{ entry.riskLevel }} ({{ entry.riskScore }})</span>
                </div>
              }
            </div>
          </div>
        </div>
      } @else {
        <div class="portfolio-list">
          @for (portfolio of portfolios(); track portfolio.id) {
            <div class="card portfolio-card" (click)="loadDashboard(portfolio.id)">
              <h3>{{ portfolio.name }}</h3>
              <p>{{ portfolio.description }}</p>
              <div class="card-actions">
                <button class="btn-danger-sm" (click)="deletePortfolio(portfolio.id); $event.stopPropagation()">Delete</button>
              </div>
            </div>
          }
          @if (portfolios().length === 0) {
            <div class="empty-state">No portfolios yet. Create one to group your projects.</div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .portfolio-container { padding: 24px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .page-header h1 { color: #e6edf3; margin: 0; }
    .create-form { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
    .input { background: #0d1117; border: 1px solid #30363d; color: #e6edf3; padding: 8px 12px; border-radius: 6px; flex: 1; }
    .btn-primary { background: #238636; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-primary:hover { background: #2ea043; }
    .btn-back { background: #21262d; color: #e6edf3; border: 1px solid #30363d; padding: 6px 12px; border-radius: 6px; cursor: pointer; margin-left: auto; }
    .btn-danger-sm { background: #da3633; color: #fff; border: none; padding: 4px 10px; border-radius: 4px; cursor: pointer; font-size: 0.8rem; }
    .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
    .portfolio-card { cursor: pointer; transition: border-color 0.2s; }
    .portfolio-card:hover { border-color: #58a6ff; }
    .portfolio-card h3 { color: #e6edf3; margin: 0 0 8px; }
    .portfolio-card p { color: #8b949e; margin: 0; }
    .card-actions { margin-top: 12px; }
    .dashboard-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .dashboard-header h2 { color: #e6edf3; margin: 0; }
    .health-badge { padding: 4px 12px; border-radius: 12px; font-weight: 600; font-size: 0.85rem; }
    .health-badge.good { background: #23883733; color: #3fb950; }
    .health-badge.medium { background: #d2992233; color: #d29922; }
    .health-badge.bad { background: #f8514933; color: #f85149; }
    .summary-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-bottom: 24px; }
    .stat-card { text-align: center; }
    .stat-value { display: block; font-size: 2rem; font-weight: 700; color: #e6edf3; }
    .stat-label { color: #8b949e; font-size: 0.85rem; }
    h3 { color: #e6edf3; margin-bottom: 12px; }
    .project-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px; }
    .project-card { background: #0d1117; border: 1px solid #30363d; border-radius: 6px; padding: 12px; }
    .project-name { color: #e6edf3; font-weight: 600; margin-bottom: 8px; }
    .key { color: #8b949e; font-weight: 400; font-size: 0.85rem; }
    .progress-bar { background: #21262d; height: 6px; border-radius: 3px; margin-bottom: 8px; }
    .progress-fill { background: #238636; height: 100%; border-radius: 3px; transition: width 0.3s; }
    .project-stats { display: flex; justify-content: space-between; color: #8b949e; font-size: 0.8rem; }
    .risk-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
    .risk-card { padding: 12px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; }
    .risk-card.low { background: #23883720; border: 1px solid #238837; }
    .risk-card.medium { background: #d2992220; border: 1px solid #d29922; }
    .risk-card.high { background: #f8514920; border: 1px solid #f85149; }
    .risk-card.critical { background: #f8514940; border: 1px solid #f85149; }
    .risk-project { color: #e6edf3; font-weight: 500; }
    .risk-badge { font-size: 0.75rem; font-weight: 600; color: #e6edf3; }
    .empty-state { color: #8b949e; text-align: center; padding: 40px; }
  `]
})
export class PortfolioComponent implements OnInit {
  portfolios = signal<Portfolio[]>([]);
  selectedDashboard = signal<PortfolioDashboard | null>(null);
  showCreate = false;
  newName = '';
  newDescription = '';
  private orgId = '';

  constructor(private portfolioService: PortfolioService) {}

  ngOnInit() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.orgId = user.organizationId || '';
    this.loadPortfolios();
  }

  loadPortfolios() {
    if (this.orgId) {
      this.portfolioService.getByOrganization(this.orgId).subscribe(p => this.portfolios.set(p));
    }
  }

  create() {
    if (!this.newName) return;
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.portfolioService.create({ organizationId: this.orgId, name: this.newName, description: this.newDescription, ownerId: user.id })
      .subscribe(() => { this.loadPortfolios(); this.showCreate = false; this.newName = ''; this.newDescription = ''; });
  }

  loadDashboard(portfolioId: string) {
    this.portfolioService.getDashboard(portfolioId).subscribe(d => this.selectedDashboard.set(d));
  }

  deletePortfolio(id: string) {
    this.portfolioService.delete(id).subscribe(() => this.loadPortfolios());
  }

  getHealthClass(score: number): string {
    if (score >= 70) return 'good';
    if (score >= 40) return 'medium';
    return 'bad';
  }
}
