import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientPortalService } from '../../core/services/client-portal.service';
import { ClientPortalConfig, ClientUser, ClientFeedback } from '../../core/models/enterprise.model';

@Component({
  selector: 'app-client-portal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="portal-container">
      <div class="page-header">
        <h1>Client Portal</h1>
      </div>

      <div class="card config-section">
        <h3>Portal Configuration</h3>
        <div class="config-form">
          <label class="toggle-row">
            <input type="checkbox" [(ngModel)]="portalEnabled" (change)="saveConfig()">
            <span>Portal Enabled</span>
          </label>
          <textarea [(ngModel)]="welcomeMessage" placeholder="Welcome message for clients" class="input textarea"
                    (blur)="saveConfig()"></textarea>
          <div class="visibility-options">
            <label class="toggle-row">
              <input type="checkbox" [(ngModel)]="showRoadmap" (change)="saveConfig()"> Show Roadmap
            </label>
            <label class="toggle-row">
              <input type="checkbox" [(ngModel)]="showReleases" (change)="saveConfig()"> Show Releases
            </label>
            <label class="toggle-row">
              <input type="checkbox" [(ngModel)]="showChangelog" (change)="saveConfig()"> Show Changelog
            </label>
          </div>
        </div>
      </div>

      <div class="card users-section">
        <h3>Client Users ({{ clientUsers().length }})</h3>
        <div class="add-user-form">
          <input [(ngModel)]="newUserEmail" placeholder="Email" class="input">
          <input [(ngModel)]="newUserName" placeholder="Name" class="input">
          <input [(ngModel)]="newUserCompany" placeholder="Company" class="input">
          <button class="btn-primary" (click)="addUser()">Add User</button>
        </div>
        <div class="users-list">
          @for (user of clientUsers(); track user.id) {
            <div class="user-row">
              <div class="user-info">
                <span class="user-name">{{ user.name }}</span>
                <span class="user-email">{{ user.email }}</span>
                @if (user.company) {
                  <span class="user-company">{{ user.company }}</span>
                }
              </div>
              <span class="badge" [class.active]="user.active" [class.inactive]="!user.active">
                {{ user.active ? 'Active' : 'Inactive' }}
              </span>
              <button class="btn-danger-sm" (click)="removeUser(user.id)">Remove</button>
            </div>
          }
        </div>
      </div>

      <div class="card feedback-section">
        <h3>Client Feedback</h3>
        @for (fb of feedback(); track fb.id) {
          <div class="feedback-item">
            <div class="feedback-header">
              <span class="feedback-type">{{ fb.type }}</span>
              @if (fb.rating) {
                <span class="feedback-rating">{{ '★'.repeat(fb.rating) }}{{ '☆'.repeat(5 - fb.rating) }}</span>
              }
              <span class="feedback-date">{{ fb.createdAt | date:'short' }}</span>
            </div>
            <p class="feedback-content">{{ fb.content }}</p>
          </div>
        }
        @if (feedback().length === 0) {
          <div class="empty-state">No feedback received yet.</div>
        }
      </div>
    </div>
  `,
  styles: [`
    .portal-container { padding: 24px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .page-header h1 { color: #e6edf3; margin: 0; }
    .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
    h3 { color: #e6edf3; margin: 0 0 12px; }
    .config-form { display: flex; flex-direction: column; gap: 12px; }
    .toggle-row { color: #e6edf3; display: flex; align-items: center; gap: 8px; cursor: pointer; }
    .visibility-options { display: flex; gap: 24px; flex-wrap: wrap; }
    .input { background: #0d1117; border: 1px solid #30363d; color: #e6edf3; padding: 8px 12px; border-radius: 6px; }
    .textarea { min-height: 60px; resize: vertical; width: 100%; }
    .btn-primary { background: #238636; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-danger-sm { background: #da3633; color: #fff; border: none; padding: 4px 10px; border-radius: 4px; cursor: pointer; font-size: 0.8rem; }
    .add-user-form { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
    .users-list { display: flex; flex-direction: column; gap: 8px; }
    .user-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #21262d; }
    .user-info { display: flex; flex-direction: column; flex: 1; }
    .user-name { color: #e6edf3; font-weight: 500; }
    .user-email { color: #8b949e; font-size: 0.85rem; }
    .user-company { color: #8b949e; font-size: 0.8rem; }
    .badge { padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
    .badge.active { background: #23883733; color: #3fb950; }
    .badge.inactive { background: #8b949e33; color: #8b949e; }
    .feedback-item { padding: 12px 0; border-bottom: 1px solid #21262d; }
    .feedback-header { display: flex; gap: 12px; align-items: center; margin-bottom: 4px; }
    .feedback-type { color: #58a6ff; font-weight: 600; font-size: 0.8rem; text-transform: uppercase; }
    .feedback-rating { color: #d29922; }
    .feedback-date { color: #8b949e; font-size: 0.75rem; margin-left: auto; }
    .feedback-content { color: #e6edf3; margin: 0; }
    .empty-state { color: #8b949e; text-align: center; padding: 20px; }
  `]
})
export class ClientPortalComponent implements OnInit {
  portalConfig = signal<ClientPortalConfig | null>(null);
  clientUsers = signal<ClientUser[]>([]);
  feedback = signal<ClientFeedback[]>([]);

  portalEnabled = false;
  welcomeMessage = '';
  showRoadmap = true;
  showReleases = true;
  showChangelog = true;

  newUserEmail = '';
  newUserName = '';
  newUserCompany = '';
  private projectId = '';

  constructor(private portalService: ClientPortalService) {}

  ngOnInit() {
    this.projectId = localStorage.getItem('selectedProjectId') || '';
    if (this.projectId) {
      this.loadConfig();
      this.loadUsers();
      this.loadFeedback();
    }
  }

  loadConfig() {
    this.portalService.getConfig(this.projectId).subscribe({
      next: config => {
        this.portalConfig.set(config);
        this.portalEnabled = config.enabled;
        this.welcomeMessage = config.welcomeMessage || '';
        this.showRoadmap = config.showRoadmap;
        this.showReleases = config.showReleases;
        this.showChangelog = config.showChangelog;
      },
      error: () => {}
    });
  }

  saveConfig() {
    this.portalService.configure(this.projectId, {
      isEnabled: this.portalEnabled, welcomeMessage: this.welcomeMessage,
      showRoadmap: this.showRoadmap, showReleases: this.showReleases, showChangelog: this.showChangelog
    }).subscribe(config => this.portalConfig.set(config));
  }

  loadUsers() {
    this.portalService.getUsers(this.projectId).subscribe(u => this.clientUsers.set(u));
  }

  loadFeedback() {
    this.portalService.getFeedback(this.projectId).subscribe(f => this.feedback.set(f));
  }

  addUser() {
    if (!this.newUserEmail || !this.newUserName) return;
    this.portalService.addUser(this.projectId, { email: this.newUserEmail, name: this.newUserName, company: this.newUserCompany || undefined })
      .subscribe(() => { this.loadUsers(); this.newUserEmail = ''; this.newUserName = ''; this.newUserCompany = ''; });
  }

  removeUser(id: string) {
    this.portalService.removeUser(id).subscribe(() => this.loadUsers());
  }
}
