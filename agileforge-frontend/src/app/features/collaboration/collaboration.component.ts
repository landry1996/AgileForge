import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CollaborationService } from '../../core/services/collaboration.service';
import { PresenceInfo } from '../../core/models/ecosystem.model';

@Component({
  selector: 'app-collaboration',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="collaboration-container">
      <header class="page-header">
        <h1>Real-Time Collaboration</h1>
        <p class="subtitle">See who's online and collaborate in real-time</p>
      </header>

      <div class="collab-grid">
        <div class="presence-panel">
          <h3>Online Team Members</h3>
          <div class="presence-list">
            @for (user of onlineUsers(); track user.userId) {
              <div class="presence-item">
                <div class="avatar" [class]="'status-' + user.status.toLowerCase()">
                  {{ getInitials(user.userId) }}
                </div>
                <div class="presence-info">
                  <span class="presence-name">{{ user.userId }}</span>
                  <span class="presence-page">{{ user.currentPage || 'Dashboard' }}</span>
                </div>
                <span class="presence-dot" [class]="user.status.toLowerCase()"></span>
              </div>
            }
            @empty {
              <p class="empty-state">No team members online</p>
            }
          </div>

          <div class="presence-legend">
            <span><span class="dot online"></span> Online</span>
            <span><span class="dot away"></span> Away</span>
            <span><span class="dot busy"></span> Busy</span>
          </div>
        </div>

        <div class="features-panel">
          <h3>Collaboration Features</h3>

          <div class="feature-card">
            <span class="feature-icon">✏️</span>
            <div>
              <h4>Live Editing</h4>
              <p>Edit tickets and documents simultaneously with your team. Changes sync in real-time using CRDT technology.</p>
            </div>
          </div>

          <div class="feature-card">
            <span class="feature-icon">👆</span>
            <div>
              <h4>Cursor Presence</h4>
              <p>See where your teammates are working. Colored cursors show real-time positions in shared documents.</p>
            </div>
          </div>

          <div class="feature-card">
            <span class="feature-icon">🔔</span>
            <div>
              <h4>Instant Notifications</h4>
              <p>Get notified instantly when someone mentions you, moves a ticket, or comments on your work.</p>
            </div>
          </div>

          <div class="feature-card">
            <span class="feature-icon">📋</span>
            <div>
              <h4>Live Board Updates</h4>
              <p>Kanban board updates in real-time. See tickets move across columns as your team progresses.</p>
            </div>
          </div>

          <div class="feature-card">
            <span class="feature-icon">💬</span>
            <div>
              <h4>Typing Indicators</h4>
              <p>Know when someone is typing a comment on a ticket you're viewing.</p>
            </div>
          </div>
        </div>
      </div>

      <div class="stats-section">
        <h3>Collaboration Stats</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <span class="stat-value">{{ onlineUsers().length }}</span>
            <span class="stat-label">Online Now</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">{{ activeSessions() }}</span>
            <span class="stat-label">Active Sessions</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">WebSocket</span>
            <span class="stat-label">Connection</span>
          </div>
          <div class="stat-card">
            <span class="stat-value">CRDT</span>
            <span class="stat-label">Sync Protocol</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .collaboration-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
    .page-header h1 { margin: 0; font-size: 24px; }
    .subtitle { color: #6b7280; margin: 4px 0 24px; }
    .collab-grid { display: grid; grid-template-columns: 300px 1fr; gap: 24px; margin-bottom: 32px; }
    .presence-panel { padding: 20px; background: #f9fafb; border-radius: 12px; }
    .presence-panel h3 { margin: 0 0 16px; font-size: 16px; }
    .presence-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
    .presence-item { display: flex; align-items: center; gap: 12px; padding: 8px; border-radius: 8px; }
    .presence-item:hover { background: #e5e7eb; }
    .avatar { width: 36px; height: 36px; border-radius: 50%; background: #4f46e5; color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; }
    .avatar.status-away { background: #f59e0b; }
    .avatar.status-busy { background: #dc2626; }
    .presence-info { flex: 1; }
    .presence-name { display: block; font-weight: 500; font-size: 14px; }
    .presence-page { display: block; font-size: 11px; color: #9ca3af; }
    .presence-dot { width: 8px; height: 8px; border-radius: 50%; }
    .presence-dot.online { background: #22c55e; }
    .presence-dot.away { background: #f59e0b; }
    .presence-dot.busy { background: #dc2626; }
    .presence-dot.offline { background: #9ca3af; }
    .presence-legend { display: flex; gap: 16px; font-size: 12px; color: #6b7280; }
    .presence-legend .dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 4px; }
    .presence-legend .dot.online { background: #22c55e; }
    .presence-legend .dot.away { background: #f59e0b; }
    .presence-legend .dot.busy { background: #dc2626; }
    .features-panel h3 { margin: 0 0 16px; font-size: 16px; }
    .feature-card { display: flex; gap: 16px; padding: 16px; border: 1px solid #e5e7eb; border-radius: 8px; margin-bottom: 12px; }
    .feature-icon { font-size: 24px; }
    .feature-card h4 { margin: 0 0 4px; font-size: 14px; }
    .feature-card p { margin: 0; font-size: 13px; color: #6b7280; }
    .stats-section h3 { margin: 0 0 16px; }
    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
    .stat-card { padding: 20px; background: #f9fafb; border-radius: 8px; text-align: center; }
    .stat-value { display: block; font-size: 20px; font-weight: bold; color: #4f46e5; }
    .stat-label { font-size: 12px; color: #6b7280; }
    .empty-state { text-align: center; color: #9ca3af; padding: 16px; font-size: 13px; }
  `]
})
export class CollaborationComponent implements OnInit, OnDestroy {
  private collaborationService = inject(CollaborationService);
  private heartbeatInterval: ReturnType<typeof setInterval> | null = null;

  onlineUsers = signal<PresenceInfo[]>([]);
  activeSessions = signal(0);

  ngOnInit() {
    this.collaborationService.getOnlineUsers('current-org').subscribe(u => this.onlineUsers.set(u));
    this.heartbeatInterval = setInterval(() => {
      this.collaborationService.heartbeat('current-user').subscribe();
    }, 30000);
  }

  ngOnDestroy() {
    if (this.heartbeatInterval) clearInterval(this.heartbeatInterval);
  }

  getInitials(userId: string): string {
    return userId.substring(0, 2).toUpperCase();
  }
}
