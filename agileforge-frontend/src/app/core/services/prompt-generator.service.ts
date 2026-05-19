import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GeneratedPrompt, GeneratePromptRequest, PromptTemplate } from '../models/prompt.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PromptGeneratorService {
  private readonly apiUrl = `${environment.apiUrl}/prompts`;

  constructor(private http: HttpClient) {}

  generateForTicket(ticketId: string, request: GeneratePromptRequest): Observable<GeneratedPrompt> {
    return this.http.post<GeneratedPrompt>(`${this.apiUrl}/tickets/${ticketId}/generate`, request);
  }

  getHistory(ticketId: string): Observable<GeneratedPrompt[]> {
    return this.http.get<GeneratedPrompt[]>(`${this.apiUrl}/tickets/${ticketId}/history`);
  }

  ratePrompt(promptId: string, rating: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${promptId}/rate`, { rating });
  }

  getTemplates(projectId: string): Observable<PromptTemplate[]> {
    return this.http.get<PromptTemplate[]>(`${this.apiUrl}/templates/project/${projectId}`);
  }

  getGlobalTemplates(): Observable<PromptTemplate[]> {
    return this.http.get<PromptTemplate[]>(`${this.apiUrl}/templates/global`);
  }

  createTemplate(projectId: string, template: Partial<PromptTemplate>): Observable<PromptTemplate> {
    return this.http.post<PromptTemplate>(`${this.apiUrl}/templates/project/${projectId}`, template);
  }
}
