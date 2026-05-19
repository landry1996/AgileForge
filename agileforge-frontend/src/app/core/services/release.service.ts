import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateReleaseRequest, CreateRoadmapItemRequest, Release, ReleaseReadiness, RoadmapItem } from '../models/release.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReleaseService {
  private readonly apiUrl = `${environment.apiUrl}/releases`;

  constructor(private http: HttpClient) {}

  getByProject(projectId: string): Observable<Release[]> {
    return this.http.get<Release[]>(`${this.apiUrl}/project/${projectId}`);
  }

  getById(id: string): Observable<Release> {
    return this.http.get<Release>(`${this.apiUrl}/${id}`);
  }

  create(projectId: string, request: CreateReleaseRequest): Observable<Release> {
    return this.http.post<Release>(`${this.apiUrl}/project/${projectId}`, request);
  }

  update(id: string, data: Partial<Release>): Observable<Release> {
    return this.http.put<Release>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addTicket(releaseId: string, ticketId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${releaseId}/tickets/${ticketId}`, {});
  }

  removeTicket(releaseId: string, ticketId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${releaseId}/tickets/${ticketId}`);
  }

  release(id: string): Observable<Release> {
    return this.http.post<Release>(`${this.apiUrl}/${id}/release`, {});
  }

  getReadiness(id: string): Observable<ReleaseReadiness> {
    return this.http.get<ReleaseReadiness>(`${this.apiUrl}/${id}/readiness`);
  }

  getRoadmap(projectId: string): Observable<RoadmapItem[]> {
    return this.http.get<RoadmapItem[]>(`${environment.apiUrl}/roadmap/project/${projectId}`);
  }

  createRoadmapItem(projectId: string, request: CreateRoadmapItemRequest): Observable<RoadmapItem> {
    return this.http.post<RoadmapItem>(`${environment.apiUrl}/roadmap/project/${projectId}`, request);
  }

  updateRoadmapItem(id: string, data: Partial<RoadmapItem>): Observable<RoadmapItem> {
    return this.http.put<RoadmapItem>(`${environment.apiUrl}/roadmap/${id}`, data);
  }

  deleteRoadmapItem(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/roadmap/${id}`);
  }
}
