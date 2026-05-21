import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditService } from '../../core/services/audit.service';
import { AuditEvent, AuditSummary, AuditAlertRule } from '../../core/models/enterprise.model';

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="audit-container">
      <div class="page-header">
        <h1>Audit Trail</h1>
        <button class="btn-export" (click)="exportLog()">Export CSV</button>
      </div>

      @if (summary()) {
        <div class="summary-cards">
          <div class="card stat-card">
            <span class="stat-value">{{ summary()!.totalEvents }}</span>
            <span class="stat-label">Total Events</span>
          </div>
          <div class="card stat-card critical">
            <span class="stat-value">{{ summary()!.criticalCount }}</span>
            <span class="stat-label">Critical</span>
          </div>
          <div class="card stat-card">
            <span class="stat-value">{{ summary()!.todayEvents }}</span>
            <span class="stat-label">Today</span>
          </div>
        </div>
      }

      <div class="filters">
        <select [(ngModel)]="filterAction" (change)="loadEvents()">
          <option value="">All Actions</option>
          <option value="CREATE">Create</option>
          <option value="UPDATE">Update</option>
          <option value="DELETE">Delete</option>
          <option value="LOGIN">Login</option>
          <option value="LOGOUT">Logout</option>
          <option value="PERMISSION_CHANGE">Permission Change</option>
          <option value="ACCESS_DENIED">Access Denied</option>
        </select>
        <select [(ngModel)]="filterSeverity" (change)="loadEvents()">
          <option value="">All Severities</option>
          <option value="INFO">Info</option>
          <option value="WARNING">Warning</option>
          <option value="CRITICAL">Critical</option>
        </select>
        <input type="date" [(ngModel)]="filterFrom" (change)="loadEvents()" placeholder="From">
        <input type="date" [(ngModel)]="filterTo" (change)="loadEvents()" placeholder="To">
      </div>

      <div class="card">
        <table class="audit-table">
          <thead>
            <tr>
              <th>Time</th>
              <th>Action</th>
              <th>Entity</th>
              <th>Severity</th>
              <th>IP Address</th>
            </tr>
          </thead>
          <tbody>
            @for (event of events(); track event.id) {
              <tr>
                <td>{{ event.createdAt | date:'short' }}</td>
                <td><span class="badge action">{{ event.action }}</span></td>
                <td>{{ event.entityType }}</td>
                <td><span class="badge" [class]="event.severity.toLowerCase()">{{ event.severity }}</span></td>
                <td>{{ event.ipAddress }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      <div class="card alert-rules-section">
        <h3>Alert Rules</h3>
        <div class="rule-form">
          <input [(ngModel)]="newRuleName" placeholder="Rule name">
          <input [(ngModel)]="newRulePattern" placeholder="Action pattern (e.g. DELETE*)">
          <select [(ngModel)]="newRuleSeverity">
            <option value="WARNING">Warning</option>
            <option value="CRITICAL">Critical</option>
          </select>
          <input [(ngModel)]="newRuleEmails" placeholder="Notify emails (comma-separated)">
          <button class="btn-primary" (click)="createRule()">Add Rule</button>
        </div>
        @for (rule of alertRules(); track rule.id) {
          <div class="rule-item">
            <span class="rule-name">{{ rule.name }}</span>
            <span class="badge" [class]="rule.severity.toLowerCase()">{{ rule.actionPattern }}</span>
            <button class="btn-danger-sm" (click)="deleteRule(rule.id)">Delete</button>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .audit-container { padding: 24px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .page-header h1 { color: #e6edf3; margin: 0; }
    .summary-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
    .stat-card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px; text-align: center; }
    .stat-card.critical { border-color: #f85149; }
    .stat-value { display: block; font-size: 2rem; font-weight: 700; color: #e6edf3; }
    .stat-label { color: #8b949e; font-size: 0.85rem; }
    .filters { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
    .filters select, .filters input { background: #0d1117; border: 1px solid #30363d; color: #e6edf3; padding: 8px 12px; border-radius: 6px; }
    .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
    .audit-table { width: 100%; border-collapse: collapse; }
    .audit-table th { text-align: left; padding: 10px; color: #8b949e; border-bottom: 1px solid #30363d; }
    .audit-table td { padding: 10px; color: #e6edf3; border-bottom: 1px solid #21262d; }
    .badge { padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
    .badge.info { background: #1f6feb33; color: #58a6ff; }
    .badge.warning { background: #d2992233; color: #d29922; }
    .badge.critical { background: #f8514933; color: #f85149; }
    .badge.action { background: #23883733; color: #3fb950; }
    .btn-export { background: #21262d; color: #e6edf3; border: 1px solid #30363d; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-export:hover { background: #30363d; }
    .btn-primary { background: #238636; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-primary:hover { background: #2ea043; }
    .btn-danger-sm { background: #da3633; color: #fff; border: none; padding: 4px 10px; border-radius: 4px; cursor: pointer; font-size: 0.8rem; }
    .alert-rules-section h3 { color: #e6edf3; margin-bottom: 12px; }
    .rule-form { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
    .rule-form input, .rule-form select { background: #0d1117; border: 1px solid #30363d; color: #e6edf3; padding: 8px; border-radius: 6px; }
    .rule-item { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #21262d; }
    .rule-name { color: #e6edf3; flex: 1; }
  `]
})
export class AuditComponent implements OnInit {
  events = signal<AuditEvent[]>([]);
  summary = signal<AuditSummary | null>(null);
  alertRules = signal<AuditAlertRule[]>([]);

  filterAction = '';
  filterSeverity = '';
  filterFrom = '';
  filterTo = '';

  newRuleName = '';
  newRulePattern = '';
  newRuleSeverity = 'WARNING';
  newRuleEmails = '';

  private orgId = '';

  constructor(private auditService: AuditService) {}

  ngOnInit() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.orgId = user.organizationId || '';
    if (this.orgId) {
      this.loadEvents();
      this.loadSummary();
      this.loadRules();
    }
  }

  loadEvents() {
    this.auditService.getAuditLog(this.orgId, {
      action: this.filterAction || undefined,
      severity: this.filterSeverity || undefined,
      fromDate: this.filterFrom || undefined,
      toDate: this.filterTo || undefined
    }).subscribe(events => this.events.set(events));
  }

  loadSummary() {
    this.auditService.getSummary(this.orgId).subscribe(s => this.summary.set(s));
  }

  loadRules() {
    this.auditService.getAlertRules(this.orgId).subscribe(r => this.alertRules.set(r));
  }

  createRule() {
    if (!this.newRuleName || !this.newRulePattern) return;
    this.auditService.createAlertRule(this.orgId, {
      name: this.newRuleName, actionPattern: this.newRulePattern,
      severity: this.newRuleSeverity, notifyEmails: this.newRuleEmails
    }).subscribe(() => {
      this.loadRules();
      this.newRuleName = ''; this.newRulePattern = ''; this.newRuleEmails = '';
    });
  }

  deleteRule(id: string) {
    this.auditService.deleteAlertRule(id).subscribe(() => this.loadRules());
  }

  exportLog() {
    this.auditService.exportAuditLog(this.orgId, this.filterFrom, this.filterTo).subscribe(events => {
      const csv = ['Time,Action,Entity,Severity,IP'].concat(
        events.map(e => `${e.createdAt},${e.action},${e.entityType},${e.severity},${e.ipAddress}`)
      ).join('\n');
      const blob = new Blob([csv], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a'); a.href = url; a.download = 'audit-log.csv'; a.click();
    });
  }
}
