import { Component, inject, OnInit, signal } from '@angular/core';
import { DocumentService } from '../../core/services/document.service';
import { Document, DocumentTree, DocumentType, DocumentStatus, CreateDocumentRequest, UpdateDocumentRequest } from '../../core/models/document.model';

@Component({
  selector: 'app-documents',
  standalone: true,
  template: `
    <div class="documents-page">
      <div class="documents-header">
        <h1>Documents</h1>
        <button class="btn-primary" (click)="startCreate()">+ New Document</button>
      </div>

      @if (loading()) {
        <div class="loading">Loading documents...</div>
      } @else {
        <div class="documents-layout">
          <!-- Sidebar with document tree -->
          <aside class="doc-sidebar">
            <div class="sidebar-header">
              <span>Document Tree</span>
            </div>
            @if (tree().length === 0) {
              <p class="sidebar-empty">No documents yet</p>
            } @else {
              <div class="tree-list">
                @for (node of tree(); track node.id) {
                  <div class="tree-item" [class.active]="selectedDoc()?.id === node.id" (click)="selectDocument(node.id)">
                    <span class="tree-icon">{{ getTypeIcon(node.docType) }}</span>
                    <span class="tree-title">{{ node.title }}</span>
                  </div>
                  @if (node.children && node.children.length > 0) {
                    @for (child of node.children; track child.id) {
                      <div class="tree-item tree-child" [class.active]="selectedDoc()?.id === child.id" (click)="selectDocument(child.id)">
                        <span class="tree-icon">{{ getTypeIcon(child.docType) }}</span>
                        <span class="tree-title">{{ child.title }}</span>
                      </div>
                    }
                  }
                }
              </div>
            }
          </aside>

          <!-- Main content area -->
          <main class="doc-main">
            @if (editing()) {
              <!-- Edit mode -->
              <div class="editor-container">
                <div class="editor-toolbar">
                  <input type="text" class="title-input" [value]="editTitle()" (input)="editTitle.set(asInputValue($event))" placeholder="Document title" />
                  <div class="toolbar-actions">
                    <select class="type-select" [value]="editType()" (change)="editType.set(asSelectValue($event))">
                      <option value="PAGE">Page</option>
                      <option value="API_DOC">API Doc</option>
                      <option value="ADR">ADR</option>
                      <option value="SPEC">Spec</option>
                      <option value="MEETING_NOTES">Meeting Notes</option>
                      <option value="RUNBOOK">Runbook</option>
                      <option value="ONBOARDING">Onboarding</option>
                      <option value="FAQ">FAQ</option>
                      <option value="RELEASE_NOTES">Release Notes</option>
                      <option value="POST_MORTEM">Post Mortem</option>
                    </select>
                    <button class="btn-save" (click)="saveDocument()">Save</button>
                    <button class="btn-cancel" (click)="cancelEdit()">Cancel</button>
                  </div>
                </div>
                <textarea
                  class="editor-textarea"
                  [value]="editContent()"
                  (input)="editContent.set(asTextareaValue($event))"
                  placeholder="Write your document content here... (Markdown supported)"
                ></textarea>
              </div>
            } @else if (selectedDoc()) {
              <!-- View mode -->
              <div class="doc-viewer">
                <div class="doc-viewer-header">
                  <h2>{{ selectedDoc()!.title }}</h2>
                  <div class="doc-actions">
                    <button class="btn-edit" (click)="startEdit()">Edit</button>
                    <button class="btn-danger" (click)="deleteDocument()">Delete</button>
                  </div>
                </div>
                <div class="doc-meta">
                  <span class="meta-item">
                    <span class="meta-label">Type:</span> {{ selectedDoc()!.docType }}
                  </span>
                  <span class="meta-item">
                    <span class="meta-label">Status:</span>
                    <span class="status-badge status-{{ selectedDoc()!.status.toLowerCase() }}">{{ selectedDoc()!.status }}</span>
                  </span>
                  <span class="meta-item">
                    <span class="meta-label">Version:</span> {{ selectedDoc()!.version }}
                  </span>
                  <span class="meta-item">
                    <span class="meta-label">Updated:</span> {{ selectedDoc()!.updatedAt || selectedDoc()!.createdAt }}
                  </span>
                </div>
                <div class="doc-content">
                  <pre class="content-rendered">{{ selectedDoc()!.content }}</pre>
                </div>
              </div>
            } @else {
              <div class="doc-placeholder">
                <h3>Select a document from the sidebar</h3>
                <p>Or create a new document to get started.</p>
              </div>
            }
          </main>
        </div>
      }
    </div>
  `,
  styles: [`
    .documents-page { max-width: 1400px; height: calc(100vh - 100px); display: flex; flex-direction: column; }
    .documents-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    h1 { color: #e1e4e8; font-size: 1.5rem; margin: 0; }

    .btn-primary {
      background: #238636; color: white; border: none; padding: 8px 16px;
      border-radius: 6px; font-size: 0.85rem; cursor: pointer; font-weight: 600;
    }
    .btn-primary:hover { background: #2ea043; }
    .btn-edit {
      background: #1f6feb; color: white; border: none; padding: 6px 14px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer;
    }
    .btn-danger {
      background: transparent; color: #f85149; border: 1px solid #f85149; padding: 6px 14px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer;
    }
    .btn-danger:hover { background: #f8514922; }
    .btn-save {
      background: #238636; color: white; border: none; padding: 6px 14px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer; font-weight: 600;
    }
    .btn-cancel {
      background: #21262d; color: #c9d1d9; border: 1px solid #30363d; padding: 6px 14px;
      border-radius: 6px; font-size: 0.8rem; cursor: pointer;
    }

    .loading { color: #8b949e; text-align: center; padding: 60px; }

    .documents-layout { display: flex; gap: 0; flex: 1; min-height: 0; border: 1px solid #30363d; border-radius: 8px; overflow: hidden; }

    .doc-sidebar {
      width: 260px; min-width: 260px; background: #161b22; border-right: 1px solid #30363d;
      overflow-y: auto; display: flex; flex-direction: column;
    }
    .sidebar-header {
      padding: 12px 16px; font-size: 0.85rem; font-weight: 600; color: #e1e4e8;
      border-bottom: 1px solid #21262d;
    }
    .sidebar-empty { color: #484f58; font-size: 0.85rem; padding: 16px; }
    .tree-list { padding: 8px 0; }
    .tree-item {
      display: flex; align-items: center; gap: 8px; padding: 8px 16px;
      cursor: pointer; font-size: 0.85rem; color: #c9d1d9; transition: background 0.15s;
    }
    .tree-item:hover { background: #21262d; }
    .tree-item.active { background: #1f6feb22; color: #58a6ff; border-right: 2px solid #58a6ff; }
    .tree-child { padding-left: 32px; }
    .tree-icon { font-size: 0.9rem; }
    .tree-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

    .doc-main { flex: 1; background: #0d1117; overflow-y: auto; display: flex; flex-direction: column; }

    .doc-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #8b949e; }
    .doc-placeholder h3 { color: #e1e4e8; margin-bottom: 8px; }

    .doc-viewer { padding: 24px; }
    .doc-viewer-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
    .doc-viewer-header h2 { color: #e1e4e8; margin: 0; font-size: 1.4rem; }
    .doc-actions { display: flex; gap: 8px; }
    .doc-meta { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #21262d; }
    .meta-item { font-size: 0.8rem; color: #8b949e; }
    .meta-label { color: #484f58; }
    .status-badge { padding: 2px 8px; border-radius: 8px; font-size: 0.7rem; font-weight: 600; }
    .status-draft { background: #d2992233; color: #d29922; }
    .status-published { background: #23863633; color: #3fb950; }
    .status-archived { background: #8b949e33; color: #8b949e; }
    .doc-content { flex: 1; }
    .content-rendered {
      white-space: pre-wrap; word-wrap: break-word; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
      color: #c9d1d9; line-height: 1.7; font-size: 0.9rem; margin: 0;
    }

    .editor-container { flex: 1; display: flex; flex-direction: column; }
    .editor-toolbar {
      display: flex; align-items: center; gap: 12px; padding: 12px 16px;
      border-bottom: 1px solid #21262d; background: #161b22; flex-wrap: wrap;
    }
    .title-input {
      flex: 1; min-width: 200px; background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      padding: 8px 12px; color: #e1e4e8; font-size: 1rem; font-weight: 600;
    }
    .title-input:focus { border-color: #58a6ff; outline: none; }
    .type-select {
      background: #0d1117; border: 1px solid #30363d; border-radius: 6px;
      padding: 6px 10px; color: #c9d1d9; font-size: 0.8rem;
    }
    .toolbar-actions { display: flex; gap: 8px; align-items: center; }
    .editor-textarea {
      flex: 1; background: #0d1117; border: none; padding: 24px;
      color: #c9d1d9; font-size: 0.9rem; line-height: 1.7; resize: none;
      font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Fira Code', monospace;
    }
    .editor-textarea:focus { outline: none; }
  `]
})
export class DocumentsComponent implements OnInit {
  private documentService = inject(DocumentService);

