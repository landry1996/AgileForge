import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WebhookSubscription } from '../models/enterprise.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WebhookService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  create(projectId: string, data: { url: string; secret?: string; events: string[] }): Observable<WebhookSubscription> {
    return this.http.post<WebhookSubscription>(`${this.apiUrl}/projects/${projectId}/webhooks`, data);
  }

  getByProject(projectId: string): Observable<WebhookSubscription[]> {
    return this.http.get<WebhookSubscription[]>(`${this.apiUrl}/projects/${projectId}/webhooks`);
  }

  update(webhookId: string, data: { url?: string; secret?: string; events?: string[] }): Observable<WebhookSubscription> {
    return this.http.put<WebhookSubscription>(`${this.apiUrl}/webhooks/${webhookId}`, data);
  }

  delete(webhookId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/webhooks/${webhookId}`);
  }

  test(webhookId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/webhooks/${webhookId}/test`, null);
  }
}
