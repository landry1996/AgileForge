import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GamificationProfile, Badge, LeaderboardEntry, Achievement } from '../models/ecosystem.model';

@Injectable({ providedIn: 'root' })
export class GamificationService {
  private http = inject(HttpClient);
  private baseUrl = '/api/gamification';

  getProfile(userId: string): Observable<GamificationProfile> {
    return this.http.get<GamificationProfile>(`${this.baseUrl}/profile/${userId}`);
  }

  getAvailableBadges(): Observable<Badge[]> {
    return this.http.get<Badge[]>(`${this.baseUrl}/badges`);
  }

  getLeaderboard(organizationId: string, period: string = 'WEEKLY'): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${this.baseUrl}/leaderboard/${organizationId}`, { params: { period } });
  }

  optInLeaderboard(userId: string, organizationId: string, optIn: boolean): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/leaderboard/opt-in`, { userId, organizationId, optIn });
  }

  getTeamAchievements(projectId: string): Observable<Achievement[]> {
    return this.http.get<Achievement[]>(`${this.baseUrl}/achievements/${projectId}`);
  }

  recordActivity(userId: string, activityType: string, metadata?: Record<string, unknown>): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/activity`, { userId, activityType, metadata });
  }
}
