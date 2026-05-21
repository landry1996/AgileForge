import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Incident, IncidentTimeline } from '../models/enterprise.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class IncidentService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  create(projectId: string, data: { title: string; description?: string; severity?: string }): Observable<Incident> {
    return this.http.post<Incident>(`${this.apiUrl}/projects/${projectId}/incidents`, data);
  }

  getByProject(projectId: string): Observable<Incident[]> {
    return this.http.get<Incident[]>(`${this.apiUrl}/projects/${projectId}/incidents`);
  }

  getActive(projectId: string): Observable<Incident[]> {
    return this.http.get<Incident[]>(`${this.apiUrl}/projects/${projectId}/incidents/active`);
  }

  getById(id: string): Observable<Incident> {
    return this.http.get<Incident>(`${this.apiUrl}/incidents/${id}`);
  }

  update(id: string, data: Partial<{ title: string; description: string; severity: string; status: string; commanderId: string; rootCause: string; resolution: string; postMortem: string }>): Observable<Incident> {
    return this.http.put<Incident>(`${this.apiUrl}/incidents/${id}`, data);
  }

  resolve(id: string, resolution: string): Observable<Incident> {
    return this.http.post<Incident>(`${this.apiUrl}/incidents/${id}/resolve`, { resolution });
  }

  addEvent(id: string, data: { eventType: string; message: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/incidents/${id}/events`, data);
  }

  addParticipant(id: string, data: { userId: string; role?: string }): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/incidents/${id}/participants`, data);
  }

  getTimeline(id: string): Observable<IncidentTimeline> {
    return this.http.get<IncidentTimeline>(`${this.apiUrl}/incidents/${id}/timeline`);
  }
}