  tree = signal<DocumentTree[]>([]);
  selectedDoc = signal<Document | null>(null);
  loading = signal(true);
  editing = signal(false);
  editTitle = signal('');
  editContent = signal('');
  editType = signal<string>('PAGE');
  isCreating = signal(false);

  private projectId = 'demo';

  ngOnInit(): void {
    this.loadTree();
  }

  loadTree(): void {
    this.loading.set(true);
    this.documentService.getTree(this.projectId).subscribe({
      next: (tree) => {
        this.tree.set(tree);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  selectDocument(id: string): void {
    this.editing.set(false);
    this.documentService.getById(id).subscribe({
      next: (doc) => this.selectedDoc.set(doc)
    });
  }

  startCreate(): void {
    this.isCreating.set(true);
    this.editing.set(true);
    this.editTitle.set('');
    this.editContent.set('');
    this.editType.set('PAGE');
    this.selectedDoc.set(null);
  }

  startEdit(): void {
    const doc = this.selectedDoc();
    if (!doc) return;
    this.isCreating.set(false);
    this.editing.set(true);
    this.editTitle.set(doc.title);
    this.editContent.set(doc.content || '');
    this.editType.set(doc.docType);
  }

  cancelEdit(): void {
    this.editing.set(false);
    this.isCreating.set(false);
  }

  saveDocument(): void {
    if (this.isCreating()) {
      const request: CreateDocumentRequest = {
        title: this.editTitle(),
        content: this.editContent(),
        docType: this.editType() as DocumentType
      };
      this.documentService.create(this.projectId, request).subscribe({
        next: (doc) => {
          this.selectedDoc.set(doc);
          this.editing.set(false);
          this.isCreating.set(false);
          this.loadTree();
        }
      });
    } else {
      const doc = this.selectedDoc();
      if (!doc) return;
      const request: UpdateDocumentRequest = {
        title: this.editTitle(),
        content: this.editContent()
      };
      this.documentService.update(doc.id, request).subscribe({
        next: (updated) => {
          this.selectedDoc.set(updated);
          this.editing.set(false);
          this.loadTree();
        }
      });
    }
  }

  deleteDocument(): void {
    const doc = this.selectedDoc();
    if (!doc) return;
    if (confirm('Are you sure you want to delete this document?')) {
      this.documentService.delete(doc.id).subscribe({
        next: () => {
          this.selectedDoc.set(null);
          this.loadTree();
        }
      });
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'PAGE': return '\u{1F4C4}';
      case 'API_DOC': return '\u{1F310}';
      case 'ADR': return '\u{1F4CB}';
      case 'SPEC': return '\u{1F4D0}';
      case 'MEETING_NOTES': return '\u{1F4DD}';
      case 'RUNBOOK': return '\u{1F6E0}';
      case 'ONBOARDING': return '\u{1F44B}';
      case 'FAQ': return '\u{2753}';
      case 'RELEASE_NOTES': return '\u{1F680}';
      case 'POST_MORTEM': return '\u{1F50D}';
      default: return '\u{1F4C4}';
    }
  }

  asInputValue(event: Event): string {
    return (event.target as HTMLInputElement).value;
  }

  asTextareaValue(event: Event): string {
    return (event.target as HTMLTextAreaElement).value;
  }

  asSelectValue(event: Event): string {
    return (event.target as HTMLSelectElement).value;
  }
}
