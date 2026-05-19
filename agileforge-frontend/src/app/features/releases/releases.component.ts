import { Component, inject, OnInit, signal } from '@angular/core';
import { ReleaseService } from '../../core/services/release.service';
import { Release, CreateReleaseRequest, ReleaseStatus } from '../../core/models/release.model';

@Component({
  selector: 'app-releases',
  standalone: true,
  template: `
    <div class="releases-page">
      <div class="releases-header">
        <h1>Releases</h1>
        <button class="btn-primary" (click)="showCreateModal.set(true)">+ Create Release</button>
      </div>

      @if (loading()) {
        <div class="loading">Loading releases...</div>
      } @else if (releases().length === 0) {
        <div class="empty-state">
          <div class="empty-icon">&#x1F4E6;</div>
          <h3>No releases yet</h3>
          <p>Create your first release to start tracking deployments.</p>
        </div>
      } @else {
        <div class="releases-grid">
          @for (release of releases(); track release.id) {
            <div class="release-card">
              <div class="release-card-header">
                <div class="release-title-row">
                  <h3>{{ release.name }}</h3>
                  <span class="version-badge">v{{ release.version }}</span>
                </div>
                <span class="status-badge status-{{ release.status.toLowerCase() }}">
                  {{ formatStatus(release.status) }}
                </span>
              </div>

              <div class="release-dates">
                @if (release.startDate) {
                  <span class="date-item">
                    <span class="date-label">Start:</span> {{ release.startDate }}
                  </span>
                }
                @if (release.releaseDate) {
                  <span class="date-item">
                    <span class="date-label">Target:</span> {{ release.releaseDate }}
                  </span>
                }
                @if (release.releasedAt) {
                  <span class="date-item">
                    <span class="date-label">Released:</span> {{ release.releasedAt }}
                  </span>
                }
              </div>

              <div class="release-progress">
                <div class="progress-header">
                  <span>{{ release.completedCount }}/{{ release.ticketCount }} tickets</span>
                  <span>{{ release.progress }}%</span>
                </div>
                <div class="progress-track">
                  <div class="progress-fill" [style.width.%]="release.progress"></div>
                </div>
              </div>

              <div class="release-footer">
                <span class="readiness-badge" [class]="getReadinessClass(release.progress)">
                  Readiness: {{ release.progress }}%
                </span>
                @if (release.status === 'READY') {
                  <button class="btn-release" (click)="releaseNow(release)">Release Now</button>
                }
              </div>
            </div>
          }
        </div>
      }

      @if (showCreateModal()) {
        <div class="modal-overlay" (click)="showCreateModal.set(false)">
          <div class="modal" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h2>Create Release</h2>
              <button class="btn-close" (click)="showCreateModal.set(false)">&times;</button>
            </div>
            <form (submit)="createRelease($event)">
              <div class="form-group">
                <label>Name</label>
                <input type="text" #nameInput placeholder="e.g., Sprint 12 Release" required />
              </div>
              <div class="form-group">
                <label>Version</label>
                <input type="text" #versionInput placeholder="e.g., 1.2.0" required />
              </div>
              <div class="form-group">
                <label>Description</label>
                <textarea #descInput placeholder="Release description..." rows="3"></textarea>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>Start Date</label>
                  <input type="date" #startDateInput />
                </div>
                <div class="form-group">
                  <label>Release Date</label>
                  <input type="date" #releaseDateInput />
                </div>
              </div>
              <div class="modal-actions">
                <button type="button" class="btn-secondary" (click)="showCreateModal.set(false)">Cancel</button>
                <button type="submit" class="btn-primary">Create</button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .releases-page { max-width: 1200px; }
    .releases-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    h1 { color: #e1e4e8; font-size: 1.5rem; margin: 0; }

    .btn-primary {
      background: #238636; color: white; border: none; padding: 8px 16px;
      border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 600;
    }
    .btn-primary:hover { background: #2ea043; }
    .btn-secondary {
      background: #21262d; color: #c9d1d9; border: 1px solid #30363d; padding: 8px 16px;
      border-radius: 6px; font-size: 0.85rem; cursor: pointer;
    }
    .btn-secondary:hover { background: #30363d; }
    .btn-release {
      background: #1f6feb; color: white; border: none; padding: 6px 12px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer; font-weight: 600;
    }
    .btn-release:hover { background: #388bfd; }
    .btn-close {
      background: none; border: none; color: #8b949e; font-size: 1.5rem; cursor: pointer;
    }

    .loading { color: #8b949e; text-align: center; padding: 60px; }
    .empty-state { text-align: center; padding: 60px; color: #8b949e; }
    .empty-state h3 { color: #e1e4e8; margin: 12px 0 8px; }
    .empty-icon { font-size: 3rem; }

    .releases-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 16px; }

    .release-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px;
      transition: border-color 0.2s;
    }
    .release-card:hover { border-color: #58a6ff; }

    .release-card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
    .release-title-row { display: flex; align-items: center; gap: 8px; }
    h3 { color: #e1e4e8; margin: 0; font-size: 1.05rem; }
    .version-badge { background: #21262d; color: #8b949e; padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; font-family: monospace; }

    .status-badge {
      font-size: 0.7rem; padding: 3px 10px; border-radius: 12px; font-weight: 600; text-transform: uppercase;
    }
    .status-planning { background: #1f6feb33; color: #58a6ff; }
    .status-in_progress { background: #d2992233; color: #d29922; }
    .status-ready { background: #23863633; color: #3fb950; }
    .status-released { background: #8957e533; color: #a371f7; }
    .status-cancelled { background: #8b949e33; color: #8b949e; }

    .release-dates { display: flex; gap: 16px; margin-bottom: 16px; flex-wrap: wrap; }
    .date-item { font-size: 0.8rem; color: #8b949e; }
    .date-label { color: #484f58; }

    .release-progress { margin-bottom: 16px; }
    .progress-header { display: flex; justify-content: space-between; font-size: 0.8rem; color: #8b949e; margin-bottom: 6px; }
    .progress-track { height: 6px; background: #21262d; border-radius: 3px; overflow: hidden; }
    .progress-fill { height: 100%; background: #238636; border-radius: 3px; transition: width 0.3s; }

    .release-footer { display: flex; justify-content: space-between; align-items: center; }
    .readiness-badge { font-size: 0.75rem; padding: 4px 10px; border-radius: 4px; font-weight: 600; }
    .readiness-high { background: #23863633; color: #3fb950; }
    .readiness-medium { background: #d2992233; color: #d29922; }
    .readiness-low { background: #f8514933; color: #f85149; }

    .modal-overlay {
      position: fixed; top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 1000;
    }
    .modal {
      background: #161b22; border: 1px solid #30363d; border-radius: 12px; padding: 24px;
      width: 100%; max-width: 480px;
    }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .modal-header h2 { color: #e1e4e8; margin: 0; font-size: 1.2rem; }
    .form-group { margin-bottom: 16px; flex: 1; }
    .form-group label { display: block; color: #c9d1d9; font-size: 0.85rem; margin-bottom: 6px; }
    .form-group input, .form-group textarea {
      width: 100%; background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      padding: 10px 12px; color: #c9d1d9; font-size: 0.9rem; box-sizing: border-box;
    }
    .form-group input:focus, .form-group textarea:focus { border-color: #58a6ff; outline: none; }
    .form-row { display: flex; gap: 12px; }
    .modal-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 20px; }
  `]
})
export class ReleasesComponent implements OnInit {
  private releaseService = inject(ReleaseService);

