import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IndustryTemplate, MarketplaceExtension } from '../models/ecosystem.model';

@Injectable({ providedIn: 'root' })
export class TemplateService {
  private http = inject(HttpClient);
  private baseUrl = '/api/templates';

  getAll(): Observable<IndustryTemplate[]> {
    return this.http.get<IndustryTemplate[]>(this.baseUrl);
  }

  getByIndustry(industry: string): Observable<IndustryTemplate[]> {
    return this.http.get<IndustryTemplate[]>(`${this.baseUrl}/industry/${industry}`);
  }

  getById(templateId: string): Observable<IndustryTemplate> {
    return this.http.get<IndustryTemplate>(`${this.baseUrl}/${templateId}`);
  }

  applyTemplate(templateId: string, projectId: string): Observable<unknown> {
    return this.http.post(`${this.baseUrl}/${templateId}/apply/${projectId}`, {});
  }

  createTemplate(data: Partial<IndustryTemplate>): Observable<IndustryTemplate> {
    return this.http.post<IndustryTemplate>(this.baseUrl, data);
  }

  // Marketplace
  getMarketplace(category?: string, page: number = 0, size: number = 20): Observable<MarketplaceExtension[]> {
    const params: Record<string, string | number> = { page, size };
    if (category) params['category'] = category;
    return this.http.get<MarketplaceExtension[]>(`${this.baseUrl}/marketplace`, { params });
  }

  getExtension(extensionId: string): Observable<MarketplaceExtension> {
    return this.http.get<MarketplaceExtension>(`${this.baseUrl}/marketplace/${extensionId}`);
  }

  publishExtension(authorId: string, name: string, description: string,
                   category: string, manifest: string): Observable<MarketplaceExtension> {
    return this.http.post<MarketplaceExtension>(`${this.baseUrl}/marketplace/publish`, {
      authorId, name, description, category, manifest
    });
  }

  installExtension(extensionId: string, organizationId: string, config?: Record<string, unknown>): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/marketplace/${extensionId}/install`, { organizationId, config });
  }

  uninstallExtension(extensionId: string, orgId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/marketplace/${extensionId}/uninstall/${orgId}`);
  }

  getInstalled(orgId: string): Observable<MarketplaceExtension[]> {
    return this.http.get<MarketplaceExtension[]>(`${this.baseUrl}/marketplace/installed/${orgId}`);
  }
}
