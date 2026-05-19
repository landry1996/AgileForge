import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TicketService } from '../../core/services/ticket.service';
import { TimeTrackingService } from '../../core/services/time-tracking.service';
import { TimeEntry, CreateTimeEntryRequest } from '../../core/models/time-tracking.model';
import { LabelService } from '../../core/services/label.service';
import { Label } from '../../core/models/label.model';
import { GitIntegrationService } from '../../core/services/git-integration.service';
import { GitBranch, GitPullRequest, GitPipeline } from '../../core/models/git.model';
import { ActivityService } from '../../core/services/activity.service';
import { Ticket } from '../../core/models/ticket.model';
import { Activity } from '../../core/models/activity.model';

type DetailTab = 'comments' | 'activity' | 'time' | 'links' | 'attachments' | 'devinfo';

interface Comment {
  id: string;
  userId: string;
  userName: string;
  content: string;
  createdAt: string;
}

interface TicketLink {
  id: string;
  type: 'BLOCKS' | 'IS_BLOCKED_BY' | 'RELATES_TO';
  linkedTicketId: string;
  linkedTicketKey: string;
  linkedTicketTitle: string;
}

interface Attachment {
  id: string;
  fileName: string;
  fileSize: number;
  contentType: string;
  uploadedBy: string;
  uploadedAt: string;
  url: string;
}

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="ticket-detail-page">
      @if (loading()) {
        <div class="loading-state">Loading ticket...</div>
      } @else if (error()) {
        <div class="error-state">
          <p>{{ error() }}</p>
          <button class="btn-secondary" (click)="loadTicket()">Retry</button>
        </div>
      } @else if (ticket()) {
        <div class="ticket-layout">
          <!-- Main Content -->
          <div class="ticket-main">
            <!-- Header -->
            <div class="ticket-header">
              <div class="ticket-key-row">
                <span class="ticket-key">{{ ticket()!.fullKey }}</span>
                <span class="type-icon">{{ getTypeIcon(ticket()!.type) }}</span>
                <span class="status-badge status-{{ ticket()!.status.toLowerCase() }}">{{ ticket()!.status }}</span>
                <span class="priority-badge priority-{{ ticket()!.priority.toLowerCase() }}">{{ ticket()!.priority }}</span>
                @if (ticket()!.qualityScore > 0) {
                  <span class="quality-badge" [class.quality-good]="ticket()!.qualityScore >= 70"
                        [class.quality-medium]="ticket()!.qualityScore >= 40 && ticket()!.qualityScore < 70"
                        [class.quality-poor]="ticket()!.qualityScore < 40">
                    Quality: {{ ticket()!.qualityScore }}%
                  </span>
                }
              </div>
              <h1 class="ticket-title">{{ ticket()!.title }}</h1>
              <div class="ticket-actions">
                <button class="btn-primary" (click)="generatePrompt()">Generate Prompt</button>
              </div>
            </div>

            <!-- Description -->
            <div class="description-section">
              <h3>Description</h3>
              @if (ticket()!.description) {
                <div class="description-content">{{ ticket()!.description }}</div>
              } @else {
                <p class="placeholder">No description provided.</p>
              }
            </div>

            <!-- Tabs -->
            <div class="detail-tabs">
              <button class="tab" [class.active]="activeTab() === 'comments'" (click)="activeTab.set('comments')">Comments</button>
              <button class="tab" [class.active]="activeTab() === 'activity'" (click)="activeTab.set('activity')">Activity</button>
              <button class="tab" [class.active]="activeTab() === 'time'" (click)="activeTab.set('time')">Time</button>
              <button class="tab" [class.active]="activeTab() === 'links'" (click)="activeTab.set('links')">Links</button>
              <button class="tab" [class.active]="activeTab() === 'attachments'" (click)="activeTab.set('attachments')">Attachments</button>
              <button class="tab" [class.active]="activeTab() === 'devinfo'" (click)="activeTab.set('devinfo')">Dev Info</button>
            </div>

            <!-- Comments Tab -->
            @if (activeTab() === 'comments') {
              <div class="tab-content">
                <!-- Add Comment Form -->
                <form class="comment-form" (submit)="addComment($event)">
                  <textarea [(ngModel)]="newComment" name="comment" rows="3"
                            placeholder="Add a comment..."></textarea>
                  <button type="submit" class="btn-primary" [disabled]="!newComment.trim() || addingComment()">
                    @if (addingComment()) { Posting... } @else { Comment }
                  </button>
                </form>

                @if (comments().length === 0) {
                  <p class="placeholder">No comments yet.</p>
                } @else {
                  <div class="comments-list">
                    @for (comment of comments(); track comment.id) {
                      <div class="comment-item">
                        <div class="comment-header">
                          <span class="comment-author">{{ comment.userName }}</span>
                          <span class="comment-date">{{ comment.createdAt }}</span>
                        </div>
                        <div class="comment-body">{{ comment.content }}</div>
                      </div>
                    }
                  </div>
                }
              </div>
            }

            <!-- Activity Tab -->
            @if (activeTab() === 'activity') {
              <div class="tab-content">
                @if (activities().length === 0) {
                  <p class="placeholder">No activity recorded.</p>
                } @else {
                  <div class="activity-timeline">
                    @for (activity of activities(); track activity.id) {
                      <div class="activity-item">
                        <div class="activity-dot"></div>
                        <div class="activity-content">
                          <span class="activity-user">{{ activity.userName }}</span>
                          <span class="activity-action">{{ activity.action }}</span>
                          <span class="activity-field">{{ activity.field }}</span>
                          @if (activity.oldValue && activity.newValue) {
                            <span class="activity-change">
                              <span class="old-value">{{ activity.oldValue }}</span>
                              ->
                              <span class="new-value">{{ activity.newValue }}</span>
                            </span>
                          }
                          <span class="activity-date">{{ activity.createdAt }}</span>
                        </div>
                      </div>
                    }
                  </div>
                }
              </div>
            }

            <!-- Time Tab -->
            @if (activeTab() === 'time') {
              <div class="tab-content">
                <!-- Log Time Form -->
                <form class="time-form" (submit)="logTime($event)">
                  <div class="time-form-row">
                    <input type="number" [(ngModel)]="logHours" name="hours" min="0.25" max="24" step="0.25"
                           placeholder="Hours" required>
                    <input type="text" [(ngModel)]="logDescription" name="description" placeholder="Description">
                    <input type="date" [(ngModel)]="logDate" name="date" required>
                    <button type="submit" class="btn-primary" [disabled]="loggingTime()">Log</button>
                  </div>
                </form>

                <div class="time-summary">
                  <span class="time-logged">Logged: {{ ticket()!.loggedHours || 0 }}h</span>
                  <span class="time-estimated">Estimated: {{ ticket()!.estimatedHours || '-' }}h</span>
                </div>

                @if (timeEntries().length === 0) {
                  <p class="placeholder">No time entries.</p>
                } @else {
                  <div class="time-entries">
                    @for (entry of timeEntries(); track entry.id) {
                      <div class="time-entry-row">
                        <span class="entry-date">{{ entry.workDate }}</span>
                        <span class="entry-desc">{{ entry.description || '-' }}</span>
                        <span class="entry-hours">{{ entry.hours }}h</span>
                      </div>
                    }
                  </div>
                }
              </div>
            }

            <!-- Links Tab -->
            @if (activeTab() === 'links') {
              <div class="tab-content">
                @if (ticketLinks().length === 0) {
                  <p class="placeholder">No linked tickets.</p>
                } @else {
                  <div class="links-list">
                    @for (link of ticketLinks(); track link.id) {
                      <div class="link-row">
                        <span class="link-type link-type-{{ link.type.toLowerCase() }}">{{ formatLinkType(link.type) }}</span>
                        <a [routerLink]="['/tickets', link.linkedTicketId]" class="link-ticket">
                          {{ link.linkedTicketKey }}
                        </a>
                        <span class="link-title">{{ link.linkedTicketTitle }}</span>
                      </div>
                    }
                  </div>
                }
              </div>
            }

            <!-- Attachments Tab -->
            @if (activeTab() === 'attachments') {
              <div class="tab-content">
                <div class="attachments-header">
                  <button class="btn-secondary" (click)="triggerUpload()">Upload File</button>
                </div>
                @if (attachments().length === 0) {
                  <p class="placeholder">No attachments.</p>
                } @else {
                  <div class="attachments-list">
                    @for (attachment of attachments(); track attachment.id) {
                      <div class="attachment-row">
                        <span class="attachment-name">{{ attachment.fileName }}</span>
                        <span class="attachment-size">{{ formatFileSize(attachment.fileSize) }}</span>
                        <span class="attachment-by">{{ attachment.uploadedBy }}</span>
                      </div>
                    }
                  </div>
                }
              </div>
            }

            <!-- Dev Info Tab -->
            @if (activeTab() === 'devinfo') {
              <div class="tab-content">
                <!-- Branches -->
                <div class="dev-section">
                  <h4>Branches</h4>
                  @if (branches().length === 0) {
                    <p class="placeholder">No branches linked.</p>
                  } @else {
                    @for (branch of branches(); track branch.id) {
                      <div class="branch-row">
                        <span class="branch-name">{{ branch.branchName }}</span>
                        <span class="branch-commit">{{ branch.createdAt }}</span>
                      </div>
                    }
                  }
                </div>

                <!-- Pull Requests -->
                <div class="dev-section">
                  <h4>Pull Requests</h4>
                  @if (pullRequests().length === 0) {
                    <p class="placeholder">No pull requests.</p>
                  } @else {
                    @for (pr of pullRequests(); track pr.id) {
                      <div class="pr-row">
                        <span class="pr-state pr-state-{{ pr.status.toLowerCase() }}">{{ pr.status }}</span>
                        <a [href]="pr.url" target="_blank" class="pr-title">{{ pr.title }}</a>
                        <span class="pr-author">{{ pr.author }}</span>
                      </div>
                    }
                  }
                </div>

                <!-- Pipeline Status -->
                <div class="dev-section">
                  <h4>Pipeline</h4>
                  @if (pipelines().length === 0) {
                    <p class="placeholder">No pipeline runs.</p>
                  } @else {
                    @for (pipeline of pipelines(); track pipeline.id) {
                      <div class="pipeline-row">
                        <span class="pipeline-status pipeline-{{ pipeline.status.toLowerCase() }}">{{ pipeline.status }}</span>
                        <a [href]="pipeline.url" target="_blank" class="pipeline-link">View</a>
                      </div>
                    }
                  }
                </div>
              </div>
            }
          </div>

          <!-- Sidebar -->
          <aside class="ticket-sidebar">
            <div class="sidebar-section">
              <h4>Details</h4>
              <div class="detail-row">
                <span class="detail-label">Assignee</span>
                <span class="detail-value">{{ ticket()!.assigneeId || 'Unassigned' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Reporter</span>
                <span class="detail-value">{{ ticket()!.reporterId }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Sprint</span>
                <span class="detail-value">{{ ticket()!.sprintId || 'None' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Epic</span>
                <span class="detail-value">{{ ticket()!.epicId || 'None' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Story Points</span>
                <span class="detail-value">{{ ticket()!.storyPoints || '-' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Due Date</span>
                <span class="detail-value">{{ ticket()!.dueDate || 'No due date' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Component</span>
                <span class="detail-value">{{ ticket()!.component || '-' }}</span>
              </div>
            </div>

            <!-- Labels -->
            @if (ticketLabels().length > 0) {
              <div class="sidebar-section">
                <h4>Labels</h4>
                <div class="labels-display">
                  @for (label of ticketLabels(); track label.id) {
                    <span class="label-chip" [style.border-color]="label.color">
                      <span class="label-dot" [style.background-color]="label.color"></span>
                      {{ label.name }}
                    </span>
                  }
                </div>
              </div>
            }

            <!-- Dates -->
            <div class="sidebar-section">
              <h4>Dates</h4>
              <div class="detail-row">
                <span class="detail-label">Created</span>
                <span class="detail-value">{{ ticket()!.createdAt }}</span>
              </div>
              @if (ticket()!.updatedAt) {
                <div class="detail-row">
                  <span class="detail-label">Updated</span>
                  <span class="detail-value">{{ ticket()!.updatedAt }}</span>
                </div>
              }
            </div>
          </aside>
        </div>
      }
    </div>
  `,
  styles: [`
    .ticket-detail-page { max-width: 1200px; }
    .loading-state, .error-state { color: #8b949e; padding: 60px; text-align: center; }
    .error-state p { color: #f85149; }

    .ticket-layout { display: grid; grid-template-columns: 1fr 280px; gap: 24px; }

    /* Header */
    .ticket-header { margin-bottom: 24px; }
    .ticket-key-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; flex-wrap: wrap; }
    .ticket-key { color: #58a6ff; font-size: 0.9rem; font-weight: 600; }
    .type-icon { font-size: 1rem; }
    .status-badge {
      padding: 3px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: 600;
    }
    .status-backlog { background: #8b949e22; color: #8b949e; }
    .status-todo { background: #8b949e22; color: #8b949e; }
    .status-in_progress { background: #1f6feb33; color: #58a6ff; }
    .status-code_review { background: #a371f733; color: #a371f7; }
    .status-qa { background: #d2992233; color: #d29922; }
    .status-done { background: #23863633; color: #3fb950; }
    .status-blocked { background: #f8514933; color: #f85149; }
    .status-cancelled { background: #8b949e22; color: #8b949e; }
    .status-in_review { background: #a371f733; color: #a371f7; }
    .status-deployed { background: #23863633; color: #3fb950; }

    .priority-badge { padding: 3px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
    .priority-critical { background: #f8514922; color: #f85149; }
    .priority-high { background: #d2992222; color: #d29922; }
    .priority-medium { background: #58a6ff22; color: #58a6ff; }
    .priority-low { background: #3fb95022; color: #3fb950; }
    .priority-trivial { background: #8b949e22; color: #8b949e; }

    .quality-badge { padding: 3px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
    .quality-good { background: #23863633; color: #3fb950; }
    .quality-medium { background: #d2992233; color: #d29922; }
    .quality-poor { background: #f8514933; color: #f85149; }

    .ticket-title { color: #e1e4e8; font-size: 1.5rem; margin: 0 0 12px 0; }
    .ticket-actions { display: flex; gap: 8px; }
    .btn-primary {
      background: #238636; border: none; color: white; padding: 8px 16px;
      border-radius: 6px; cursor: pointer; font-size: 0.85rem; font-weight: 500;
    }
    .btn-primary:hover { background: #2ea043; }
    .btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
    .btn-secondary {
      background: #21262d; border: 1px solid #30363d; color: #c9d1d9;
      padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 0.85rem;
    }
    .btn-secondary:hover { background: #30363d; }

    /* Description */
    .description-section {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px;
      padding: 20px; margin-bottom: 24px;
    }
    .description-section h3 { color: #e1e4e8; font-size: 1rem; margin: 0 0 12px 0; }
    .description-content { color: #c9d1d9; font-size: 0.9rem; line-height: 1.6; white-space: pre-wrap; }

    /* Tabs */
    .detail-tabs {
      display: flex; gap: 0; border-bottom: 1px solid #30363d; margin-bottom: 0;
    }
    .tab {
      background: none; border: none; color: #8b949e; padding: 10px 16px;
      font-size: 0.85rem; cursor: pointer; border-bottom: 2px solid transparent;
    }
    .tab:hover { color: #e1e4e8; }
    .tab.active { color: #58a6ff; border-bottom-color: #58a6ff; }

    .tab-content {
      background: #161b22; border: 1px solid #30363d; border-top: none;
      border-radius: 0 0 8px 8px; padding: 20px;
    }

    /* Comments */
    .comment-form { display: flex; flex-direction: column; gap: 8px; margin-bottom: 20px; }
    .comment-form textarea {
      background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      color: #c9d1d9; padding: 10px; font-size: 0.9rem; resize: vertical;
    }
    .comment-form textarea:focus { outline: none; border-color: #58a6ff; }
    .comment-form .btn-primary { align-self: flex-end; }
    .comments-list { display: flex; flex-direction: column; gap: 16px; }
    .comment-item { border-bottom: 1px solid #21262d; padding-bottom: 12px; }
    .comment-header { display: flex; gap: 12px; margin-bottom: 6px; }
    .comment-author { color: #e1e4e8; font-size: 0.85rem; font-weight: 600; }
    .comment-date { color: #484f58; font-size: 0.8rem; }
    .comment-body { color: #c9d1d9; font-size: 0.9rem; line-height: 1.5; }

    /* Activity */
    .activity-timeline { display: flex; flex-direction: column; gap: 12px; }
    .activity-item { display: flex; gap: 12px; align-items: flex-start; }
    .activity-dot {
      width: 8px; height: 8px; border-radius: 50%; background: #30363d;
      margin-top: 6px; flex-shrink: 0;
    }
    .activity-content { font-size: 0.85rem; color: #8b949e; line-height: 1.4; }
    .activity-user { color: #e1e4e8; font-weight: 500; }
    .activity-field { color: #58a6ff; }
    .activity-change { display: inline-flex; gap: 4px; }
    .old-value { text-decoration: line-through; color: #f85149; }
    .new-value { color: #3fb950; }
    .activity-date { display: block; color: #484f58; font-size: 0.75rem; margin-top: 2px; }

    /* Time */
    .time-form { margin-bottom: 16px; }
    .time-form-row { display: flex; gap: 8px; align-items: center; }
    .time-form-row input {
      background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      color: #c9d1d9; padding: 8px 12px; font-size: 0.85rem;
    }
    .time-form-row input:focus { outline: none; border-color: #58a6ff; }
    .time-form-row input[type="number"] { width: 80px; }
    .time-form-row input[type="text"] { flex: 1; }
    .time-summary {
      display: flex; gap: 16px; margin-bottom: 16px; padding: 10px;
      background: #0d1117; border-radius: 6px;
    }
    .time-logged { color: #58a6ff; font-size: 0.85rem; font-weight: 600; }
    .time-estimated { color: #8b949e; font-size: 0.85rem; }
    .time-entries { display: flex; flex-direction: column; gap: 6px; }
    .time-entry-row {
      display: flex; gap: 12px; align-items: center; padding: 6px 0;
      border-bottom: 1px solid #21262d; font-size: 0.85rem;
    }
    .entry-date { color: #8b949e; width: 90px; }
    .entry-desc { color: #c9d1d9; flex: 1; }
    .entry-hours { color: #58a6ff; font-weight: 600; }

    /* Links */
    .links-list { display: flex; flex-direction: column; gap: 8px; }
    .link-row { display: flex; gap: 12px; align-items: center; padding: 8px 0; border-bottom: 1px solid #21262d; }
    .link-type { font-size: 0.75rem; padding: 2px 8px; border-radius: 4px; font-weight: 500; }
    .link-type-blocks { background: #f8514922; color: #f85149; }
    .link-type-is_blocked_by { background: #d2992222; color: #d29922; }
    .link-type-relates_to { background: #58a6ff22; color: #58a6ff; }
    .link-ticket { color: #58a6ff; text-decoration: none; font-weight: 500; font-size: 0.85rem; }
    .link-ticket:hover { text-decoration: underline; }
    .link-title { color: #c9d1d9; font-size: 0.85rem; }

    /* Attachments */
    .attachments-header { margin-bottom: 16px; }
    .attachments-list { display: flex; flex-direction: column; gap: 8px; }
    .attachment-row {
      display: flex; gap: 12px; align-items: center; padding: 8px 12px;
      background: #0d1117; border-radius: 6px;
    }
    .attachment-name { color: #58a6ff; font-size: 0.85rem; flex: 1; }
    .attachment-size { color: #8b949e; font-size: 0.8rem; }
    .attachment-by { color: #484f58; font-size: 0.8rem; }

    /* Dev Info */
    .dev-section { margin-bottom: 20px; }
    .dev-section h4 { color: #e1e4e8; font-size: 0.9rem; margin: 0 0 10px 0; }
    .branch-row, .pr-row, .pipeline-row {
      display: flex; gap: 10px; align-items: center; padding: 6px 0;
      border-bottom: 1px solid #21262d; font-size: 0.85rem;
    }
    .branch-name { color: #a371f7; font-family: monospace; }
    .branch-commit { color: #8b949e; font-family: monospace; font-size: 0.8rem; }
    .pr-state { padding: 2px 8px; border-radius: 4px; font-size: 0.7rem; font-weight: 600; }
    .pr-state-open { background: #23863633; color: #3fb950; }
    .pr-state-merged { background: #a371f733; color: #a371f7; }
    .pr-state-closed { background: #f8514933; color: #f85149; }
    .pr-title { color: #58a6ff; text-decoration: none; }
    .pr-title:hover { text-decoration: underline; }
    .pr-author { color: #8b949e; }
    .pipeline-status { padding: 2px 8px; border-radius: 4px; font-size: 0.7rem; font-weight: 600; }
    .pipeline-running { background: #d2992233; color: #d29922; }
    .pipeline-success { background: #23863633; color: #3fb950; }
    .pipeline-failed { background: #f8514933; color: #f85149; }
    .pipeline-pending { background: #8b949e22; color: #8b949e; }
    .pipeline-branch { color: #a371f7; font-family: monospace; font-size: 0.8rem; }
    .pipeline-link { color: #58a6ff; text-decoration: none; font-size: 0.8rem; }

    /* Sidebar */
    .ticket-sidebar {
      display: flex; flex-direction: column; gap: 16px;
    }
    .sidebar-section {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px;
    }
    .sidebar-section h4 { color: #e1e4e8; font-size: 0.85rem; margin: 0 0 12px 0; }
    .detail-row { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid #21262d; }
    .detail-label { color: #8b949e; font-size: 0.8rem; }
    .detail-value { color: #c9d1d9; font-size: 0.8rem; text-align: right; }

    .labels-display { display: flex; flex-wrap: wrap; gap: 6px; }
    .label-chip {
      display: flex; align-items: center; gap: 4px; padding: 3px 8px;
      border-radius: 12px; border: 1px solid; font-size: 0.75rem; color: #c9d1d9;
    }
    .label-dot { width: 8px; height: 8px; border-radius: 50%; }

    .placeholder { color: #484f58; font-size: 0.9rem; }
  `]
})
export class TicketDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private ticketService = inject(TicketService);
  private timeTrackingService = inject(TimeTrackingService);
  private labelService = inject(LabelService);
  private gitIntegrationService = inject(GitIntegrationService);
  private activityService = inject(ActivityService);

  // State
  loading = signal(false);
  error = signal<string | null>(null);
  ticket = signal<Ticket | null>(null);
  activeTab = signal<DetailTab>('comments');

  // Tab data
  comments = signal<Comment[]>([]);
  activities = signal<Activity[]>([]);
  timeEntries = signal<TimeEntry[]>([]);
  ticketLinks = signal<TicketLink[]>([]);
  attachments = signal<Attachment[]>([]);
  branches = signal<GitBranch[]>([]);
  pullRequests = signal<GitPullRequest[]>([]);
  pipelines = signal<GitPipeline[]>([]);
  ticketLabels = signal<Label[]>([]);

  // Form state
  newComment = '';
  addingComment = signal(false);
  logHours = 0;
  logDescription = '';
  logDate = new Date().toISOString().split('T')[0];
  loggingTime = signal(false);

  ngOnInit(): void {
    const ticketId = this.route.snapshot.paramMap.get('id');
    if (ticketId) {
      this.loadTicket(ticketId);
    }
  }

  loadTicket(ticketId?: string): void {
    const id = ticketId || this.route.snapshot.paramMap.get('id');
    if (!id) return;

    this.loading.set(true);
    this.error.set(null);

    this.ticketService.getById(id).subscribe({
      next: (ticket) => {
        this.ticket.set(ticket);
        this.loading.set(false);
        this.loadTabData(id);
      },
      error: () => {
        this.error.set('Failed to load ticket. Please try again.');
        this.loading.set(false);
      }
    });
  }

  private loadTabData(ticketId: string): void {
    // Load time entries
    this.timeTrackingService.getByTicket(ticketId).subscribe({
      next: (entries) => this.timeEntries.set(entries),
      error: () => {}
    });

    // Load activity
    this.activityService.getProjectActivity(this.ticket()!.projectId).subscribe({
      next: (activities) => this.activities.set(activities.filter(a => a.ticketId === ticketId)),
      error: () => {}
    });

    // Load git info
    this.gitIntegrationService.getTicketDevInfo(ticketId).subscribe({
      next: (devInfo) => {
        this.branches.set(devInfo.branches);
        this.pullRequests.set(devInfo.pullRequests);
        this.pipelines.set(devInfo.pipelines);
      },
      error: () => {}
    });

    // Load labels
    this.labelService.getTicketLabels(ticketId).subscribe({
      next: (labels) => this.ticketLabels.set(labels),
      error: () => {}
    });
  }

  getTypeIcon(type: string): string {
    const icons: Record<string, string> = {
      'EPIC': '🏔',
      'STORY': '📖',
      'TASK': '✅',
      'BUG': '🐛',
      'SPIKE': '🔬',
      'SUBTASK': '📌',
      'IMPROVEMENT': '💡',
      'FEATURE': '⭐',
      'TECH_DEBT': '🔧',
      'SECURITY': '🔒',
      'PERFORMANCE': '⚡',
      'DOCUMENTATION': '📄',
      'DESIGN': '🎨',
      'RESEARCH': '🔍',
      'DEVOPS': '🚀'
    };
    return icons[type] || '📋';
  }

  addComment(event: Event): void {
    event.preventDefault();
    if (!this.newComment.trim()) return;
    this.addingComment.set(true);
    // In a real app, this would call a comment service
    const comment: Comment = {
      id: crypto.randomUUID(),
      userId: 'current-user',
      userName: 'You',
      content: this.newComment,
      createdAt: new Date().toISOString()
    };
    this.comments.update(list => [comment, ...list]);
    this.newComment = '';
    this.addingComment.set(false);
  }

  logTime(event: Event): void {
    event.preventDefault();
    if (!this.logHours || !this.logDate) return;

    this.loggingTime.set(true);
    const request: CreateTimeEntryRequest = {
      hours: this.logHours,
      description: this.logDescription || undefined,
      workDate: this.logDate
    };

    this.timeTrackingService.logTime(this.ticket()!.id, request).subscribe({
      next: (entry) => {
        this.timeEntries.update(list => [entry, ...list]);
        this.logHours = 0;
        this.logDescription = '';
        this.loggingTime.set(false);
      },
      error: () => this.loggingTime.set(false)
    });
  }

  formatLinkType(type: string): string {
    const labels: Record<string, string> = {
      'BLOCKS': 'Blocks',
      'IS_BLOCKED_BY': 'Is blocked by',
      'RELATES_TO': 'Relates to'
    };
    return labels[type] || type;
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  triggerUpload(): void {
    // In a real app, this would open a file picker
    console.log('Upload triggered');
  }

  generatePrompt(): void {
    // Navigate to AI assistant with ticket context
    console.log('Generate prompt for ticket', this.ticket()?.fullKey);
  }
}
