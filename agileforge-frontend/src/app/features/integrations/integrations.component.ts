import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IntegrationService } from '../../core/services/integration.service';
import { IntegrationConfig, JiraImportJob } from '../../core/models/ecosystem.model';

@Component({
  selector: 'app-integrations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="integrations-container">
      <header class="page-header">
        <h1>Integrations</h1>
        <p class="subtitle">Connect Slack, Teams, and import from Jira</p>
      </header>

      <div class="tabs">
        <button [class.active]="activeTab() === 'connections'" (click)="activeTab.set('connections')">Connections</button>
        <button [class.active]="activeTab() === 'jira-import'" (click)="activeTab.set('jira-import')">Jira Import</button>
      </div>

      @if (activeTab() === 'connections') {
        <div class="providers-grid">
          <div class="provider-card">
            <div class="provider-header">
              <span class="provider-icon">💬</span>
              <h3>Slack</h3>
            </div>
            <p>Send notifications to Slack channels when tickets are updated, sprints change, or deployments happen.</p>
            <div class="provider-features">
              <span>Ticket notifications</span>
              <span>Sprint updates</span>
              <span>Deployment alerts</span>
            </div>
            <button class="btn-connect" (click)="connectProvider('SLACK')">
              {{ isConnected('SLACK') ? 'Configure' : 'Connect' }}
            </button>
          </div>

          <div class="provider-card">
            <div class="provider-header">
              <span class="provider-icon">👥</span>
              <h3>Microsoft Teams</h3>
            </div>
            <p>Integrate with Teams for notifications, daily standup summaries, and sprint reports.</p>
            <div class="provider-features">
              <span>Adaptive cards</span>
              <span>Bot commands</span>
              <span>Channel feeds</span>
            </div>
            <button class="btn-connect" (click)="connectProvider('TEAMS')">
              {{ isConnected('TEAMS') ? 'Configure' : 'Connect' }}
            </button>
          </div>

          <div class="provider-card">
            <div class="provider-header">
              <span class="provider-icon">🎮</span>
              <h3>Discord</h3>
            </div>
            <p>Get project updates in your Discord server. Perfect for open-source and indie teams.</p>
            <div class="provider-features">
              <span>Webhook notifications</span>
              <span>Bot commands</span>
              <span>Embeds</span>
            </div>
            <button class="btn-connect" (click)="connectProvider('DISCORD')">
              {{ isConnected('DISCORD') ? 'Configure' : 'Connect' }}
            </button>
          </div>
        </div>

        @if (integrations().length) {
          <h3 class="section-title">Active Integrations</h3>
          <div class="active-integrations">
            @for (integration of integrations(); track integration.id) {
              <div class="integration-row">
                <span class="integration-provider">{{ integration.provider }}</span>
                <span class="integration-status" [class.enabled]="integration.enabled">
                  {{ integration.enabled ? 'Enabled' : 'Disabled' }}
                </span>
                <div class="integration-actions">
                  <button (click)="testConnection(integration.id)">Test</button>
                  <button (click)="toggleIntegration(integration)">{{ integration.enabled ? 'Disable' : 'Enable' }}</button>
                  <button class="btn-danger" (click)="deleteIntegration(integration.id)">Delete</button>
                </div>
              </div>
            }
          </div>
        }
      }

      @if (activeTab() === 'jira-import') {
        <div class="jira-import-section">
          <div class="import-form">
            <h3>Import from Jira</h3>
            <p>Migrate your existing Jira projects to AgileForge. We'll import tickets, sprints, and metadata.</p>

            <div class="form-group">
              <label>Jira URL</label>
              <input type="url" [(ngModel)]="jiraUrl" placeholder="https://your-domain.atlassian.net" />
            </div>
            <div class="form-group">
              <label>Project Key</label>
              <input type="text" [(ngModel)]="jiraProjectKey" placeholder="PROJ" />
            </div>
            <div class="form-group">
              <label>API Token</label>
              <input type="password" [(ngModel)]="jiraApiToken" placeholder="Your Jira API token" />
            </div>
            <div class="form-group">
              <label>Email</label>
              <input type="email" [(ngModel)]="jiraEmail" placeholder="your@email.com" />
            </div>

            <div class="form-actions">
              <button class="btn-secondary" (click)="previewImport()">Preview</button>
              <button class="btn-primary" (click)="startImport()" [disabled]="!canImport()">Start Import</button>
            </div>
          </div>

          @if (importJobs().length) {
            <h3 class="section-title">Import History</h3>
            <div class="import-history">
              @for (job of importJobs(); track job.id) {
                <div class="import-job">
                  <div class="job-info">
                    <strong>{{ job.jiraProjectKey }}</strong>
                    <span class="job-url">{{ job.jiraUrl }}</span>
                  </div>
                  <div class="job-progress">
                    @if (job.status === 'IN_PROGRESS') {
                      <div class="progress-bar">
                        <div class="progress-fill" [style.width.%]="(job.importedItems / job.totalItems) * 100"></div>
                      </div>
                      <span>{{ job.importedItems }}/{{ job.totalItems }}</span>
                    } @else {
                      <span class="job-status" [class]="job.status.toLowerCase()">{{ job.status }}</span>
                    }
                  </div>
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .integrations-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
    .page-header h1 { margin: 0; font-size: 24px; }
    .subtitle { color: #6b7280; margin: 4px 0 24px; }
    .tabs { display: flex; gap: 8px; margin-bottom: 24px; border-bottom: 1px solid #e5e7eb; padding-bottom: 8px; }
    .tabs button { padding: 8px 16px; border: none; background: none; cursor: pointer; border-radius: 6px; font-weight: 500; }
    .tabs button.active { background: #4f46e5; color: white; }
    .providers-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; margin-bottom: 32px; }
    .provider-card { padding: 24px; border: 2px solid #e5e7eb; border-radius: 12px; }
    .provider-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
    .provider-icon { font-size: 28px; }
    .provider-card h3 { margin: 0; }
    .provider-card p { font-size: 13px; color: #6b7280; margin: 0 0 12px; }
    .provider-features { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 16px; }
    .provider-features span { font-size: 11px; padding: 3px 8px; background: #f3f4f6; border-radius: 4px; }
    .btn-connect { width: 100%; padding: 10px; background: #4f46e5; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 500; }
    .section-title { margin: 32px 0 16px; }
    .integration-row { display: flex; align-items: center; gap: 16px; padding: 12px 16px; background: #f9fafb; border-radius: 8px; margin-bottom: 8px; }
    .integration-provider { font-weight: 600; width: 100px; }
    .integration-status { font-size: 12px; padding: 3px 8px; border-radius: 4px; background: #fee2e2; color: #dc2626; }
    .integration-status.enabled { background: #dcfce7; color: #16a34a; }
    .integration-actions { margin-left: auto; display: flex; gap: 8px; }
    .integration-actions button { padding: 4px 10px; border: 1px solid #d1d5db; background: white; border-radius: 4px; cursor: pointer; font-size: 12px; }
    .btn-danger { color: #dc2626 !important; border-color: #fecaca !important; }
    .import-form { padding: 24px; background: #f9fafb; border-radius: 12px; max-width: 500px; }
    .import-form h3 { margin: 0 0 8px; }
    .import-form > p { font-size: 13px; color: #6b7280; margin: 0 0 20px; }
    .form-group { margin-bottom: 16px; }
    .form-group label { display: block; font-size: 13px; font-weight: 500; margin-bottom: 4px; }
    .form-group input { width: 100%; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; box-sizing: border-box; }
    .form-actions { display: flex; gap: 12px; }
    .btn-primary { padding: 10px 20px; background: #4f46e5; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 500; }
    .btn-primary:disabled { background: #d1d5db; cursor: not-allowed; }
    .btn-secondary { padding: 10px 20px; background: white; color: #374151; border: 1px solid #d1d5db; border-radius: 8px; cursor: pointer; }
    .import-job { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #f9fafb; border-radius: 8px; margin-bottom: 8px; }
    .job-url { font-size: 12px; color: #9ca3af; display: block; }
    .progress-bar { width: 120px; height: 6px; background: #e5e7eb; border-radius: 3px; }
    .progress-fill { height: 100%; background: #4f46e5; border-radius: 3px; }
    .job-status { font-size: 12px; padding: 3px 8px; border-radius: 4px; }
    .job-status.completed { background: #dcfce7; color: #16a34a; }
    .job-status.failed { background: #fee2e2; color: #dc2626; }
  `]
})
export class IntegrationsComponent implements OnInit {
  private integrationService = inject(IntegrationService);

  activeTab = signal<'connections' | 'jira-import'>('connections');
  integrations = signal<IntegrationConfig[]>([]);
  importJobs = signal<JiraImportJob[]>([]);

  jiraUrl = '';
  jiraProjectKey = '';
  jiraApiToken = '';
  jiraEmail = '';

  ngOnInit() {
    this.integrationService.getByOrganization('current-org').subscribe(i => this.integrations.set(i));
    this.integrationService.getImportHistory('current-org').subscribe(j => this.importJobs.set(j));
  }

  isConnected(provider: string): boolean {
    return this.integrations().some(i => i.provider === provider && i.enabled);
  }

  connectProvider(provider: string) {
    this.integrationService.configure('current-org', provider, {}).subscribe(config => {
      this.integrations.update(list => [...list, config]);
    });
  }

  toggleIntegration(integration: IntegrationConfig) {
    const action = integration.enabled
      ? this.integrationService.disable(integration.id)
      : this.integrationService.enable(integration.id);
    action.subscribe(() => {
      this.integrations.update(list => list.map(i =>
        i.id === integration.id ? { ...i, enabled: !i.enabled } : i
      ));
    });
  }

  testConnection(integrationId: string) {
    this.integrationService.testConnection(integrationId).subscribe();
  }

  deleteIntegration(integrationId: string) {
    this.integrationService.delete(integrationId).subscribe(() => {
      this.integrations.update(list => list.filter(i => i.id !== integrationId));
    });
  }

  canImport(): boolean {
    return !!(this.jiraUrl && this.jiraProjectKey && this.jiraApiToken && this.jiraEmail);
  }

  previewImport() {
    this.integrationService.previewJiraImport(this.jiraUrl, this.jiraProjectKey, this.jiraApiToken, this.jiraEmail).subscribe();
  }

  startImport() {
    this.integrationService.startJiraImport('current-org', 'target-project', this.jiraUrl, this.jiraProjectKey, this.jiraApiToken, this.jiraEmail).subscribe();
  }
}
