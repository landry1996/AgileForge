import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { ReleaseService } from '../../core/services/release.service';
import { RoadmapItem, CreateRoadmapItemRequest } from '../../core/models/release.model';

interface MonthColumn {
  label: string;
  year: number;
  month: number;
}

@Component({
  selector: 'app-roadmap',
  standalone: true,
  template: `
    <div class="roadmap-page">
      <div class="roadmap-header">
        <h1>Roadmap</h1>
        <div class="header-actions">
          <button class="btn-nav" (click)="navigateMonths(-3)">&larr; Prev</button>
          <span class="date-range">{{ months()[0]?.label }} - {{ months()[months().length - 1]?.label }}</span>
          <button class="btn-nav" (click)="navigateMonths(3)">Next &rarr;</button>
          <button class="btn-primary" (click)="showAddForm.set(true)">+ Add Item</button>
        </div>
      </div>

      @if (loading()) {
        <div class="loading">Loading roadmap...</div>
      } @else {
        <div class="timeline-container">
          <div class="timeline-header">
            <div class="item-label-col">Items</div>
            @for (month of months(); track month.label) {
              <div class="month-col">{{ month.label }}</div>
            }
          </div>

          @if (items().length === 0) {
            <div class="empty-state">
              <p>No roadmap items. Add items to visualize your project timeline.</p>
            </div>
          } @else {
            <div class="timeline-body">
              @for (item of items(); track item.id) {
                <div class="timeline-row">
                  <div class="item-label-col">
                    <div class="item-title">{{ item.title }}</div>
                    <div class="item-meta">
                      <span class="item-status status-{{ item.status.toLowerCase() }}">{{ formatStatus(item.status) }}</span>
                      @if (item.category) {
                        <span class="item-category">{{ item.category }}</span>
                      }
                    </div>
                  </div>
                  @for (month of months(); track month.label) {
                    <div class="month-col">
                      @if (isItemInMonth(item, month)) {
                        <div class="roadmap-bar"
                             [style.background]="item.color || getStatusColor(item.status)"
                             [class.bar-start]="isStartMonth(item, month)"
                             [class.bar-end]="isEndMonth(item, month)">
                        </div>
                      }
                    </div>
                  }
                </div>
              }
            </div>
          }
        </div>
      }

      @if (showAddForm()) {
        <div class="modal-overlay" (click)="showAddForm.set(false)">
          <div class="modal" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h2>Add Roadmap Item</h2>
              <button class="btn-close" (click)="showAddForm.set(false)">&times;</button>
            </div>
            <form (submit)="addItem($event)">
              <div class="form-group">
                <label>Title</label>
                <input type="text" class="input-title" placeholder="Feature or milestone name" required />
              </div>
              <div class="form-group">
                <label>Description</label>
                <textarea class="input-desc" placeholder="Description..." rows="2"></textarea>
              </div>
              <div class="form-group">
                <label>Category</label>
                <input type="text" class="input-category" placeholder="e.g., Feature, Infrastructure, UX" />
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>Start Date</label>
                  <input type="date" class="input-start" required />
                </div>
                <div class="form-group">
                  <label>End Date</label>
                  <input type="date" class="input-end" required />
                </div>
              </div>
              <div class="form-group">
                <label>Color</label>
                <input type="color" class="input-color" value="#58a6ff" />
              </div>
              <div class="modal-actions">
                <button type="button" class="btn-secondary" (click)="showAddForm.set(false)">Cancel</button>
                <button type="submit" class="btn-primary">Add Item</button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .roadmap-page { max-width: 1400px; overflow-x: auto; }
    .roadmap-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
    h1 { color: #e1e4e8; font-size: 1.5rem; margin: 0; }
    .header-actions { display: flex; align-items: center; gap: 12px; }
    .date-range { color: #8b949e; font-size: 0.9rem; min-width: 160px; text-align: center; }

    .btn-primary {
      background: #238636; color: white; border: none; padding: 8px 16px;
      border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 600;
    }
    .btn-primary:hover { background: #2ea043; }
    .btn-secondary {
      background: #21262d; color: #c9d1d9; border: 1px solid #30363d; padding: 8px 16px;
      border-radius: 6px; font-size: 0.85rem; cursor: pointer;
    }
    .btn-nav {
      background: #21262d; color: #c9d1d9; border: 1px solid #30363d; padding: 6px 12px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer;
    }
    .btn-nav:hover { background: #30363d; }
    .btn-close { background: none; border: none; color: #8b949e; font-size: 1.5rem; cursor: pointer; }

    .loading { color: #8b949e; text-align: center; padding: 60px; }
    .empty-state { text-align: center; padding: 40px; color: #8b949e; }

    .timeline-container {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; overflow-x: auto;
    }
    .timeline-header {
      display: grid; grid-template-columns: 240px repeat(6, 1fr);
      border-bottom: 1px solid #30363d; position: sticky; top: 0; background: #161b22; z-index: 1;
    }
    .timeline-body { display: flex; flex-direction: column; }
    .timeline-row {
      display: grid; grid-template-columns: 240px repeat(6, 1fr);
      border-bottom: 1px solid #21262d; min-height: 60px; align-items: center;
    }
    .timeline-row:last-child { border-bottom: none; }

    .item-label-col {
      padding: 12px 16px; font-size: 0.85rem; color: #c9d1d9; font-weight: 600;
      border-right: 1px solid #21262d;
    }
    .item-title { color: #e1e4e8; margin-bottom: 4px; }
    .item-meta { display: flex; gap: 8px; align-items: center; }
    .item-status { font-size: 0.65rem; padding: 2px 6px; border-radius: 8px; font-weight: 600; }
    .status-planned { background: #1f6feb33; color: #58a6ff; }
    .status-in_progress { background: #d2992233; color: #d29922; }
    .status-completed { background: #23863633; color: #3fb950; }
    .status-cancelled { background: #8b949e33; color: #8b949e; }
    .item-category { font-size: 0.65rem; color: #a371f7; }

    .month-col {
      padding: 12px 8px; text-align: center; font-size: 0.8rem; color: #8b949e;
      position: relative; border-right: 1px solid #21262d;
    }
    .month-col:last-child { border-right: none; }

    .roadmap-bar {
      height: 8px; border-radius: 0; margin: 0 -8px;
    }
    .roadmap-bar.bar-start { border-radius: 4px 0 0 4px; margin-left: 4px; }
    .roadmap-bar.bar-end { border-radius: 0 4px 4px 0; margin-right: 4px; }
    .roadmap-bar.bar-start.bar-end { border-radius: 4px; margin: 0 4px; }

    .modal-overlay {
      position: fixed; top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 1000;
    }
    .modal {
      background: #161b22; border: 1px solid #30363d; border-radius: 12px; padding: 24px;
      width: 100%; max-width: 500px;
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
export class RoadmapComponent implements OnInit {
  private releaseService = inject(ReleaseService);

  items = signal<RoadmapItem[]>([]);
  loading = signal(true);
  showAddForm = signal(false);
  startMonth = signal(new Date());

  private projectId = 'demo';

  months = computed<MonthColumn[]>(() => {
    const start = this.startMonth();
    const result: MonthColumn[] = [];
    for (let i = 0; i < 6; i++) {
      const d = new Date(start.getFullYear(), start.getMonth() + i, 1);
      result.push({
        label: d.toLocaleDateString('en-US', { month: 'short', year: '2-digit' }),
        year: d.getFullYear(),
        month: d.getMonth()
      });
    }
    return result;
  });

  ngOnInit(): void {
    this.loadItems();
  }

  loadItems(): void {
    this.loading.set(true);
    this.releaseService.getRoadmap(this.projectId).subscribe({
      next: (items) => {
        this.items.set(items);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  navigateMonths(offset: number): void {
    const current = this.startMonth();
    this.startMonth.set(new Date(current.getFullYear(), current.getMonth() + offset, 1));
  }

  formatStatus(status: string): string {
    return status.replace('_', ' ');
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PLANNED': return '#58a6ff';
      case 'IN_PROGRESS': return '#d29922';
      case 'COMPLETED': return '#3fb950';
      case 'CANCELLED': return '#8b949e';
      default: return '#58a6ff';
    }
  }

  isItemInMonth(item: RoadmapItem, month: MonthColumn): boolean {
    if (!item.startDate || !item.endDate) return false;
    const itemStart = new Date(item.startDate);
    const itemEnd = new Date(item.endDate);
    const monthStart = new Date(month.year, month.month, 1);
    const monthEnd = new Date(month.year, month.month + 1, 0);
    return itemStart <= monthEnd && itemEnd >= monthStart;
  }

  isStartMonth(item: RoadmapItem, month: MonthColumn): boolean {
    if (!item.startDate) return false;
    const itemStart = new Date(item.startDate);
    return itemStart.getFullYear() === month.year && itemStart.getMonth() === month.month;
  }

  isEndMonth(item: RoadmapItem, month: MonthColumn): boolean {
    if (!item.endDate) return false;
    const itemEnd = new Date(item.endDate);
    return itemEnd.getFullYear() === month.year && itemEnd.getMonth() === month.month;
  }

  addItem(event: Event): void {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const title = (form.querySelector('.input-title') as HTMLInputElement).value;
    const description = (form.querySelector('.input-desc') as HTMLTextAreaElement).value;
    const category = (form.querySelector('.input-category') as HTMLInputElement).value;
    const startDate = (form.querySelector('.input-start') as HTMLInputElement).value;
    const endDate = (form.querySelector('.input-end') as HTMLInputElement).value;
    const color = (form.querySelector('.input-color') as HTMLInputElement).value;

    const request: CreateRoadmapItemRequest = { title };
    if (description) request.description = description;
    if (category) request.category = category;
    if (startDate) request.startDate = startDate;
    if (endDate) request.endDate = endDate;
    if (color) request.color = color;

    this.releaseService.createRoadmapItem(this.projectId, request).subscribe({
      next: () => {
        this.showAddForm.set(false);
        this.loadItems();
      }
    });
  }
}
