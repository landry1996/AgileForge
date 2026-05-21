import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IntegrationConfig, ChannelMapping, JiraImportJob, JiraImportPreview } from '../models/ecosystem.model';

@Injectable({ providedIn: 'root' })
export class IntegrationService {
  private http = inject(HttpClient);
  private baseUrl = '/api/integrations';

  getByOrganization(orgId: string): Observable<IntegrationConfig[]> {
    return this.http.get<IntegrationConfig[]>(`${this.baseUrl}/organization/${orgId}`);
  }

  configure(organizationId: string, provider: string, config: Record<string, unknown>): Observable<IntegrationConfig> {
    return this.http.post<IntegrationConfig>(`${this.baseUrl}/configure`, { organizationId, provider, config });
  }

  enable(integrationId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${integrationId}/enable`, {});
  }

  disable(integrationId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${integrationId}/disable`, {});
  }

  delete(integrationId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${integrationId}`);
  }

  testConnection(integrationId: string): Observable<{ status: string }> {
    return this.http.post<{ status: string }>(`${this.baseUrl}/${integrationId}/test`, {});
  }

  getChannelMappings(integrationId: string): Observable<ChannelMapping[]> {
    return this.http.get<ChannelMapping[]>(`${this.baseUrl}/${integrationId}/channels`);
  }

  addChannelMapping(integrationId: string, projectId: string, channelId: string,
                    channelName: string, events: string[]): Observable<ChannelMapping> {
    return this.http.post<ChannelMapping>(`${this.baseUrl}/${integrationId}/channels`, {
      projectId, channelId, channelName, events
    });
  }

  // Jira Import
  previewJiraImport(jiraUrl: string, projectKey: string, apiToken: string, email: string): Observable<JiraImportPreview> {
    return this.http.post<JiraImportPreview>(`${this.baseUrl}/jira/preview`, { jiraUrl, projectKey, apiToken, email });
  }

  startJiraImport(organizationId: string, targetProjectId: string, jiraUrl: string,
                  projectKey: string, apiToken: string, email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/jira/import`, {
      organizationId, targetProjectId, jiraUrl, projectKey, apiToken, email
    });
  }

  getImportStatus(jobId: string): Observable<JiraImportJob> {
    return this.http.get<JiraImportJob>(`${this.baseUrl}/jira/status/${jobId}`);
  }

  getImportHistory(orgId: string): Observable<JiraImportJob[]> {
    return this.http.get<JiraImportJob[]>(`${this.baseUrl}/jira/history/${orgId}`);
  }
}
