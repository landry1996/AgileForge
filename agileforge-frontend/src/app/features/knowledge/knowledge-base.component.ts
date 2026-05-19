import { Component, inject, OnInit, signal } from '@angular/core';
import { KnowledgeBaseService } from '../../core/services/knowledge-base.service';
import { KnowledgeEntry, KnowledgeCategory, ProjectContext, CreateKnowledgeEntryRequest } from '../../core/models/knowledge.model';

@Component({
  selector: 'app-knowledge-base',
  standalone: true,
  template: `
    <div class="kb-page">
      <div class="kb-header">
        <h1>Knowledge Base</h1>
        <button class="btn-primary" (click)="showAddForm.set(true)">+ Add Entry</button>
      </div>

      <!-- Project Context Summary -->
      @if (context()) {
        <div class="context-card">
          <div class="context-header">
            <h2>{{ context()!.projectName }}</h2>
          </div>
          @if (context()!.architecture) {
            <p class="context-summary">{{ context()!.architecture }}</p>
          }
          @if (context()!.techStack.length > 0) {
            <div class="tech-stack">
              @for (tech of context()!.techStack; track tech) {
                <span class="tech-tag">{{ tech }}</span>
              }
            </div>
          }
        </div>
      }

      <!-- Category Tabs -->
      <div class="category-tabs">
        @for (cat of allCategories; track cat) {
          <button
            class="tab-btn"
            [class.active]="activeCategory() === cat"
            (click)="switchCategory(cat)">
            {{ formatCategory(cat) }}
          </button>
        }
      </div>

      <!-- Entries List -->
      @if (loading()) {
        <div class="loading">Loading entries...</div>
      } @else if (entries().length === 0) {
        <div class="empty-state">
          <h3>No entries in this category</h3>
          <p>Add knowledge entries to build your project context.</p>
        </div>
      } @else {
        <div class="entries-list">
          @for (entry of entries(); track entry.id) {
            <div class="entry-card" [class.expanded]="expandedEntry() === entry.id">
              <div class="entry-header" (click)="toggleEntry(entry.id)">
                <div class="entry-info">
                  <h3>{{ entry.title }}</h3>
                  <p class="entry-preview">{{ getPreview(entry.content) }}</p>
                </div>
                <span class="expand-icon">{{ expandedEntry() === entry.id ? '&#x25B2;' : '&#x25BC;' }}</span>
              </div>
              @if (expandedEntry() === entry.id) {
                <div class="entry-content">
                  <pre class="entry-text">{{ entry.content }}</pre>
                  <div class="entry-meta">
                    @if (entry.tags) {
                      <div class="entry-tags">
                        @for (tag of splitTags(entry.tags); track tag) {
                          <span class="tag">{{ tag }}</span>
                        }
                      </div>
                    }
                    <span class="entry-date">Updated: {{ entry.updatedAt || entry.createdAt }}</span>
                  </div>
                  <div class="entry-actions">
                    <button class="btn-delete" (click)="deleteEntry(entry.id); $event.stopPropagation()">Delete</button>
                  </div>
                </div>
              }
            </div>
          }
        </div>
      }

      <!-- Add Entry Modal -->
      @if (showAddForm()) {
        <div class="modal-overlay" (click)="showAddForm.set(false)">
          <div class="modal" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h2>Add Knowledge Entry</h2>
              <button class="btn-close" (click)="showAddForm.set(false)">&times;</button>
            </div>
            <form (submit)="addEntry($event)">
              <div class="form-group">
                <label>Title</label>
                <input type="text" class="input-title" placeholder="Entry title" required />
              </div>
              <div class="form-group">
                <label>Category</label>
                <select class="input-category">
                  @for (cat of allCategories; track cat) {
                    <option [value]="cat">{{ formatCategory(cat) }}</option>
                  }
                </select>
              </div>
              <div class="form-group">
                <label>Content</label>
                <textarea class="input-content" placeholder="Knowledge content..." rows="8" required></textarea>
              </div>
              <div class="form-group">
                <label>Tags (comma separated)</label>
                <input type="text" class="input-tags" placeholder="e.g., angular, api, backend" />
              </div>
              <div class="modal-actions">
                <button type="button" class="btn-secondary" (click)="showAddForm.set(false)">Cancel</button>
                <button type="submit" class="btn-primary">Add Entry</button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .kb-page { max-width: 1000px; }
    .kb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    h1 { color: #e1e4e8; font-size: 1.5rem; margin: 0; }
    h2 { color: #e1e4e8; font-size: 1.1rem; margin: 0; }
    h3 { color: #e1e4e8; margin: 0; font-size: 1rem; }

    .btn-primary {
      background: #238636; color: white; border: none; padding: 8px 16px;
      border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 600;
    }
    .btn-primary:hover { background: #2ea043; }
    .btn-secondary {
      background: #21262d; color: #c9d1d9; border: 1px solid #30363d; padding: 8px 16px;
      border-radius: 6px; font-size: 0.85rem; cursor: pointer;
    }
    .btn-delete {
      background: transparent; color: #f85149; border: 1px solid #f85149; padding: 4px 12px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer;
    }
    .btn-delete:hover { background: #f8514922; }
    .btn-close { background: none; border: none; color: #8b949e; font-size: 1.5rem; cursor: pointer; }

    .context-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px; margin-bottom: 20px;
      border-left: 3px solid #58a6ff;
    }
    .context-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
    .context-summary { color: #8b949e; font-size: 0.9rem; margin: 0 0 12px; line-height: 1.5; }
    .tech-stack { display: flex; flex-wrap: wrap; gap: 6px; }
    .tech-tag {
      background: #1f6feb22; color: #58a6ff; padding: 3px 10px; border-radius: 4px;
      font-size: 0.75rem; font-weight: 600;
    }

    .category-tabs {
      display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 20px; padding-bottom: 12px;
      border-bottom: 1px solid #21262d;
    }
    .tab-btn {
      background: transparent; color: #8b949e; border: none; padding: 8px 14px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer; transition: all 0.15s;
    }
    .tab-btn:hover { color: #c9d1d9; background: #21262d; }
    .tab-btn.active { background: #1f6feb; color: white; }

    .loading { color: #8b949e; text-align: center; padding: 40px; }
    .empty-state { text-align: center; padding: 40px; color: #8b949e; }
    .empty-state h3 { color: #e1e4e8; margin-bottom: 8px; }

    .entries-list { display: flex; flex-direction: column; gap: 8px; }
    .entry-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px;
      transition: border-color 0.2s;
    }
    .entry-card:hover { border-color: #484f58; }
    .entry-card.expanded { border-color: #58a6ff; }

    .entry-header {
      padding: 16px 20px; cursor: pointer; display: flex; justify-content: space-between; align-items: flex-start;
    }
    .entry-info { flex: 1; min-width: 0; }
    .entry-preview { color: #8b949e; font-size: 0.85rem; margin: 4px 0 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .expand-icon { color: #8b949e; font-size: 0.7rem; margin-left: 12px; flex-shrink: 0; }

    .entry-content { padding: 0 20px 16px; border-top: 1px solid #21262d; margin-top: 0; padding-top: 16px; }
    .entry-text {
      white-space: pre-wrap; word-wrap: break-word; color: #c9d1d9; font-size: 0.9rem;
      line-height: 1.6; margin: 0 0 12px; font-family: inherit;
    }
    .entry-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .entry-tags { display: flex; flex-wrap: wrap; gap: 4px; }
    .tag { background: #21262d; color: #8b949e; padding: 2px 8px; border-radius: 4px; font-size: 0.7rem; }
    .entry-date { color: #484f58; font-size: 0.75rem; }
    .entry-actions { display: flex; justify-content: flex-end; }

    .modal-overlay {
      position: fixed; top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 1000;
    }
    .modal {
      background: #161b22; border: 1px solid #30363d; border-radius: 12px; padding: 24px;
      width: 100%; max-width: 560px; max-height: 90vh; overflow-y: auto;
    }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .form-group { margin-bottom: 16px; }
    .form-group label { display: block; color: #c9d1d9; font-size: 0.85rem; margin-bottom: 6px; font-weight: 600; }
    .form-group input, .form-group textarea, .form-group select {
      width: 100%; background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      padding: 10px 12px; color: #c9d1d9; font-size: 0.9rem; box-sizing: border-box;
    }
    .form-group input:focus, .form-group textarea:focus, .form-group select:focus { border-color: #58a6ff; outline: none; }
    .modal-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 20px; }
  `]
})
export class KnowledgeBaseComponent implements OnInit {
  private kbService = inject(KnowledgeBaseService);

