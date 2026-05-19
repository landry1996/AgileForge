import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateWorkflowRequest, Workflow } from '../models/workflow.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WorkflowService {
  private readonly apiUrl = `${environment.apiUrl}/workflows`;

  constructor(private http: HttpClient) {}

  getByProject(projectId: string): Observable<Workflow[]> {
    return this.http.get<Workflow[]>(`${this.apiUrl}/project/${projectId}`);
  }

  getForTicketType(projectId: string, ticketType: string): Observable<Workflow> {
    return this.http.get<Workflow>(`${this.apiUrl}/project/${projectId}/type/${ticketType}`);
  }

  create(projectId: string, request: CreateWorkflowRequest): Observable<Workflow> {
    return this.http.post<Workflow>(`${this.apiUrl}/project/${projectId}`, request);
  }

  update(id: string, data: Partial<Workflow>): Observable<Workflow> {
    return this.http.put<Workflow>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  validateTransition(workflowId: string, fromStatus: string, toStatus: string): Observable<{ valid: boolean }> {
    return this.http.get<{ valid: boolean }>(`${this.apiUrl}/${workflowId}/validate`, {
      params: { fromStatus, toStatus }
    });
  }
}
