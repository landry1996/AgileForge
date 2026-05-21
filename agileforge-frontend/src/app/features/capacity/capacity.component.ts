import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CapacityService } from '../../core/services/capacity.service';
import { TeamCapacity, CapacityForecast } from '../../core/models/enterprise.model';

@Component({
  selector: 'app-capacity',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="capacity-container">
      <div class="page-header">
        <h1>Capacity Planning</h1>
      </div>

      @if (forecast()) {
        <div class="summary-cards">
          <div class="card stat-card">
            <span class="stat-value">{{ forecast()!.currentSprintCapacity | number:'1.0-0' }}h</span>
            <span class="stat-label">Sprint Capacity</span>
          </div>
          <div class="card stat-card">
            <span class="stat-value">{{ forecast()!.averageVelocity | number:'1.0-1' }}</span>
            <span class="stat-label">Avg Velocity</span>
          </div>
          <div class="card stat-card">
            <span class="stat-value">{{ forecast()!.estimatedSprints >= 0 ? forecast()!.estimatedSprints : '?' }}</span>
            <span class="stat-label">Sprints to Clear Backlog</span>
          </div>
        </div>

        @if (forecast()!.bottleneckSkills.length > 0) {
          <div class="card bottleneck-alert">
            <h3>Bottlenecks Detected</h3>
            @for (skill of forecast()!.bottleneckSkills; track skill) {
              <span class="bottleneck-badge">{{ skill }}</span>
            }
          </div>
        }
      }

      @if (teamCapacity()) {
        <div class="card">
          <h3>Team Capacity</h3>
          <div class="capacity-summary">
            <span>Available: {{ teamCapacity()!.totalAvailable }}h</span>
            <span>Leave: {{ teamCapacity()!.totalLeave }}h</span>
            <span>Net: {{ teamCapacity()!.netCapacity }}h</span>
          </div>
          <div class="members-list">
            @for (member of teamCapacity()!.members; track member.userId) {
              <div class="member-row">
                <span class="member-name">{{ member.userName }}</span>
                <div class="member-stats">
                  <span>{{ member.availableHours }}h available</span>
                  <span>{{ member.leaveHours }}h leave</span>
                  <span>{{ member.assignedPoints }} pts</span>
                </div>
                <div class="load-bar-container">
                  <div class="load-bar" [style.width.%]="member.loadPercentage"
                       [class.overloaded]="member.loadPercentage > 90"
                       [class.high]="member.loadPercentage > 70 && member.loadPercentage <= 90">
                  </div>
                  <span class="load-text">{{ member.loadPercentage }}%</span>
                </div>
              </div>
            }
          </div>
        </div>
      }

      <div class="card add-entry-section">
        <h3>Add Capacity Entry</h3>
        <div class="entry-form">
          <input [(ngModel)]="entryUserId" placeholder="User ID" class="input">
          <input type="number" [(ngModel)]="entryHours" placeholder="Available hours" class="input">
          <input type="number" [(ngModel)]="entryLeave" placeholder="Leave hours" class="input">
          <input [(ngModel)]="entryNotes" placeholder="Notes" class="input">
          <button class="btn-primary" (click)="addEntry()">Add</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .capacity-container { padding: 24px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .page-header h1 { color: #e6edf3; margin: 0; }
    .summary-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
    .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
    .stat-card { text-align: center; }
    .stat-value { display: block; font-size: 2rem; font-weight: 700; color: #e6edf3; }
    .stat-label { color: #8b949e; font-size: 0.85rem; }
    .bottleneck-alert { border-color: #f85149; }
    .bottleneck-alert h3 { color: #f85149; margin-bottom: 8px; }
    .bottleneck-badge { background: #f8514920; color: #f85149; padding: 4px 10px; border-radius: 12px; font-size: 0.8rem; margin-right: 8px; }
    h3 { color: #e6edf3; margin-bottom: 12px; }
    .capacity-summary { display: flex; gap: 24px; color: #8b949e; margin-bottom: 16px; }
    .members-list { display: flex; flex-direction: column; gap: 12px; }
    .member-row { display: grid; grid-template-columns: 150px 1fr 200px; align-items: center; gap: 16px; padding: 8px 0; border-bottom: 1px solid #21262d; }
    .member-name { color: #e6edf3; font-weight: 500; }
    .member-stats { display: flex; gap: 12px; color: #8b949e; font-size: 0.8rem; }
    .load-bar-container { position: relative; background: #21262d; height: 20px; border-radius: 4px; overflow: hidden; }
    .load-bar { height: 100%; background: #238636; transition: width 0.3s; }
    .load-bar.high { background: #d29922; }
    .load-bar.overloaded { background: #f85149; }
    .load-text { position: absolute; right: 6px; top: 2px; font-size: 0.75rem; color: #e6edf3; }
    .entry-form { display: flex; gap: 8px; flex-wrap: wrap; }
    .input { background: #0d1117; border: 1px solid #30363d; color: #e6edf3; padding: 8px 12px; border-radius: 6px; }
    .btn-primary { background: #238636; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
  `]
})
export class CapacityComponent implements OnInit {
  teamCapacity = signal<TeamCapacity | null>(null);
  forecast = signal<CapacityForecast | null>(null);

  entryUserId = '';
  entryHours = 40;
  entryLeave = 0;
  entryNotes = '';
  private projectId = '';

  constructor(private capacityService: CapacityService) {}

  ngOnInit() {
    this.projectId = localStorage.getItem('selectedProjectId') || '';
    if (this.projectId) {
      this.loadForecast();
    }
  }

  loadForecast() {
    this.capacityService.getForecast(this.projectId).subscribe(f => this.forecast.set(f));
  }

  addEntry() {
    if (!this.entryUserId || !this.projectId) return;
    this.capacityService.addEntry(this.projectId, {
      userId: this.entryUserId, availableHours: this.entryHours,
      plannedLeaveHours: this.entryLeave, notes: this.entryNotes
    }).subscribe(() => { this.entryUserId = ''; this.entryNotes = ''; this.loadForecast(); });
  }
}