  entries = signal<KnowledgeEntry[]>([]);
  context = signal<ProjectContext | null>(null);
  loading = signal(true);
  activeCategory = signal<KnowledgeCategory>('TECH_STACK');
  expandedEntry = signal<string | null>(null);
  showAddForm = signal(false);

  private projectId = 'demo';

  allCategories: KnowledgeCategory[] = [
    'TECH_STACK', 'ARCHITECTURE', 'CONVENTIONS', 'DECISIONS',
    'API_ENDPOINTS', 'BUSINESS_RULES', 'CONSTRAINTS', 'PATTERNS',
    'KNOWN_ISSUES', 'DEPENDENCIES', 'TEAM_PREFERENCES'
  ];

  ngOnInit(): void {
    this.loadContext();
    this.loadEntries();
  }

  loadContext(): void {
    this.kbService.getProjectContext(this.projectId).subscribe({
      next: (ctx) => this.context.set(ctx),
      error: () => {}
    });
  }

  loadEntries(): void {
    this.loading.set(true);
    this.kbService.getByCategory(this.projectId, this.activeCategory()).subscribe({
      next: (entries) => {
        this.entries.set(entries);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  switchCategory(category: KnowledgeCategory): void {
    this.activeCategory.set(category);
    this.expandedEntry.set(null);
    this.loadEntries();
  }

  toggleEntry(id: string): void {
    this.expandedEntry.set(this.expandedEntry() === id ? null : id);
  }

  formatCategory(category: string): string {
    return category.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  }

  getPreview(content: string): string {
    return content.length > 120 ? content.substring(0, 120) + '...' : content;
  }

  splitTags(tags: string): string[] {
    return tags.split(',').map(t => t.trim()).filter(t => t.length > 0);
  }

  deleteEntry(id: string): void {
    if (confirm('Delete this knowledge entry?')) {
      this.kbService.delete(id).subscribe({
        next: () => this.loadEntries()
      });
    }
  }

  addEntry(event: Event): void {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const title = (form.querySelector('.input-title') as HTMLInputElement).value;
    const category = (form.querySelector('.input-category') as HTMLSelectElement).value as KnowledgeCategory;
    const content = (form.querySelector('.input-content') as HTMLTextAreaElement).value;
    const tagsStr = (form.querySelector('.input-tags') as HTMLInputElement).value;

    const request: CreateKnowledgeEntryRequest = { title, content, category };
    if (tagsStr) {
      request.tags = tagsStr;
    }

    this.kbService.create(this.projectId, request).subscribe({
      next: () => {
        this.showAddForm.set(false);
        if (this.activeCategory() === category) {
          this.loadEntries();
        } else {
          this.switchCategory(category);
        }
        this.loadContext();
      }
    });
  }
}
