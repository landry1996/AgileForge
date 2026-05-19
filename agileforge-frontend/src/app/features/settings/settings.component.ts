import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LabelService } from '../../core/services/label.service';
import { Label, CreateLabelRequest } from '../../core/models/label.model';
import { WorkflowService } from '../../core/services/workflow.service';
import { Workflow } from '../../core/models/workflow.model';
import { InvitationService } from '../../core/services/invitation.service';
import { Invitation, InviteRequest } from '../../core/models/invitation.model';
import { GitIntegrationService } from '../../core/services/git-integration.service';
import { GitRepository, ConnectRepositoryRequest } from '../../core/models/git.model';

type SettingsTab = 'labels' | 'workflows' | 'invitations' | 'integrations';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="settings-page">
      <div class="page-header">
        <h1>Project Settings</h1>
      </div>

      <!-- Tabs -->
      <div class="tabs">
        <button class="tab" [class.active]="activeTab() === 'labels'" (click)="activeTab.set('labels')">Labels</button>
        <button class="tab" [class.active]="activeTab() === 'workflows'" (click)="activeTab.set('workflows')">Workflows</button>
        <button class="tab" [class.active]="activeTab() === 'invitations'" (click)="activeTab.set('invitations')">Invitations</button>
        <button class="tab" [class.active]="activeTab() === 'integrations'" (click)="activeTab.set('integrations')">Integrations</button>
      </div>

      <!-- Labels Tab -->
      @if (activeTab() === 'labels') {
        <div class="tab-content">
          <div class="section-header">
            <h2>Project Labels</h2>
          </div>

          <!-- Create Label Form -->
          <form class="inline-form" (submit)="createLabel($event)">
            <input type="text" [(ngModel)]="newLabelName" name="labelName"
                   placeholder="Label name" required>
            <input type="color" [(ngModel)]="newLabelColor" name="labelColor" class="color-input">
            <button type="submit" class="btn-primary" [disabled]="creatingLabel()">Create</button>
          </form>

          @if (loadingLabels()) {
            <p class="loading-text">Loading labels...</p>
          } @else if (labels().length === 0) {
            <p class="placeholder">No labels defined yet.</p>
          } @else {
            <div class="labels-list">
              @for (label of labels(); track label.id) {
                <div class="label-row">
                  <div class="label-info">
                    <span class="label-dot" [style.background-color]="label.color"></span>
                    <span class="label-name">{{ label.name }}</span>
                  </div>
                  <button class="btn-icon-delete" (click)="deleteLabel(label)" title="Delete label">x</button>
                </div>
              }
            </div>
          }
        </div>
      }

      <!-- Workflows Tab -->
      @if (activeTab() === 'workflows') {
        <div class="tab-content">
          <div class="section-header">
            <h2>Workflows</h2>
          </div>

          @if (loadingWorkflows()) {
            <p class="loading-text">Loading workflows...</p>
          } @else if (workflows().length === 0) {
            <p class="placeholder">No workflows configured.</p>
          } @else {
            @for (workflow of workflows(); track workflow.id) {
              <div class="workflow-card">
                <div class="workflow-header">
                  <span class="workflow-type">{{ workflow.ticketType }}</span>
                  <span class="workflow-name">{{ workflow.name }}</span>
                </div>
                <div class="workflow-pipeline">
                  @for (status of workflow.statuses; track status.name; let last = $last) {
                    <div class="pipeline-status" [class]="'category-' + status.category.toLowerCase()">
                      {{ status.name }}
                    </div>
                    @if (!last) {
                      <span class="pipeline-arrow">-></span>
                    }
                  }
                </div>
              </div>
            }
          }
        </div>
      }

      <!-- Invitations Tab -->
      @if (activeTab() === 'invitations') {
        <div class="tab-content">
          <div class="section-header">
            <h2>Invite Members</h2>
          </div>

          <!-- Invite Form -->
          <form class="inline-form" (submit)="sendInvitation($event)">
            <input type="email" [(ngModel)]="inviteEmail" name="email"
                   placeholder="Email address" required>
            <select [(ngModel)]="inviteRole" name="role">
              <option value="MEMBER">Member</option>
              <option value="ADMIN">Admin</option>
              <option value="VIEWER">Viewer</option>
            </select>
            <button type="submit" class="btn-primary" [disabled]="sendingInvite()">Invite</button>
          </form>

          @if (loadingInvitations()) {
            <p class="loading-text">Loading invitations...</p>
          } @else if (invitations().length === 0) {
            <p class="placeholder">No pending invitations.</p>
          } @else {
            <div class="invitations-list">
              <h3>Pending Invitations</h3>
              @for (invite of invitations(); track invite.id) {
                <div class="invitation-row">
                  <div class="invitation-info">
                    <span class="invitation-email">{{ invite.email }}</span>
                    <span class="invitation-role">{{ invite.role }}</span>
                    <span class="invitation-status status-{{ invite.status.toLowerCase() }}">{{ invite.status }}</span>
                  </div>
                  <div class="invitation-actions">
                    @if (invite.status === 'PENDING') {
                      <button class="btn-small" (click)="resendInvitation(invite)">Resend</button>
                      <button class="btn-small btn-small-danger" (click)="cancelInvitation(invite)">Cancel</button>
                    }
                  </div>
                </div>
              }
            </div>
          }
        </div>
      }

      <!-- Integrations Tab -->
      @if (activeTab() === 'integrations') {
        <div class="tab-content">
          <div class="section-header">
            <h2>GitHub Integration</h2>
          </div>

          <!-- Connect Repo Form -->
          <form class="inline-form" (submit)="connectRepo($event)">
            <input type="text" [(ngModel)]="repoOwner" name="repoOwner"
                   placeholder="Owner (e.g. organization)" required>
            <input type="text" [(ngModel)]="repoName" name="repoName"
                   placeholder="Repository name" required class="input-wide">
            <button type="submit" class="btn-primary" [disabled]="connectingRepo()">Connect</button>
          </form>

          @if (loadingRepos()) {
            <p class="loading-text">Loading repositories...</p>
          } @else if (repos().length === 0) {
            <p class="placeholder">No repositories connected.</p>
          } @else {
            <div class="repos-list">
              <h3>Connected Repositories</h3>
              @for (repo of repos(); track repo.id) {
                <div class="repo-row">
                  <div class="repo-info">
                    <span class="repo-provider">{{ repo.provider }}</span>
                    <span class="repo-name">{{ repo.owner }}/{{ repo.repoName }}</span>
                    <span class="repo-branch">{{ repo.defaultBranch }}</span>
                  </div>
                  <button class="btn-small btn-small-danger" (click)="disconnectRepo(repo)">Disconnect</button>
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .settings-page { max-width: 900px; }
    .page-header { margin-bottom: 24px; }
    .page-header h1 { color: #e1e4e8; font-size: 1.6rem; margin: 0; }

    .tabs {
      display: flex; gap: 0; border-bottom: 1px solid #30363d; margin-bottom: 24px;
    }
    .tab {
      background: none; border: none; color: #8b949e; padding: 12px 20px;
      font-size: 0.9rem; cursor: pointer; border-bottom: 2px solid transparent;
      transition: all 0.2s;
    }
    .tab:hover { color: #e1e4e8; }
    .tab.active { color: #58a6ff; border-bottom-color: #58a6ff; }

    .tab-content {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 24px;
    }
    .section-header { margin-bottom: 16px; }
    .section-header h2 { color: #e1e4e8; font-size: 1.1rem; margin: 0; }

    .inline-form {
      display: flex; gap: 8px; align-items: center; margin-bottom: 20px;
      padding-bottom: 20px; border-bottom: 1px solid #21262d;
    }
    .inline-form input, .inline-form select {
      background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      color: #c9d1d9; padding: 8px 12px; font-size: 0.9rem;
    }
    .inline-form input:focus, .inline-form select:focus {
      outline: none; border-color: #58a6ff;
    }
    .input-wide { flex: 1; }
    .color-input { width: 40px; height: 36px; padding: 2px; cursor: pointer; }

    .btn-primary {
      background: #238636; border: none; color: white; padding: 8px 16px;
      border-radius: 6px; cursor: pointer; font-size: 0.85rem; font-weight: 500;
      white-space: nowrap;
    }
    .btn-primary:hover { background: #2ea043; }
    .btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

    .btn-small {
      background: #21262d; border: 1px solid #30363d; color: #c9d1d9;
      padding: 4px 10px; border-radius: 4px; cursor: pointer; font-size: 0.8rem;
    }
    .btn-small:hover { background: #30363d; }
    .btn-small-danger { color: #f85149; border-color: #f8514933; }
    .btn-small-danger:hover { background: #f8514922; }

    .btn-icon-delete {
      background: none; border: 1px solid #30363d; color: #8b949e;
      width: 24px; height: 24px; border-radius: 4px; cursor: pointer;
      font-size: 0.75rem; display: inline-flex; align-items: center; justify-content: center;
    }
    .btn-icon-delete:hover { color: #f85149; border-color: #f85149; }

    /* Labels */
    .labels-list { display: flex; flex-direction: column; gap: 8px; }
    .label-row {
      display: flex; justify-content: space-between; align-items: center;
      padding: 8px 12px; border-radius: 6px; background: #0d1117;
    }
    .label-info { display: flex; align-items: center; gap: 10px; }
    .label-dot { width: 12px; height: 12px; border-radius: 50%; }
    .label-name { font-size: 0.9rem; color: #c9d1d9; }

    /* Workflows */
    .workflow-card {
      background: #0d1117; border: 1px solid #21262d; border-radius: 6px;
      padding: 16px; margin-bottom: 12px;
    }
    .workflow-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
    .workflow-type {
      background: #1f6feb33; color: #58a6ff; padding: 2px 8px;
      border-radius: 4px; font-size: 0.75rem; font-weight: 600;
    }
    .workflow-name { color: #e1e4e8; font-size: 0.9rem; font-weight: 500; }
    .workflow-pipeline { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .pipeline-status {
      padding: 4px 10px; border-radius: 4px; font-size: 0.75rem; font-weight: 500;
    }
    .pipeline-arrow { color: #484f58; font-size: 0.8rem; }
    .category-todo { background: #8b949e22; color: #8b949e; }
    .category-in_progress { background: #1f6feb33; color: #58a6ff; }
    .category-done { background: #23863633; color: #3fb950; }

    /* Invitations */
    .invitations-list h3 { color: #e1e4e8; font-size: 0.95rem; margin: 0 0 12px 0; }
    .invitation-row {
      display: flex; justify-content: space-between; align-items: center;
      padding: 10px 12px; border-radius: 6px; background: #0d1117; margin-bottom: 8px;
    }
    .invitation-info { display: flex; align-items: center; gap: 12px; }
    .invitation-email { color: #c9d1d9; font-size: 0.9rem; }
    .invitation-role {
      background: #21262d; color: #8b949e; padding: 2px 8px;
      border-radius: 4px; font-size: 0.75rem;
    }
    .invitation-status { font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; }
    .status-pending { background: #d2992222; color: #d29922; }
    .status-accepted { background: #23863633; color: #3fb950; }
    .status-expired { background: #8b949e22; color: #8b949e; }
    .status-cancelled { background: #f8514922; color: #f85149; }
    .invitation-actions { display: flex; gap: 6px; }

    /* Repos */
    .repos-list h3 { color: #e1e4e8; font-size: 0.95rem; margin: 0 0 12px 0; }
    .repo-row {
      display: flex; justify-content: space-between; align-items: center;
      padding: 10px 12px; border-radius: 6px; background: #0d1117; margin-bottom: 8px;
    }
    .repo-info { display: flex; align-items: center; gap: 12px; }
    .repo-provider {
      background: #21262d; color: #8b949e; padding: 2px 8px;
      border-radius: 4px; font-size: 0.75rem; font-weight: 600;
    }
    .repo-name { color: #58a6ff; font-size: 0.9rem; }
    .repo-branch { color: #8b949e; font-size: 0.8rem; }

    .loading-text { color: #8b949e; font-size: 0.9rem; }
    .placeholder { color: #484f58; font-size: 0.9rem; }
  `]
})
export class SettingsComponent implements OnInit {
  private labelService = inject(LabelService);
  private workflowService = inject(WorkflowService);
  private invitationService = inject(InvitationService);
  private gitIntegrationService = inject(GitIntegrationService);

  private projectId = 'demo';

  // Tab state
  activeTab = signal<SettingsTab>('labels');

  // Labels state
  labels = signal<Label[]>([]);
  loadingLabels = signal(false);
  creatingLabel = signal(false);
  newLabelName = '';
  newLabelColor = '#58a6ff';

  // Workflows state
  workflows = signal<Workflow[]>([]);
  loadingWorkflows = signal(false);

  // Invitations state
  invitations = signal<Invitation[]>([]);
  loadingInvitations = signal(false);
  sendingInvite = signal(false);
  inviteEmail = '';
  inviteRole = 'MEMBER';

  // Integrations state
  repos = signal<GitRepository[]>([]);
  loadingRepos = signal(false);
  connectingRepo = signal(false);
  repoProvider = 'GITHUB';
  repoOwner = '';
  repoName = '';

  ngOnInit(): void {
    this.loadLabels();
    this.loadWorkflows();
    this.loadInvitations();
    this.loadRepos();
  }

  // Labels
  loadLabels(): void {
    this.loadingLabels.set(true);
    this.labelService.getProjectLabels(this.projectId).subscribe({
      next: (labels) => { this.labels.set(labels); this.loadingLabels.set(false); },
      error: () => this.loadingLabels.set(false)
    });
  }

  createLabel(event: Event): void {
    event.preventDefault();
    if (!this.newLabelName) return;

    this.creatingLabel.set(true);
    const request: CreateLabelRequest = { name: this.newLabelName, color: this.newLabelColor };
    this.labelService.createLabel(this.projectId, request).subscribe({
      next: (label) => {
        this.labels.update(labels => [...labels, label]);
        this.newLabelName = '';
        this.newLabelColor = '#58a6ff';
        this.creatingLabel.set(false);
      },
      error: () => this.creatingLabel.set(false)
    });
  }

  deleteLabel(label: Label): void {
    this.labelService.deleteLabel(label.id).subscribe({
      next: () => this.labels.update(labels => labels.filter(l => l.id !== label.id)),
      error: () => {}
    });
  }

  // Workflows
  loadWorkflows(): void {
    this.loadingWorkflows.set(true);
    this.workflowService.getByProject(this.projectId).subscribe({
      next: (workflows) => { this.workflows.set(workflows); this.loadingWorkflows.set(false); },
      error: () => this.loadingWorkflows.set(false)
    });
  }

  // Invitations
  loadInvitations(): void {
    this.loadingInvitations.set(true);
    this.invitationService.getPending(this.projectId).subscribe({
      next: (invitations) => { this.invitations.set(invitations); this.loadingInvitations.set(false); },
      error: () => this.loadingInvitations.set(false)
    });
  }

  sendInvitation(event: Event): void {
    event.preventDefault();
    if (!this.inviteEmail) return;

    this.sendingInvite.set(true);
    const request: InviteRequest = { email: this.inviteEmail, role: this.inviteRole };
    this.invitationService.invite(this.projectId, request).subscribe({
      next: (invitation) => {
        this.invitations.update(list => [...list, invitation]);
        this.inviteEmail = '';
        this.sendingInvite.set(false);
      },
      error: () => this.sendingInvite.set(false)
    });
  }

  resendInvitation(invite: Invitation): void {
    this.invitationService.resend(invite.id).subscribe({
      next: () => {},
      error: () => {}
    });
  }

  cancelInvitation(invite: Invitation): void {
    this.invitationService.cancel(invite.id).subscribe({
      next: () => this.invitations.update(list => list.filter(i => i.id !== invite.id)),
      error: () => {}
    });
  }

  // Integrations
  loadRepos(): void {
    this.loadingRepos.set(true);
    this.gitIntegrationService.getRepositories(this.projectId).subscribe({
      next: (repos) => { this.repos.set(repos); this.loadingRepos.set(false); },
      error: () => this.loadingRepos.set(false)
    });
  }

  connectRepo(event: Event): void {
    event.preventDefault();
    if (!this.repoOwner || !this.repoName) return;

    this.connectingRepo.set(true);
    const request: ConnectRepositoryRequest = { owner: this.repoOwner, repoName: this.repoName };
    this.gitIntegrationService.connectRepository(this.projectId, request).subscribe({
      next: (repo) => {
        this.repos.update(list => [...list, repo]);
        this.repoOwner = '';
        this.repoName = '';
        this.connectingRepo.set(false);
      },
      error: () => this.connectingRepo.set(false)
    });
  }

  disconnectRepo(repo: GitRepository): void {
    this.gitIntegrationService.disconnectRepository(repo.id).subscribe({
      next: () => this.repos.update(list => list.filter(r => r.id !== repo.id)),
      error: () => {}
    });
  }
}
