import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConnectRepositoryRequest, GitBranch, GitRepository, TicketDevInfo } from '../models/git.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class GitIntegrationService {
  private readonly apiUrl = `${environment.apiUrl}/git`;

  constructor(private http: HttpClient) {}

  getRepositories(projectId: string): Observable<GitRepository[]> {
    return this.http.get<GitRepository[]>(`${this.apiUrl}/project/${projectId}/repositories`);
  }

  connectRepository(projectId: string, request: ConnectRepositoryRequest): Observable<GitRepository> {
    return this.http.post<GitRepository>(`${this.apiUrl}/project/${projectId}/repositories`, request);
  }

  disconnectRepository(repositoryId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/repositories/${repositoryId}`);
  }

  getTicketDevInfo(ticketId: string): Observable<TicketDevInfo> {
    return this.http.get<TicketDevInfo>(`${this.apiUrl}/tickets/${ticketId}/dev-info`);
  }

  suggestBranchName(ticketId: string): Observable<{ branchName: string }> {
    return this.http.get<{ branchName: string }>(`${this.apiUrl}/tickets/${ticketId}/suggest-branch`);
  }

  registerBranch(ticketId: string, branchName: string): Observable<GitBranch> {
    return this.http.post<GitBranch>(`${this.apiUrl}/tickets/${ticketId}/branches`, { branchName });
  }
}
