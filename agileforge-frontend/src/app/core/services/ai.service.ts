import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GeneratedTicket, GenerateBacklogRequest, GenerateTicketsRequest, QualityAnalysis } from '../models/ai.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AiService {
  private readonly apiUrl = `${environment.apiUrl}/ai`;

  constructor(private http: HttpClient) {}

  generateTickets(request: GenerateTicketsRequest): Observable<GeneratedTicket[]> {
    return this.http.post<GeneratedTicket[]>(`${this.apiUrl}/generate-tickets`, request);
  }

  generateBacklog(request: GenerateBacklogRequest): Observable<GeneratedTicket[]> {
    return this.http.post<GeneratedTicket[]>(`${this.apiUrl}/generate-backlog`, request);
  }

  analyzeQuality(title: string, description?: string, type?: string): Observable<QualityAnalysis> {
    return this.http.post<QualityAnalysis>(`${this.apiUrl}/analyze-quality`, { title, description, type });
  }

  decomposeTicket(title: string, description?: string, type?: string): Observable<GeneratedTicket[]> {
    return this.http.post<GeneratedTicket[]>(`${this.apiUrl}/decompose`, { title, description, type });
  }

  suggestDescription(title: string, type?: string, projectContext?: string): Observable<{ suggestedDescription: string }> {
    return this.http.post<{ suggestedDescription: string }>(`${this.apiUrl}/suggest-description`, { title, type, projectContext });
  }
}
