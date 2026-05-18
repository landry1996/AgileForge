import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateTicketRequest, Ticket } from '../models/ticket.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly apiUrl = `${environment.apiUrl}/tickets`;

  constructor(private http: HttpClient) {}

  create(projectId: string, request: CreateTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/project/${projectId}`, request);
  }

  getById(id: string): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.apiUrl}/${id}`);
  }

  getByProject(projectId: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/project/${projectId}`);
  }

  getByStatus(projectId: string, status: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/project/${projectId}/status/${status}`);
  }

  getMyTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/my`);
  }

  getBySprint(sprintId: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/sprint/${sprintId}`);
  }

  update(id: string, data: Partial<Ticket>): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.apiUrl}/${id}`, data);
  }

  transition(id: string, status: string): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.apiUrl}/${id}/transition/${status}`, {});
  }

  logTime(id: string, hours: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/log-time?hours=${hours}`, {});
  }
}
