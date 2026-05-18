import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Activity } from '../models/activity.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ActivityService {
  private readonly apiUrl = `${environment.apiUrl}/activity`;

  constructor(private http: HttpClient) {}

  getProjectActivity(projectId: string, limit = 20): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.apiUrl}/project/${projectId}?limit=${limit}`);
  }

  getMyActivity(limit = 20): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.apiUrl}/my?limit=${limit}`);
  }
}
