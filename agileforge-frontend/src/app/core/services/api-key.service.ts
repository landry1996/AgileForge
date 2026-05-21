import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiKeyResponse, ApiKeyCreated } from '../models/enterprise.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiKeyService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  create(orgId: string, data: { name: string; permissions?: string[]; expiresAt?: string }): Observable<ApiKeyCreated> {
    return this.http.post<ApiKeyCreated>(`${this.apiUrl}/organizations/${orgId}/api-keys`, data);
  }

  getByOrganization(orgId: string): Observable<ApiKeyResponse[]> {
    return this.http.get<ApiKeyResponse[]>(`${this.apiUrl}/organizations/${orgId}/api-keys`);
  }

  revoke(keyId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api-keys/${keyId}`);
  }
}
