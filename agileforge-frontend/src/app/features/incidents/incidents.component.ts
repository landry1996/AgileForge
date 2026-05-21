import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IncidentService } from '../../core/services/incident.service';
import { Incident, IncidentTimeline } from '../../core/models/enterprise.model';

@Component({
  selector: 'app-incidents',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="incidents-container">
      <div class="page-header">
        <h1>Incident Management</h1>
        <button class="btn-danger" (click)="showCreate = !showCreate">Declare Incident</button>
      </div>

      @if (showCreate) {
        <div class="card create-form">
          <h3>New Incident</h3>
          <input [(ngModel)]="newTitle" placeholder="Incident title" class="input">
          <textarea [(ngModel)]="newDescription" placeholder="Description" class="input textarea"></textarea>
          <select [(ngModel)]="newSeverity" class="input">
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
          <button class="btn-danger" (click)="createIncident()">Create Incident</button>
        </div>
      }

      @if (selectedTimeline()) {
        <div class="card timeline-view">
          <div class="timeline-header">
            <h2>{{ selectedTimeline()!.title }}</h2>
            <span class="badge" [class]="selectedTimeline()!.severity.toLowerCase()">{{ selectedTimeline()!.severity }}</span>
            <span class="badge status">{{ selectedTimeline()!.status }}</span>
            <span class="duration">{{ selectedTimeline()!.durationMinutes }} min</span>
            <button class="btn-back" (click)="selectedTimeline.set(null)">Back</button>
          </div>
          <div class="timeline-events">
            @for (event of selectedTimeline()!.events; track event.id) {
              <div class="timeline-event">
                <div class="event-dot"></div>
                <div class="event-content">
                  <span class="event-type">{{ event.eventType }}</span>
                  <span class="event-message">{{ event.message }}</span>
                  <span class="event-time">{{ event.createdAt | date:'medium' }}</span>
                </div>
              </div>
            }
          </div>
          <div class="timeline-actions">
            <input [(ngModel)]="newEventMessage" placeholder="Add event message" class="input">
            <select [(ngModel)]="newEventType" class="input">
              <option value="UPDATE">Update</option>
              <option value="COMMUNICATION">Communication</option>
              <option value="ACTION_TAKEN">Action Taken</option>
              <option value="ESCALATION">Escalation</option>
            </select>
            <button class="btn-primary" (click)="addEvent()">Add Event</button>
            <button class="btn-resolve" (click)="showResolve = !showResolve">Resolve</button>
          </div>
          @if (showResolve) {
            <div class="resolve-form">
              <textarea [(ngModel)]="resolution" placeholder="Resolution description" class="input textarea"></textarea>
              <button class="btn-primary" (click)="resolveIncident()">Confirm Resolution</button>
            </div>
          }
        </div>
      } @else {
        <div class="incidents-grid">
          @for (incident of incidents(); track incident.id) {
            <div class="card incident-card" [class]="incident.severity.toLowerCase()" (click)="loadTimeline(incident.id)">
              <div class="incident-header">
                <span class="badge" [class]="incident.severity.toLowerCase()">{{ incident.severity }}</span>
                <span class="badge status">{{ incident.status }}</span>
              </div>
              <h3>{{ incident.title }}</h3>
              <p>{{ incident.description }}</p>
              <div class="incident-meta">
                <span>Duration: {{ incident.durationMinutes }} min</span>
                <span>Participants: {{ incident.participantIds.length }}</span>
              </div>
            </div>
          }
          @if (incidents().length === 0) {
            <div class="empty-state">No incidents. All systems operational.</div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .incidents-container { padding: 24px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .page-header h1 { color: #e6edf3; margin: 0; }
    .btn-danger { background: #da3633; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-danger:hover { background: #f85149; }
    .btn-primary { background: #238636; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-resolve { background: #1f6feb; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
    .btn-back { background: #21262d; color: #e6edf3; border: 1px solid #30363d; padding: 6px 12px; border-radius: 6px; cursor: pointer; margin-left: auto; }
    .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
    .create-form { display: flex; flex-direction: column; gap: 12px; }
    .create-form h3 { color: #e6edf3; margin: 0; }
    .input { background: #0d1117; border: 1px solid #30363d; color: #e6edf3; padding: 8px 12px; border-radius: 6px; }
    .textarea { min-height: 80px; resize: vertical; }
    .incidents-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
    .incident-card { cursor: pointer; transition: border-color 0.2s; }
    .incident-card:hover { border-color: #58a6ff; }
    .incident-card.critical { border-left: 4px solid #f85149; }
    .incident-card.high { border-left: 4px solid #db6d28; }
    .incident-card.medium { border-left: 4px solid #d29922; }
    .incident-card.low { border-left: 4px solid #8b949e; }
    .incident-header { display: flex; gap: 8px; margin-bottom: 8px; }
    .incident-card h3 { color: #e6edf3; margin: 0 0 4px; }
    .incident-card p { color: #8b949e; margin: 0 0 8px; font-size: 0.9rem; }
    .incident-meta { color: #8b949e; font-size: 0.8rem; display: flex; gap: 16px; }
    .badge { padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
    .badge.critical { background: #f8514933; color: #f85149; }
    .badge.high { background: #db6d2833; color: #db6d28; }
    .badge.medium { background: #d2992233; color: #d29922; }
    .badge.low { background: #8b949e33; color: #8b949e; }
    .badge.status { background: #1f6feb33; color: #58a6ff; }
    .timeline-header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
    .timeline-header h2 { color: #e6edf3; margin: 0; }
    .duration { color: #8b949e; font-size: 0.85rem; }
    .timeline-events { border-left: 2px solid #30363d; margin-left: 12px; padding-left: 20px; margin-bottom: 20px; }
    .timeline-event { position: relative; margin-bottom: 16px; }
    .event-dot { position: absolute; left: -27px; top: 4px; width: 10px; height: 10px; background: #58a6ff; border-radius: 50%; }
    .event-content { display: flex; flex-direction: column; gap: 2px; }
    .event-type { color: #58a6ff; font-weight: 600; font-size: 0.8rem; }
    .event-message { color: #e6edf3; }
    .event-time { color: #8b949e; font-size: 0.75rem; }
    .timeline-actions { display: flex; gap: 8px; flex-wrap: wrap; }
    .resolve-form { margin-top: 12px; display: flex; flex-direction: column; gap: 8px; }
    .empty-state { color: #3fb950; text-align: center; padding: 40px; font-size: 1.1rem; }
  `]
})
export class IncidentsComponent implements OnInit {
  incidents = signal<Incident[]>([]);
  selectedTimeline = signal<IncidentTimeline | null>(null);
  showCreate = false;
  showResolve = false;
  newTitle = '';
  newDescription = '';
  newSeverity = 'HIGH';
  newEventMessage = '';
  newEventType = 'UPDATE';
  resolution = '';
  private projectId = '';

  constructor(private incidentService: IncidentService) {}

  ngOnInit() {
    const stored = localStorage.getItem('selectedProjectId');
    this.projectId = stored || '';
    this.loadIncidents();
  }

  loadIncidents() {
    if (this.projectId) {
      this.incidentService.getByProject(this.projectId).subscribe(i => this.incidents.set(i));
    }
  }

  createIncident() {
    if (!this.newTitle) return;
    this.incidentService.create(this.projectId, { title: this.newTitle, description: this.newDescription, severity: this.newSeverity })
      .subscribe(() => { this.loadIncidents(); this.showCreate = false; this.newTitle = ''; this.newDescription = ''; });
  }

  loadTimeline(id: string) {
    this.incidentService.getTimeline(id).subscribe(t => this.selectedTimeline.set(t));
  }

  addEvent() {
    if (!this.newEventMessage || !this.selectedTimeline()) return;
    this.incidentService.addEvent(this.selectedTimeline()!.incidentId, { eventType: this.newEventType, message: this.newEventMessage })
      .subscribe(() => { this.loadTimeline(this.selectedTimeline()!.incidentId); this.newEventMessage = ''; });
  }

  resolveIncident() {
    if (!this.resolution || !this.selectedTimeline()) return;
    this.incidentService.resolve(this.selectedTimeline()!.incidentId, this.resolution)
      .subscribe(() => { this.loadTimeline(this.selectedTimeline()!.incidentId); this.loadIncidents(); this.showResolve = false; this.resolution = ''; });
  }
}