  releases = signal<Release[]>([]);
  loading = signal(true);
  showCreateModal = signal(false);

  private projectId = 'demo';

  ngOnInit(): void {
    this.loadReleases();
  }

  loadReleases(): void {
    this.loading.set(true);
    this.releaseService.getByProject(this.projectId).subscribe({
      next: (releases) => {
        this.releases.set(releases);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  formatStatus(status: ReleaseStatus): string {
    return status.replace('_', ' ');
  }

  getReadinessClass(score: number): string {
    if (score >= 80) return 'readiness-badge readiness-high';
    if (score >= 50) return 'readiness-badge readiness-medium';
    return 'readiness-badge readiness-low';
  }

  releaseNow(release: Release): void {
    this.releaseService.release(release.id).subscribe({
      next: () => this.loadReleases()
    });
  }

  createRelease(event: Event): void {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const inputs = form.querySelectorAll('input');
    const name = (inputs[0] as HTMLInputElement).value;
    const version = (inputs[1] as HTMLInputElement).value;
    const description = (form.querySelector('textarea') as HTMLTextAreaElement).value;
    const startDate = (inputs[2] as HTMLInputElement).value;
    const releaseDate = (inputs[3] as HTMLInputElement).value;

    const request: CreateReleaseRequest = { name, version };
    if (description) request.description = description;
    if (startDate) request.startDate = startDate;
    if (releaseDate) request.releaseDate = releaseDate;

    this.releaseService.create(this.projectId, request).subscribe({
      next: () => {
        this.showCreateModal.set(false);
        this.loadReleases();
      }
    });
  }
}
