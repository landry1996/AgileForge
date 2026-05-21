import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CapacityEntry, TeamCapacity, CapacityForecast } from '../models/enterprise.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CapacityService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  addEntry(projectId: string, data: { userId: string; sprintId?: string; availableHours: number; plannedLeaveHours?: number; notes?: string }): Observable<CapacityEntry> {
    return this.http.post<CapacityEntry>(`${this.apiUrl}/projects/${projectId}/capacity`, data);
  }

  getTeamCapacity(projectId: string, sprintId: string): Observable<TeamCapacity> {
    const params = new HttpParams().set('sprintId', sprintId);
    return this.http.get<TeamCapacity>(`${this.apiUrl}/projects/${projectId}/capacity/team`, { params });
  }

  getMemberCapacity(projectId: string, userId: string): Observable<CapacityEntry> {
    return this.http.get<CapacityEntry>(`${this.apiUrl}/projects/${projectId}/capacity/member/${userId}`);
  }

  getForecast(projectId: string): Observable<CapacityForecast> {
    return this.http.get<CapacityForecast>(`${this.apiUrl}/projects/${projectId}/capacity/forecast`);
  }

  updateEntry(entryId: string, data: { userId: string; availableHours: number; plannedLeaveHours?: number; notes?: string }): Observable<CapacityEntry> {
    return this.http.put<CapacityEntry>(`${this.apiUrl}/capacity/${entryId}`, data);
  }

  deleteEntry(entryId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/capacity/${entryId}`);
  }
}
