import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditEvent, AuditSummary, AuditAlertRule } from '../models/enterprise.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAuditLog(orgId: string, filters: {
    userId?: string; action?: string; entityType?: string;
    severity?: string; fromDate?: string; toDate?: string;
    page?: number; size?: number;
  } = {}): Observable<AuditEvent[]> {
    let params = new HttpParams();
    if (filters.userId) params = params.set('userId', filters.userId);
    if (filters.action) params = params.set('action', filters.action);
    if (filters.entityType) params = params.set('entityType', filters.entityType);
    if (filters.severity) params = params.set('severity', filters.severity);
    if (filters.fromDate) params = params.set('fromDate', filters.fromDate);
    if (filters.toDate) params = params.set('toDate', filters.toDate);
    params = params.set('page', (filters.page ?? 0).toString());
    params = params.set('size', (filters.size ?? 20).toString());
    return this.http.get<AuditEvent[]>(`${this.apiUrl}/organizations/${orgId}/audit`, { params });
  }

  getSummary(orgId: string): Observable<AuditSummary> {
    return this.http.get<AuditSummary>(`${this.apiUrl}/organizations/${orgId}/audit/summary`);
  }

  exportAuditLog(orgId: string, from?: string, to?: string): Observable<AuditEvent[]> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);
    return this.http.post<AuditEvent[]>(`${this.apiUrl}/organizations/${orgId}/audit/export`, null, { params });
  }

  createAlertRule(orgId: string, rule: { name: string; actionPattern: string; severity: string; notifyEmails: string }): Observable<AuditAlertRule> {
    return this.http.post<AuditAlertRule>(`${this.apiUrl}/organizations/${orgId}/audit/alert-rules`, rule);
  }

  getAlertRules(orgId: string): Observable<AuditAlertRule[]> {
    return this.http.get<AuditAlertRule[]>(`${this.apiUrl}/organizations/${orgId}/audit/alert-rules`);
  }

  deleteAlertRule(ruleId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/audit/alert-rules/${ruleId}`);
  }
}
