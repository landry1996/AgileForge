import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Portfolio, PortfolioDashboard, PortfolioProjectSummary, RiskHeatMapEntry } from '../models/enterprise.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  create(data: { organizationId: string; name: string; description: string; ownerId: string; projectIds?: string[] }): Observable<Portfolio> {
    return this.http.post<Portfolio>(`${this.apiUrl}/portfolios`, data);
  }

  getById(id: string): Observable<Portfolio> {
    return this.http.get<Portfolio>(`${this.apiUrl}/portfolios/${id}`);
  }

  getByOrganization(orgId: string): Observable<Portfolio[]> {
    return this.http.get<Portfolio[]>(`${this.apiUrl}/organizations/${orgId}/portfolios`);
  }

  update(id: string, data: { name?: string; description?: string }): Observable<Portfolio> {
    return this.http.put<Portfolio>(`${this.apiUrl}/portfolios/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/portfolios/${id}`);
  }

  addProject(portfolioId: string, projectId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/portfolios/${portfolioId}/projects/${projectId}`, null);
  }

  removeProject(portfolioId: string, projectId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/portfolios/${portfolioId}/projects/${projectId}`);
  }

  getDashboard(portfolioId: string): Observable<PortfolioDashboard> {
    return this.http.get<PortfolioDashboard>(`${this.apiUrl}/portfolios/${portfolioId}/dashboard`);
  }

  getRiskHeatMap(portfolioId: string): Observable<RiskHeatMapEntry[]> {
    return this.http.get<RiskHeatMapEntry[]>(`${this.apiUrl}/portfolios/${portfolioId}/risk`);
  }

  getProjectSummaries(portfolioId: string): Observable<PortfolioProjectSummary[]> {
    return this.http.get<PortfolioProjectSummary[]>(`${this.apiUrl}/portfolios/${portfolioId}/projects`);
  }
}
