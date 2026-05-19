import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateDocumentRequest, Document, DocumentTree, DocumentVersion, UpdateDocumentRequest } from '../models/document.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly apiUrl = `${environment.apiUrl}/documents`;

  constructor(private http: HttpClient) {}

  getByProject(projectId: string): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.apiUrl}/project/${projectId}`);
  }

  getTree(projectId: string): Observable<DocumentTree[]> {
    return this.http.get<DocumentTree[]>(`${this.apiUrl}/project/${projectId}/tree`);
  }

  getById(id: string): Observable<Document> {
    return this.http.get<Document>(`${this.apiUrl}/${id}`);
  }

  create(projectId: string, request: CreateDocumentRequest): Observable<Document> {
    return this.http.post<Document>(`${this.apiUrl}/project/${projectId}`, request);
  }

  update(id: string, request: UpdateDocumentRequest): Observable<Document> {
    return this.http.put<Document>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getVersions(id: string): Observable<DocumentVersion[]> {
    return this.http.get<DocumentVersion[]>(`${this.apiUrl}/${id}/versions`);
  }

  restoreVersion(id: string, version: number): Observable<Document> {
    return this.http.post<Document>(`${this.apiUrl}/${id}/versions/${version}/restore`, {});
  }

  linkTicket(id: string, ticketId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/tickets/${ticketId}`, {});
  }

  unlinkTicket(id: string, ticketId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/tickets/${ticketId}`);
  }
}
