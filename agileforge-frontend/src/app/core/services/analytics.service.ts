import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectAnalytics, SprintMetrics, TeamWorkload, VelocityDataPoint } from '../models/analytics.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly apiUrl = `${environment.apiUrl}/analytics`;

  constructor(private http: HttpClient) {}

  getProjectAnalytics(projectId: string): Observable<ProjectAnalytics> {
    return this.http.get<ProjectAnalytics>(`${this.apiUrl}/project/${projectId}`);
  }

  getSprintMetrics(sprintId: string): Observable<SprintMetrics> {
    return this.http.get<SprintMetrics>(`${this.apiUrl}/sprints/${sprintId}/metrics`);
  }

  getVelocityHistory(projectId: string): Observable<VelocityDataPoint[]> {
    return this.http.get<VelocityDataPoint[]>(`${this.apiUrl}/project/${projectId}/velocity`);
  }

  getTeamWorkload(projectId: string): Observable<TeamWorkload[]> {
    return this.http.get<TeamWorkload[]>(`${this.apiUrl}/project/${projectId}/workload`);
  }
}
