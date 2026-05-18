import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Project } from '../models/project.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly apiUrl = `${environment.apiUrl}/projects`;

  constructor(private http: HttpClient) {}

  getByOrganization(orgId: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/organization/${orgId}`);
  }

  getById(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`);
  }

  getMyProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/my`);
  }

  create(orgId: string, data: Partial<Project>): Observable<Project> {
    return this.http.post<Project>(`${this.apiUrl}/organization/${orgId}`, data);
  }

  update(id: string, data: Partial<Project>): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${id}`, data);
  }
}
