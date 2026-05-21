import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebhookService } from '../../core/services/webhook.service';
import { WebhookSubscription } from '../../core/models/enterprise.model';

@Component({
  selector: 'app-webhooks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="webhooks-container">
      <div class="page-header">
        <h1>Webhooks</h1>
        <button class="btn-primary" (click)="showCreate = !showCreate">+ New Webhook</button>
      </div>

      @if (showCreate) {
        <div class="card create-form">
          <h3>Create Webhook</h3>
          <input [(ngModel)]="newUrl" placeholder="Payload URL (https://...)" class="input">
          <input [(ngModel)]="newSecret" placeholder="Secret (optional)" class="input">
          <div class="events-select">
            <label class="event-checkbox" *ngFor="let evt of availableEvents">
              <input type="checkbox" [checked]="selectedEvents.includes(evt)" (change)="toggleEvent(evt)">
              {{ evt }}
            </label>
          </div>
          <button class="btn-primary" (click)="create()">Create</button>
        </div>
      }

      <div class="webhooks-list">
        @for (webhook of webhooks(); track webhook.id) {
          <div class="card webhook-card">
            <div class="webhook-header">
              <span class="webhook-url">{{ webhook.url }}</span>
              <span class="badge" [class.active]="webhook.active" [class.inactive]="!webhook.active">
                {{ webhook.active ? 'Active' : 'Inactive' }}
              </span>
            </div>
            <div class="webhook-events">
              @for (evt of webhook.events; track evt) {
                <span class="event-tag">{{ evt }}</span>
              }
            </div>
            <div class="webhook-meta">
              @if (webhook.lastTriggeredAt) {
                <span>Last triggered: {{ webhook.lastTriggeredAt | date:'short' }}</span>
              }
              @if (webhook.failureCount > 0) {
                <span class="failure-count">Failures: {{ webhook.failureCount }}</span>
              }
            </div>
            <div class="webhook-actions">
              <button class="btn-secondary" (click)="test(webhook.id)">Test</button>
              <button class="btn-danger-sm" (click)="deleteWebhook(webhook.id)">Delete</button>
            </div>
          </div>
        }
        @if (webhooks().length === 0) {
          <div class="empty-state">No webhooks configured. Create one to receive event notifications.</div>
        }
      </div>
    </div>
  `,
  styles: [`
    .webhooks-container { padding: 24px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .page-header h1 { color: #e6edf3; margin: 0; }
    .btn-primary { background: #238636; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-secondary { background: #21262d; color: #e6edf3; border: 1px solid #30363d; padding: 6px 12px; border-radius: 6px; cursor: pointer; }
    .btn-danger-sm { background: #da3633; color: #fff; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; }
    .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
    .create-form { display: flex; flex-direction: column; gap: 12px; }
    .create-form h3 { color: #e6edf3; margin: 0; }
    .input { background: #0d1117; border: 1px solid #30363d; color: #e6edf3; padding: 8px 12px; border-radius: 6px; }
    .events-select { display: flex; flex-wrap: wrap; gap: 12px; }
    .event-checkbox { color: #e6edf3; display: flex; align-items: center; gap: 4px; font-size: 0.85rem; }
    .webhook-card { }
    .webhook-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
    .webhook-url { color: #58a6ff; font-family: monospace; font-size: 0.9rem; }
    .badge { padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
    .badge.active { background: #23883733; color: #3fb950; }
    .badge.inactive { background: #8b949e33; color: #8b949e; }
    .webhook-events { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
    .event-tag { background: #1f6feb20; color: #58a6ff; padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; }
    .webhook-meta { color: #8b949e; font-size: 0.8rem; display: flex; gap: 16px; margin-bottom: 8px; }
    .failure-count { color: #f85149; }
    .webhook-actions { display: flex; gap: 8px; }
    .empty-state { color: #8b949e; text-align: center; padding: 40px; }
  `]
})
export class WebhooksComponent implements OnInit {
  webhooks = signal<WebhookSubscription[]>([]);
  showCreate = false;
  newUrl = '';
  newSecret = '';
  selectedEvents: string[] = [];
  availableEvents = ['ticket.created', 'ticket.updated', 'ticket.deleted', 'sprint.started', 'sprint.completed', 'release.published', 'incident.created', 'incident.resolved'];
  private projectId = '';

  constructor(private webhookService: WebhookService) {}

  ngOnInit() {
    this.projectId = localStorage.getItem('selectedProjectId') || '';
    this.loadWebhooks();
  }

  loadWebhooks() {
    if (this.projectId) {
      this.webhookService.getByProject(this.projectId).subscribe(w => this.webhooks.set(w));
    }
  }

  toggleEvent(evt: string) {
    const idx = this.selectedEvents.indexOf(evt);
    if (idx >= 0) this.selectedEvents.splice(idx, 1);
    else this.selectedEvents.push(evt);
  }

  create() {
    if (!this.newUrl) return;
    this.webhookService.create(this.projectId, { url: this.newUrl, secret: this.newSecret || undefined, events: this.selectedEvents })
      .subscribe(() => { this.loadWebhooks(); this.showCreate = false; this.newUrl = ''; this.newSecret = ''; this.selectedEvents = []; });
  }

  test(id: string) {
    this.webhookService.test(id).subscribe();
  }

  deleteWebhook(id: string) {
    this.webhookService.delete(id).subscribe(() => this.loadWebhooks());
  }
}
