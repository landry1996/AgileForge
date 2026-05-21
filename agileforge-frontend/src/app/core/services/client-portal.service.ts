import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClientPortalConfig, ClientUser, ClientFeedback, ClientPortalView } from '../models/enterprise.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ClientPortalService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  configure(projectId: string, data: {
    isEnabled?: boolean; welcomeMessage?: string; allowedTicketTypes?: string[];
    showRoadmap?: boolean; showReleases?: boolean; showChangelog?: boolean;
  }): Observable<ClientPortalConfig> {
    return this.http.put<ClientPortalConfig>(`${this.apiUrl}/projects/${projectId}/portal`, data);
  }

  getConfig(projectId: string): Observable<ClientPortalConfig> {
    return this.http.get<ClientPortalConfig>(`${this.apiUrl}/projects/${projectId}/portal`);
  }

  addUser(projectId: string, data: { email: string; name: string; company?: string }): Observable<ClientUser> {
    return this.http.post<ClientUser>(`${this.apiUrl}/projects/${projectId}/portal/users`, data);
  }

  getUsers(projectId: string): Observable<ClientUser[]> {
    return this.http.get<ClientUser[]>(`${this.apiUrl}/projects/${projectId}/portal/users`);
  }

  removeUser(clientUserId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/portal/users/${clientUserId}`);
  }

  submitFeedback(portalId: string, clientUserId: string, data: { ticketId?: string; type?: string; content: string; rating?: number }): Observable<ClientFeedback> {
    return this.http.post<ClientFeedback>(`${this.apiUrl}/portal/${portalId}/feedback?clientUserId=${clientUserId}`, data);
  }

  getFeedback(projectId: string): Observable<ClientFeedback[]> {
    return this.http.get<ClientFeedback[]>(`${this.apiUrl}/projects/${projectId}/portal/feedback`);
  }

  getPortalView(projectId: string): Observable<ClientPortalView> {
    return this.http.get<ClientPortalView>(`${this.apiUrl}/projects/${projectId}/portal/view`);
  }
}
