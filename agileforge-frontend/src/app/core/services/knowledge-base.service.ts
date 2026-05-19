import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateKnowledgeEntryRequest, KnowledgeCategory, KnowledgeEntry, ProjectContext } from '../models/knowledge.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class KnowledgeBaseService {
  private readonly apiUrl = `${environment.apiUrl}/knowledge`;

  constructor(private http: HttpClient) {}

  getByProject(projectId: string): Observable<KnowledgeEntry[]> {
    return this.http.get<KnowledgeEntry[]>(`${this.apiUrl}/project/${projectId}`);
  }

  getByCategory(projectId: string, category: KnowledgeCategory): Observable<KnowledgeEntry[]> {
    return this.http.get<KnowledgeEntry[]>(`${this.apiUrl}/project/${projectId}/category/${category}`);
  }

  getProjectContext(projectId: string): Observable<ProjectContext> {
    return this.http.get<ProjectContext>(`${this.apiUrl}/project/${projectId}/context`);
  }

  create(projectId: string, request: CreateKnowledgeEntryRequest): Observable<KnowledgeEntry> {
    return this.http.post<KnowledgeEntry>(`${this.apiUrl}/project/${projectId}`, request);
  }

  update(id: string, data: Partial<KnowledgeEntry>): Observable<KnowledgeEntry> {
    return this.http.put<KnowledgeEntry>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
