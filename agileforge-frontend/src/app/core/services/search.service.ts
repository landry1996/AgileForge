import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket } from '../models/ticket.model';
import { SearchResponse, SearchFilters } from '../models/notification.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly apiUrl = `${environment.apiUrl}/search`;

  constructor(private http: HttpClient) {}

  searchTickets(filters: SearchFilters): Observable<SearchResponse<Ticket>> {
    let params = new HttpParams();

    if (filters.q) params = params.set('q', filters.q);
    if (filters.projectId) params = params.set('projectId', filters.projectId);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.priority) params = params.set('priority', filters.priority);
    if (filters.assigneeId) params = params.set('assigneeId', filters.assigneeId);
    if (filters.page !== undefined) params = params.set('page', filters.page.toString());
    if (filters.size !== undefined) params = params.set('size', filters.size.toString());

    return this.http.get<SearchResponse<Ticket>>(`${this.apiUrl}/tickets`, { params });
  }
}
