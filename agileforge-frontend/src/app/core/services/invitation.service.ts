import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Invitation, InviteRequest } from '../models/invitation.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InvitationService {
  private readonly apiUrl = `${environment.apiUrl}/invitations`;

  constructor(private http: HttpClient) {}

  invite(organizationId: string, request: InviteRequest): Observable<Invitation> {
    return this.http.post<Invitation>(`${this.apiUrl}/organization/${organizationId}`, request);
  }

  getPending(organizationId: string): Observable<Invitation[]> {
    return this.http.get<Invitation[]>(`${this.apiUrl}/organization/${organizationId}/pending`);
  }

  accept(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/accept`, {});
  }

  cancel(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/cancel`, {});
  }

  resend(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/resend`, {});
  }
}
