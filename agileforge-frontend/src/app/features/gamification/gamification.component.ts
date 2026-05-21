import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GamificationService } from '../../core/services/gamification.service';
import { GamificationProfile, Badge, LeaderboardEntry, Achievement } from '../../core/models/ecosystem.model';

@Component({
  selector: 'app-gamification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="gamification-container">
      <header class="page-header">
        <h1>Gamification</h1>
        <p class="subtitle">Badges, streaks & achievements</p>
      </header>

      <div class="tabs">
        <button [class.active]="activeTab() === 'profile'" (click)="activeTab.set('profile')">My Profile</button>
        <button [class.active]="activeTab() === 'badges'" (click)="activeTab.set('badges')">All Badges</button>
        <button [class.active]="activeTab() === 'leaderboard'" (click)="activeTab.set('leaderboard')">Leaderboard</button>
        <button [class.active]="activeTab() === 'achievements'" (click)="activeTab.set('achievements')">Team Achievements</button>
      </div>

      @if (activeTab() === 'profile') {
        <div class="profile-section">
          <div class="level-card">
            <div class="level-badge">Lv.{{ profile()?.level || 1 }}</div>
            <div class="xp-info">
              <span class="total-xp">{{ profile()?.totalXp || 0 }} XP</span>
              <div class="xp-bar">
                <div class="xp-fill" [style.width.%]="getXpProgress()"></div>
              </div>
            </div>
          </div>

          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-value">{{ profile()?.badges?.length || 0 }}</span>
              <span class="stat-label">Badges Earned</span>
            </div>
            <div class="stat-card">
              <span class="stat-value">{{ getMaxStreak() }}</span>
              <span class="stat-label">Best Streak</span>
            </div>
            <div class="stat-card">
              <span class="stat-value">{{ profile()?.xpByDomain?.length || 0 }}</span>
              <span class="stat-label">Domains</span>
            </div>
          </div>

          @if (profile()?.streaks?.length) {
            <h3>Active Streaks</h3>
            <div class="streaks-grid">
              @for (streak of profile()!.streaks; track streak.streakType) {
                <div class="streak-card">
                  <span class="streak-icon">🔥</span>
                  <span class="streak-count">{{ streak.currentCount }}</span>
                  <span class="streak-type">{{ streak.streakType }}</span>
                  <span class="streak-best">Best: {{ streak.longestCount }}</span>
                </div>
              }
            </div>
          }

          @if (profile()?.xpByDomain?.length) {
            <h3>XP by Domain</h3>
            <div class="xp-domains">
              @for (xp of profile()!.xpByDomain; track xp.domain) {
                <div class="xp-domain-row">
                  <span class="domain-name">{{ xp.domain }}</span>
                  <div class="domain-bar">
                    <div class="domain-fill" [style.width.%]="(xp.xpPoints / getMaxDomainXp()) * 100"></div>
                  </div>
                  <span class="domain-xp">{{ xp.xpPoints }} XP (Lv.{{ xp.level }})</span>
                </div>
              }
            </div>
          }
        </div>
      }

      @if (activeTab() === 'badges') {
        <div class="badges-grid">
          @for (badge of badges(); track badge.id) {
            <div class="badge-card" [class]="'rarity-' + badge.rarity.toLowerCase()">
              <div class="badge-icon">{{ getBadgeEmoji(badge.icon) }}</div>
              <h4>{{ badge.name }}</h4>
              <p>{{ badge.description }}</p>
              <div class="badge-meta">
                <span class="badge-points">{{ badge.points }} pts</span>
                <span class="badge-rarity">{{ badge.rarity }}</span>
              </div>
            </div>
          }
          @empty {
            <p class="empty-state">No badges configured yet.</p>
          }
        </div>
      }

      @if (activeTab() === 'leaderboard') {
        <div class="leaderboard-section">
          <div class="period-selector">
            <button [class.active]="period() === 'WEEKLY'" (click)="loadLeaderboard('WEEKLY')">Weekly</button>
            <button [class.active]="period() === 'MONTHLY'" (click)="loadLeaderboard('MONTHLY')">Monthly</button>
            <button [class.active]="period() === 'ALL_TIME'" (click)="loadLeaderboard('ALL_TIME')">All Time</button>
          </div>
          <table class="leaderboard-table">
            <thead>
              <tr><th>#</th><th>User</th><th>Points</th><th>Badges</th></tr>
            </thead>
            <tbody>
              @for (entry of leaderboard(); track entry.userId) {
                <tr>
                  <td class="rank">{{ entry.rank }}</td>
                  <td>{{ entry.userName }}</td>
                  <td>{{ entry.points }}</td>
                  <td>{{ entry.badgeCount }}</td>
                </tr>
              }
              @empty {
                <tr><td colspan="4" class="empty-state">No leaderboard data. Opt-in to participate!</td></tr>
              }
            </tbody>
          </table>
        </div>
      }

      @if (activeTab() === 'achievements') {
        <div class="achievements-section">
          @for (achievement of achievements(); track achievement.id) {
            <div class="achievement-card">
              <span class="achievement-icon">🏆</span>
              <div class="achievement-info">
                <h4>{{ achievement.name }}</h4>
                <p>{{ achievement.description }}</p>
                <span class="achievement-type">{{ achievement.type }}</span>
              </div>
            </div>
          }
          @empty {
            <p class="empty-state">No team achievements yet. Keep working together!</p>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .gamification-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
    .page-header h1 { margin: 0; font-size: 24px; }
    .subtitle { color: #6b7280; margin: 4px 0 24px; }
    .tabs { display: flex; gap: 8px; margin-bottom: 24px; border-bottom: 1px solid #e5e7eb; padding-bottom: 8px; }
    .tabs button { padding: 8px 16px; border: none; background: none; cursor: pointer; border-radius: 6px; font-weight: 500; }
    .tabs button.active { background: #4f46e5; color: white; }
    .level-card { display: flex; align-items: center; gap: 16px; padding: 24px; background: linear-gradient(135deg, #4f46e5, #7c3aed); border-radius: 12px; color: white; margin-bottom: 24px; }
    .level-badge { font-size: 32px; font-weight: bold; background: rgba(255,255,255,0.2); padding: 12px 20px; border-radius: 12px; }
    .xp-info { flex: 1; }
    .total-xp { font-size: 18px; font-weight: 600; }
    .xp-bar { height: 8px; background: rgba(255,255,255,0.3); border-radius: 4px; margin-top: 8px; }
    .xp-fill { height: 100%; background: #fbbf24; border-radius: 4px; transition: width 0.3s; }
    .stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
    .stat-card { padding: 16px; background: #f9fafb; border-radius: 8px; text-align: center; }
    .stat-value { display: block; font-size: 24px; font-weight: bold; color: #4f46e5; }
    .stat-label { font-size: 12px; color: #6b7280; }
    .streaks-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; margin-bottom: 24px; }
    .streak-card { padding: 16px; background: #fff7ed; border-radius: 8px; text-align: center; border: 1px solid #fed7aa; }
    .streak-icon { font-size: 24px; }
    .streak-count { display: block; font-size: 28px; font-weight: bold; color: #ea580c; }
    .streak-type { display: block; font-size: 12px; color: #6b7280; text-transform: capitalize; }
    .streak-best { display: block; font-size: 11px; color: #9ca3af; margin-top: 4px; }
    .xp-domains { display: flex; flex-direction: column; gap: 12px; }
    .xp-domain-row { display: flex; align-items: center; gap: 12px; }
    .domain-name { width: 120px; font-weight: 500; text-transform: capitalize; }
    .domain-bar { flex: 1; height: 8px; background: #e5e7eb; border-radius: 4px; }
    .domain-fill { height: 100%; background: #4f46e5; border-radius: 4px; }
    .domain-xp { font-size: 12px; color: #6b7280; width: 120px; text-align: right; }
    .badges-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
    .badge-card { padding: 20px; border-radius: 12px; border: 2px solid #e5e7eb; text-align: center; }
    .badge-card.rarity-common { border-color: #9ca3af; }
    .badge-card.rarity-uncommon { border-color: #22c55e; }
    .badge-card.rarity-rare { border-color: #3b82f6; }
    .badge-card.rarity-epic { border-color: #a855f7; }
    .badge-card.rarity-legendary { border-color: #f59e0b; background: #fffbeb; }
    .badge-icon { font-size: 36px; margin-bottom: 8px; }
    .badge-card h4 { margin: 0 0 4px; }
    .badge-card p { font-size: 12px; color: #6b7280; margin: 0 0 8px; }
    .badge-meta { display: flex; justify-content: space-between; font-size: 11px; }
    .badge-points { color: #4f46e5; font-weight: 600; }
    .badge-rarity { text-transform: uppercase; font-weight: 500; }
    .period-selector { display: flex; gap: 8px; margin-bottom: 16px; }
    .period-selector button { padding: 6px 12px; border: 1px solid #d1d5db; background: white; border-radius: 6px; cursor: pointer; }
    .period-selector button.active { background: #4f46e5; color: white; border-color: #4f46e5; }
    .leaderboard-table { width: 100%; border-collapse: collapse; }
    .leaderboard-table th, .leaderboard-table td { padding: 12px; text-align: left; border-bottom: 1px solid #e5e7eb; }
    .leaderboard-table th { font-weight: 600; color: #6b7280; font-size: 12px; text-transform: uppercase; }
    .rank { font-weight: bold; color: #4f46e5; }
    .achievement-card { display: flex; gap: 16px; padding: 16px; background: #f9fafb; border-radius: 8px; margin-bottom: 12px; }
    .achievement-icon { font-size: 32px; }
    .achievement-info h4 { margin: 0 0 4px; }
    .achievement-info p { margin: 0; font-size: 13px; color: #6b7280; }
    .achievement-type { font-size: 11px; color: #4f46e5; font-weight: 500; text-transform: uppercase; }
    .empty-state { text-align: center; color: #9ca3af; padding: 32px; }
  `]
})
export class GamificationComponent implements OnInit {
  private gamificationService = inject(GamificationService);

  activeTab = signal<'profile' | 'badges' | 'leaderboard' | 'achievements'>('profile');
  profile = signal<GamificationProfile | null>(null);
  badges = signal<Badge[]>([]);
  leaderboard = signal<LeaderboardEntry[]>([]);
  achievements = signal<Achievement[]>([]);
  period = signal<string>('WEEKLY');

  ngOnInit() {
    this.gamificationService.getProfile('current').subscribe(p => this.profile.set(p));
    this.gamificationService.getAvailableBadges().subscribe(b => this.badges.set(b));
  }

  loadLeaderboard(period: string) {
    this.period.set(period);
    this.gamificationService.getLeaderboard('current-org', period).subscribe(l => this.leaderboard.set(l));
  }

  getXpProgress(): number {
    const xp = this.profile()?.totalXp || 0;
    const thresholds = [0, 100, 300, 600, 1000, 1500, 2500, 4000, 6000, 9000, 15000];
    const level = this.profile()?.level || 1;
    const current = thresholds[level - 1] || 0;
    const next = thresholds[level] || thresholds[thresholds.length - 1];
    return ((xp - current) / (next - current)) * 100;
  }

  getMaxStreak(): number {
    return this.profile()?.streaks?.reduce((max, s) => Math.max(max, s.longestCount), 0) || 0;
  }

  getMaxDomainXp(): number {
    return this.profile()?.xpByDomain?.reduce((max, x) => Math.max(max, x.xpPoints), 1) || 1;
  }

  getBadgeEmoji(icon: string): string {
    const emojiMap: Record<string, string> = {
      'git-commit': '📝', 'bug': '🐛', 'shield-bug': '🛡️', 'trophy': '🏆',
      'eye': '👁️', 'book': '📚', 'check-circle': '✅', 'zap': '⚡',
      'users': '👥', 'flame': '🔥'
    };
    return emojiMap[icon] || '🎖️';
  }
}
