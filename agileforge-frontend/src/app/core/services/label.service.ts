import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Attachment, CreateLabelRequest, Label } from '../models/label.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LabelService {
  private readonly apiUrl = `${environment.apiUrl}/labels`;
  private readonly attachmentsUrl = `${environment.apiUrl}/attachments`;

  constructor(private http: HttpClient) {}

  getProjectLabels(projectId: string): Observable<Label[]> {
    return this.http.get<Label[]>(`${this.apiUrl}/project/${projectId}`);
  }

  createLabel(projectId: string, request: CreateLabelRequest): Observable<Label> {
    return this.http.post<Label>(`${this.apiUrl}/project/${projectId}`, request);
  }

  updateLabel(id: string, data: Partial<Label>): Observable<Label> {
    return this.http.put<Label>(`${this.apiUrl}/${id}`, data);
  }

  deleteLabel(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addLabelToTicket(ticketId: string, labelId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/tickets/${ticketId}/labels/${labelId}`, {});
  }

  removeLabelFromTicket(ticketId: string, labelId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/tickets/${ticketId}/labels/${labelId}`);
  }

  getTicketLabels(ticketId: string): Observable<Label[]> {
    return this.http.get<Label[]>(`${this.apiUrl}/tickets/${ticketId}`);
  }

  uploadAttachment(ticketId: string, file: File): Observable<Attachment> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Attachment>(`${this.attachmentsUrl}/tickets/${ticketId}`, formData);
  }

  getAttachments(ticketId: string): Observable<Attachment[]> {
    return this.http.get<Attachment[]>(`${this.attachmentsUrl}/tickets/${ticketId}`);
  }

  deleteAttachment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.attachmentsUrl}/${id}`);
  }

  downloadAttachment(id: string): Observable<Blob> {
    return this.http.get(`${this.attachmentsUrl}/${id}/download`, { responseType: 'blob' });
  }
}
