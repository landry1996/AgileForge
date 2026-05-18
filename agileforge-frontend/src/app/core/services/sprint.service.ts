import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateSprintRequest, Sprint, SprintMetrics } from '../models/sprint.model';
import { Ticket } from '../models/ticket.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SprintService {
  private readonly apiUrl = `${environment.apiUrl}/sprints`;

  constructor(private http: HttpClient) {}

  create(projectId: string, request: CreateSprintRequest): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.apiUrl}/project/${projectId}`, request);
  }

  getById(id: string): Observable<Sprint> {
    return this.http.get<Sprint>(`${this.apiUrl}/${id}`);
  }

  getByProject(projectId: string): Observable<Sprint[]> {
    return this.http.get<Sprint[]>(`${this.apiUrl}/project/${projectId}`);
  }

  getActive(projectId: string): Observable<Sprint> {
    return this.http.get<Sprint>(`${this.apiUrl}/project/${projectId}/active`);
  }

  start(id: string): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.apiUrl}/${id}/start`, {});
  }

  complete(id: string): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.apiUrl}/${id}/complete`, {});
  }

  addTicket(sprintId: string, ticketId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${sprintId}/tickets/${ticketId}`, {});
  }

  removeTicket(sprintId: string, ticketId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${sprintId}/tickets/${ticketId}`);
  }

  getTickets(sprintId: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/${sprintId}/tickets`);
  }

  getMetrics(sprintId: string): Observable<SprintMetrics> {
    return this.http.get<SprintMetrics>(`${this.apiUrl}/${sprintId}/metrics`);
  }
}
