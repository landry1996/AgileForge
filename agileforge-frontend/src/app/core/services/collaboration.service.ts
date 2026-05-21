import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { CollaborationSession, PresenceInfo } from '../models/ecosystem.model';

@Injectable({ providedIn: 'root' })
export class CollaborationService {
  private http = inject(HttpClient);
  private baseUrl = '/api/collaboration';

  private cursorUpdates$ = new Subject<{ userId: string; position: string }>();
  private operationUpdates$ = new Subject<{ userId: string; type: string; data: string }>();

  joinSession(resourceType: string, resourceId: string, userId: string): Observable<CollaborationSession> {
    return this.http.post<CollaborationSession>(`${this.baseUrl}/sessions/join`, { resourceType, resourceId, userId });
  }

  leaveSession(sessionId: string, userId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/sessions/${sessionId}/leave`, { userId });
  }

  getParticipants(sessionId: string): Observable<CollaborationSession['participants']> {
    return this.http.get<CollaborationSession['participants']>(`${this.baseUrl}/sessions/${sessionId}/participants`);
  }

  getActiveSessions(resourceType: string, resourceId: string): Observable<CollaborationSession[]> {
    return this.http.get<CollaborationSession[]>(`${this.baseUrl}/sessions`, { params: { resourceType, resourceId } });
  }

  heartbeat(userId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/presence/heartbeat`, { userId });
  }

  updatePresence(userId: string, status: string, currentPage: string, currentResourceId?: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/presence/update`, { userId, status, currentPage, currentResourceId });
  }

  getOnlineUsers(organizationId: string): Observable<PresenceInfo[]> {
    return this.http.get<PresenceInfo[]>(`${this.baseUrl}/presence/online/${organizationId}`);
  }

  getCursorUpdates(): Observable<{ userId: string; position: string }> {
    return this.cursorUpdates$.asObservable();
  }

  getOperationUpdates(): Observable<{ userId: string; type: string; data: string }> {
    return this.operationUpdates$.asObservable();
  }
}
