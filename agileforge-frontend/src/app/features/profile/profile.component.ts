import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProfileService } from '../../core/services/profile.service';
import { ActivityService } from '../../core/services/activity.service';
import { UserProfile, UpdateProfileRequest, Activity } from '../../core/models/activity.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="profile-page">
      <h2 class="page-title">My Profile</h2>

      @if (profile()) {
        <div class="profile-grid">
          <div class="profile-card">
            <div class="avatar-section">
              <div class="avatar">
                @if (profile()!.avatarUrl) {
                  <img [src]="profile()!.avatarUrl" alt="Avatar" />
                } @else {
                  <span class="avatar-initials">{{ getInitials() }}</span>
                }
              </div>
              <div class="user-meta">
                <h3>{{ profile()!.displayName || (profile()!.firstName + ' ' + profile()!.lastName) }}</h3>
                <p class="email">{{ profile()!.email }}</p>
                @if (profile()!.emailVerified) {
                  <span class="verified-badge">Verified</span>
                }
              </div>
            </div>

            <form class="profile-form" (ngSubmit)="saveProfile()">
              <div class="form-row">
                <div class="form-group">
                  <label>First Name</label>
                  <input type="text" [(ngModel)]="form.firstName" name="firstName" />
                </div>
                <div class="form-group">
                  <label>Last Name</label>
                  <input type="text" [(ngModel)]="form.lastName" name="lastName" />
                </div>
              </div>
              <div class="form-group">
                <label>Display Name</label>
                <input type="text" [(ngModel)]="form.displayName" name="displayName" />
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>Phone</label>
                  <input type="text" [(ngModel)]="form.phone" name="phone" />
                </div>
                <div class="form-group">
                  <label>Timezone</label>
                  <select [(ngModel)]="form.timezone" name="timezone">
                    <option value="UTC">UTC</option>
                    <option value="Europe/Paris">Europe/Paris</option>
                    <option value="America/New_York">America/New_York</option>
                    <option value="America/Los_Angeles">America/Los_Angeles</option>
                    <option value="Asia/Tokyo">Asia/Tokyo</option>
                  </select>
                </div>
              </div>
              <div class="form-actions">
                <button type="submit" class="save-btn" [disabled]="saving()">
                  {{ saving() ? 'Saving...' : 'Save Changes' }}
                </button>
                @if (saved()) {
                  <span class="saved-msg">Profile updated!</span>
                }
              </div>
            </form>
          </div>

          <div class="activity-card">
            <h3>Recent Activity</h3>
            <div class="activity-list">
              @for (activity of activities(); track activity.id) {
                <div class="activity-item">
                  <div class="activity-dot"></div>
                  <div class="activity-content">
                    <span class="activity-ticket">{{ activity.ticketKey }}</span>
                    <span class="activity-text">
                      {{ activity.userName }} updated {{ activity.field }}
                      @if (activity.newValue) {
                        to <strong>{{ activity.newValue }}</strong>
                      }
                    </span>
                    <span class="activity-time">{{ getRelativeTime(activity.createdAt) }}</span>
                  </div>
                </div>
              } @empty {
                <p class="no-activity">No recent activity</p>
              }
            </div>
          </div>
        </div>
      } @else {
        <div class="loading">Loading profile...</div>
      }
    </div>
  `,
  styles: [`
    .profile-page { max-width: 1000px; }
    .page-title { color: #e1e4e8; margin-bottom: 24px; }
    .profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
    .profile-card, .activity-card {
      background: #161b22;
      border: 1px solid #21262d;
      border-radius: 8px;
      padding: 24px;
    }
    .avatar-section { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .avatar {
      width: 64px; height: 64px; border-radius: 50%;
      background: #21262d; display: flex; align-items: center; justify-content: center;
      overflow: hidden;
    }
    .avatar img { width: 100%; height: 100%; object-fit: cover; }
    .avatar-initials { font-size: 1.4rem; font-weight: 600; color: #58a6ff; }
    .user-meta h3 { margin: 0; color: #e1e4e8; }
    .email { color: #8b949e; font-size: 0.85rem; margin: 4px 0; }
    .verified-badge {
      font-size: 0.7rem; background: #23863620; color: #3fb950;
      padding: 2px 8px; border-radius: 10px;
    }
    .profile-form { display: flex; flex-direction: column; gap: 16px; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
    .form-group { display: flex; flex-direction: column; gap: 4px; }
    .form-group label { font-size: 0.8rem; color: #8b949e; }
    .form-group input, .form-group select {
      padding: 8px 12px; background: #0d1117; border: 1px solid #30363d;
      border-radius: 6px; color: #e1e4e8; font-size: 0.9rem;
    }
    .form-group input:focus, .form-group select:focus { border-color: #58a6ff; outline: none; }
    .form-actions { display: flex; align-items: center; gap: 12px; margin-top: 8px; }
    .save-btn {
      padding: 8px 20px; background: #238636; border: none; border-radius: 6px;
      color: white; font-weight: 500; cursor: pointer;
    }
    .save-btn:hover { background: #2ea043; }
    .save-btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .saved-msg { color: #3fb950; font-size: 0.85rem; }
    .activity-card h3 { color: #e1e4e8; margin: 0 0 16px; }
    .activity-list { display: flex; flex-direction: column; gap: 0; }
    .activity-item {
      display: flex; gap: 12px; padding: 10px 0;
      border-left: 2px solid #21262d; padding-left: 16px;
      position: relative;
    }
    .activity-dot {
      position: absolute; left: -5px; top: 14px;
      width: 8px; height: 8px; border-radius: 50%; background: #58a6ff;
    }
    .activity-content { display: flex; flex-direction: column; gap: 2px; }
    .activity-ticket { color: #58a6ff; font-size: 0.8rem; font-weight: 600; }
    .activity-text { color: #8b949e; font-size: 0.8rem; }
    .activity-text strong { color: #e1e4e8; }
    .activity-time { color: #484f58; font-size: 0.7rem; }
    .no-activity { color: #484f58; font-size: 0.85rem; text-align: center; padding: 24px; }
    .loading { color: #8b949e; text-align: center; padding: 48px; }
    @media (max-width: 768px) { .profile-grid { grid-template-columns: 1fr; } }
  `]
})
export class ProfileComponent implements OnInit {
  private profileService = inject(ProfileService);
  private activityService = inject(ActivityService);

  profile = signal<UserProfile | null>(null);
  activities = signal<Activity[]>([]);
  saving = signal(false);
  saved = signal(false);
  form: UpdateProfileRequest = {};

  ngOnInit(): void {
    this.profileService.getProfile().subscribe({
      next: (p) => {
        this.profile.set(p);
        this.form = {
          firstName: p.firstName,
          lastName: p.lastName,
          displayName: p.displayName || '',
          phone: p.phone || '',
          timezone: p.timezone || 'UTC',
          locale: p.locale || 'fr'
        };
      }
    });

    this.activityService.getMyActivity(15).subscribe({
      next: (list) => this.activities.set(list),
      error: () => {}
    });
  }

  saveProfile(): void {
    this.saving.set(true);
    this.saved.set(false);
    this.profileService.updateProfile(this.form).subscribe({
      next: (updated) => {
        this.profile.set(updated);
        this.saving.set(false);
        this.saved.set(true);
        setTimeout(() => this.saved.set(false), 3000);
      },
      error: () => this.saving.set(false)
    });
  }

  getInitials(): string {
    const p = this.profile();
    if (!p) return '';
    return (p.firstName?.charAt(0) || '') + (p.lastName?.charAt(0) || '');
  }

  getRelativeTime(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    return `${days}d ago`;
  }
}
