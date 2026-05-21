import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AiAgent, AiAgentTask, SprintPlanSuggestion, RiskReport } from '../models/ecosystem.model';

@Injectable({ providedIn: 'root' })
export class AiAgentService {
  private http = inject(HttpClient);
  private baseUrl = '/api/ai-agents';

  getAvailableAgents(): Observable<AiAgent[]> {
    return this.http.get<AiAgent[]>(this.baseUrl);
  }

  getAgent(agentId: string): Observable<AiAgent> {
    return this.http.get<AiAgent>(`${this.baseUrl}/${agentId}`);
  }

  executeTask(agentId: string, organizationId: string, triggeredBy: string,
              taskType: string, projectId?: string, input?: Record<string, unknown>): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${agentId}/execute`, {
      organizationId, triggeredBy, taskType, projectId, input
    });
  }

  suggestSprintPlan(projectId: string, sprintId?: string): Observable<SprintPlanSuggestion> {
    const params: Record<string, string> = {};
    if (sprintId) params['sprintId'] = sprintId;
    return this.http.get<SprintPlanSuggestion>(`${this.baseUrl}/sprint-plan/${projectId}`, { params });
  }

  analyzeRisks(projectId: string): Observable<RiskReport> {
    return this.http.get<RiskReport>(`${this.baseUrl}/risks/${projectId}`);
  }

  generateDocumentation(projectId: string, docType: string): Observable<{ content: string }> {
    return this.http.post<{ content: string }>(`${this.baseUrl}/documentation/${projectId}`, { docType });
  }

  generateRetrospective(sprintId: string): Observable<{ content: string }> {
    return this.http.get<{ content: string }>(`${this.baseUrl}/retrospective/${sprintId}`);
  }

  getTaskHistory(projectId: string, page: number = 0, size: number = 20): Observable<AiAgentTask[]> {
    return this.http.get<AiAgentTask[]>(`${this.baseUrl}/tasks/${projectId}`, { params: { page, size } });
  }

  getTaskResult(taskId: string): Observable<AiAgentTask> {
    return this.http.get<AiAgentTask>(`${this.baseUrl}/tasks/result/${taskId}`);
  }
}
