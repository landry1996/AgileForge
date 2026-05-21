import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiKeyService } from '../../core/services/api-key.service';
import { ApiKeyResponse, ApiKeyCreated } from '../../core/models/enterprise.model';

@Component({
  selector: 'app-api-keys',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="api-keys-container">
      <div class="page-header">
        <h1>API Keys</h1>
        <button class="btn-primary" (click)="showCreate = !showCreate">+ Generate Key</button>
      </div>

      @if (createdKey()) {
        <div class="card created-key-alert">
          <h3>API Key Created Successfully</h3>
          <p class="warning">Copy this key now. You won't be able to see it again.</p>
          <div class="key-display">
            <code>{{ createdKey()!.key }}</code>
            <button class="btn-copy" (click)="copyKey()">Copy</button>
          </div>
          <button class="btn-secondary" (click)="createdKey.set(null)">Dismiss</button>
        </div>
      }

      @if (showCreate) {
        <div class="card create-form">
          <h3>Generate New API Key</h3>
          <input [(ngModel)]="newKeyName" placeholder="Key name (e.g. CI/CD Pipeline)" class="input">
          <div class="permissions-select">
            <label class="perm-checkbox">
              <input type="checkbox" [checked]="selectedPerms.includes('read')" (change)="togglePerm('read')"> Read
            </label>
            <label class="perm-checkbox">
              <input type="checkbox" [checked]="selectedPerms.includes('write')" (change)="togglePerm('write')"> Write
            </label>
            <label class="perm-checkbox">
              <input type="checkbox" [checked]="selectedPerms.includes('admin')" (change)="togglePerm('admin')"> Admin
            </label>
            <label class="perm-checkbox">
              <input type="checkbox" [checked]="selectedPerms.includes('webhooks')" (change)="togglePerm('webhooks')"> Webhooks
            </label>
          </div>
          <input type="datetime-local" [(ngModel)]="newKeyExpiry" class="input" placeholder="Expiration (optional)">
          <button class="btn-primary" (click)="create()">Generate</button>
        </div>
      }

      <div class="keys-list">
        @for (key of keys(); track key.id) {
          <div class="card key-card">
            <div class="key-header">
              <span class="key-name">{{ key.name }}</span>
              <span class="badge" [class.active]="key.active" [class.inactive]="!key.active">
                {{ key.active ? 'Active' : 'Revoked' }}
              </span>
            </div>
            <div class="key-info">
              <span class="key-prefix">{{ key.keyPrefix }}...</span>
              <div class="key-perms">
                @for (perm of key.permissions; track perm) {
                  <span class="perm-tag">{{ perm }}</span>
                }
              </div>
            </div>
            <div class="key-meta">
              <span>Created: {{ key.createdAt | date:'mediumDate' }}</span>
              @if (key.expiresAt) {
                <span>Expires: {{ key.expiresAt | date:'mediumDate' }}</span>
              }
              @if (key.lastUsedAt) {
                <span>Last used: {{ key.lastUsedAt | date:'short' }}</span>
              }
            </div>
            <div class="key-actions">
              <button class="btn-danger-sm" (click)="revoke(key.id)">Revoke</button>
            </div>
          </div>
        }
        @if (keys().length === 0) {
          <div class="empty-state">No API keys. Generate one to enable programmatic access.</div>
        }
      </div>
    </div>
  `,
  styles: [`
    .api-keys-container { padding: 24px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .page-header h1 { color: #e6edf3; margin: 0; }
    .btn-primary { background: #238636; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-secondary { background: #21262d; color: #e6edf3; border: 1px solid #30363d; padding: 6px 12px; border-radius: 6px; cursor: pointer; }
    .btn-copy { background: #1f6feb; color: #fff; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; }
    .btn-danger-sm { background: #da3633; color: #fff; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; }
    .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
    .created-key-alert { border-color: #d29922; }
    .created-key-alert h3 { color: #d29922; margin: 0 0 8px; }
    .warning { color: #d29922; font-size: 0.85rem; margin-bottom: 12px; }
    .key-display { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; }
    .key-display code { background: #0d1117; color: #3fb950; padding: 8px 16px; border-radius: 6px; font-size: 0.9rem; flex: 1; word-break: break-all; }
    .create-form { display: flex; flex-direction: column; gap: 12px; }
    .create-form h3 { color: #e6edf3; margin: 0; }
    .input { background: #0d1117; border: 1px solid #30363d; color: #e6edf3; padding: 8px 12px; border-radius: 6px; }
    .permissions-select { display: flex; gap: 16px; }
    .perm-checkbox { color: #e6edf3; display: flex; align-items: center; gap: 4px; }
    .key-card { }
    .key-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
    .key-name { color: #e6edf3; font-weight: 600; }
    .badge { padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
    .badge.active { background: #23883733; color: #3fb950; }
    .badge.inactive { background: #8b949e33; color: #8b949e; }
    .key-info { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
    .key-prefix { color: #8b949e; font-family: monospace; }
    .key-perms { display: flex; gap: 6px; }
    .perm-tag { background: #1f6feb20; color: #58a6ff; padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; }
    .key-meta { color: #8b949e; font-size: 0.8rem; display: flex; gap: 16px; margin-bottom: 8px; }
    .key-actions { }
    .empty-state { color: #8b949e; text-align: center; padding: 40px; }
  `]
})
export class ApiKeysComponent implements OnInit {
  keys = signal<ApiKeyResponse[]>([]);
  createdKey = signal<ApiKeyCreated | null>(null);
  showCreate = false;
  newKeyName = '';
  newKeyExpiry = '';
  selectedPerms: string[] = ['read'];
  private orgId = '';

  constructor(private apiKeyService: ApiKeyService) {}

  ngOnInit() {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.orgId = user.organizationId || '';
    this.loadKeys();
  }

  loadKeys() {
    if (this.orgId) {
      this.apiKeyService.getByOrganization(this.orgId).subscribe(k => this.keys.set(k));
    }
  }

  togglePerm(perm: string) {
    const idx = this.selectedPerms.indexOf(perm);
    if (idx >= 0) this.selectedPerms.splice(idx, 1);
    else this.selectedPerms.push(perm);
  }

  create() {
    if (!this.newKeyName) return;
    this.apiKeyService.create(this.orgId, {
      name: this.newKeyName, permissions: this.selectedPerms,
      expiresAt: this.newKeyExpiry || undefined
    }).subscribe(created => {
      this.createdKey.set(created);
      this.loadKeys();
      this.showCreate = false;
      this.newKeyName = '';
      this.selectedPerms = ['read'];
    });
  }

  revoke(id: string) {
    this.apiKeyService.revoke(id).subscribe(() => this.loadKeys());
  }

  copyKey() {
    if (this.createdKey()) {
      navigator.clipboard.writeText(this.createdKey()!.key);
    }
  }
}
