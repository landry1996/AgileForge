import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateTimeEntryRequest, TimeEntry, TimeTrackingSummary } from '../models/time-tracking.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TimeTrackingService {
  private readonly apiUrl = `${environment.apiUrl}/time-entries`;

  constructor(private http: HttpClient) {}

  logTime(ticketId: string, request: CreateTimeEntryRequest): Observable<TimeEntry> {
    return this.http.post<TimeEntry>(`${this.apiUrl}/tickets/${ticketId}`, request);
  }

  getByTicket(ticketId: string): Observable<TimeEntry[]> {
    return this.http.get<TimeEntry[]>(`${this.apiUrl}/tickets/${ticketId}`);
  }

  getTimeSummary(ticketId: string): Observable<TimeTrackingSummary> {
    return this.http.get<TimeTrackingSummary>(`${this.apiUrl}/tickets/${ticketId}/summary`);
  }

  getMyEntries(): Observable<TimeEntry[]> {
    return this.http.get<TimeEntry[]>(`${this.apiUrl}/my`);
  }

  getWeeklyTotal(): Observable<{ totalHours: number }> {
    return this.http.get<{ totalHours: number }>(`${this.apiUrl}/my/weekly`);
  }

  deleteEntry(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
