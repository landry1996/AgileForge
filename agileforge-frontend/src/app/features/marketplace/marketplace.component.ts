import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TemplateService } from '../../core/services/template.service';
import { IndustryTemplate, MarketplaceExtension } from '../../core/models/ecosystem.model';

@Component({
  selector: 'app-marketplace',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="marketplace-container">
      <header class="page-header">
        <h1>Marketplace & Templates</h1>
        <p class="subtitle">Industry templates, extensions, and community plugins</p>
      </header>

      <div class="tabs">
        <button [class.active]="activeTab() === 'templates'" (click)="activeTab.set('templates')">Industry Templates</button>
        <button [class.active]="activeTab() === 'extensions'" (click)="activeTab.set('extensions')">Extensions</button>
        <button [class.active]="activeTab() === 'installed'" (click)="activeTab.set('installed')">Installed</button>
      </div>

      @if (activeTab() === 'templates') {
        <div class="industry-filter">
          <button [class.active]="selectedIndustry() === ''" (click)="filterByIndustry('')">All</button>
          <button [class.active]="selectedIndustry() === 'SOFTWARE'" (click)="filterByIndustry('SOFTWARE')">Software</button>
          <button [class.active]="selectedIndustry() === 'MARKETING'" (click)="filterByIndustry('MARKETING')">Marketing</button>
          <button [class.active]="selectedIndustry() === 'DEVOPS'" (click)="filterByIndustry('DEVOPS')">DevOps</button>
          <button [class.active]="selectedIndustry() === 'HR'" (click)="filterByIndustry('HR')">HR</button>
        </div>

        <div class="templates-grid">
          @for (template of templates(); track template.id) {
            <div class="template-card">
              <div class="template-header">
                <span class="template-icon">{{ getTemplateIcon(template.icon) }}</span>
                @if (template.official) {
                  <span class="official-badge">Official</span>
                }
              </div>
              <h3>{{ template.name }}</h3>
              <p>{{ template.description }}</p>
              <div class="template-meta">
                <span class="template-industry">{{ template.industry }}</span>
                <span class="template-category">{{ template.category }}</span>
              </div>
              <button class="btn-apply" (click)="applyTemplate(template.id)">Apply to Project</button>
            </div>
          }
          @empty {
            <p class="empty-state">No templates available for this filter.</p>
          }
        </div>
      }

      @if (activeTab() === 'extensions') {
        <div class="category-filter">
          <button [class.active]="selectedCategory() === ''" (click)="filterByCategory('')">All</button>
          <button [class.active]="selectedCategory() === 'INTEGRATION'" (click)="filterByCategory('INTEGRATION')">Integrations</button>
          <button [class.active]="selectedCategory() === 'WIDGET'" (click)="filterByCategory('WIDGET')">Widgets</button>
          <button [class.active]="selectedCategory() === 'AUTOMATION'" (click)="filterByCategory('AUTOMATION')">Automation</button>
          <button [class.active]="selectedCategory() === 'THEME'" (click)="filterByCategory('THEME')">Themes</button>
        </div>

        <div class="extensions-grid">
          @for (ext of extensions(); track ext.id) {
            <div class="extension-card">
              <div class="ext-header">
                <div class="ext-icon">{{ ext.iconUrl || '🧩' }}</div>
                <div class="ext-info">
                  <h4>{{ ext.name }}</h4>
                  <span class="ext-version">v{{ ext.version }}</span>
                </div>
              </div>
              <p>{{ ext.description }}</p>
              <div class="ext-stats">
                <span>⬇️ {{ ext.downloads }}</span>
                <span>⭐ {{ ext.rating.toFixed(1) }} ({{ ext.ratingCount }})</span>
              </div>
              <button class="btn-install" (click)="installExtension(ext.id)">Install</button>
            </div>
          }
          @empty {
            <p class="empty-state">No extensions available yet.</p>
          }
        </div>
      }

      @if (activeTab() === 'installed') {
        <div class="installed-list">
          @for (ext of installedExtensions(); track ext.id) {
            <div class="installed-row">
              <div class="installed-info">
                <h4>{{ ext.name }}</h4>
                <span>v{{ ext.version }} - {{ ext.category }}</span>
              </div>
              <button class="btn-uninstall" (click)="uninstallExtension(ext.id)">Uninstall</button>
            </div>
          }
          @empty {
            <p class="empty-state">No extensions installed yet. Browse the marketplace to get started.</p>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .marketplace-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
    .page-header h1 { margin: 0; font-size: 24px; }
    .subtitle { color: #6b7280; margin: 4px 0 24px; }
    .tabs { display: flex; gap: 8px; margin-bottom: 24px; border-bottom: 1px solid #e5e7eb; padding-bottom: 8px; }
    .tabs button { padding: 8px 16px; border: none; background: none; cursor: pointer; border-radius: 6px; font-weight: 500; }
    .tabs button.active { background: #4f46e5; color: white; }
    .industry-filter, .category-filter { display: flex; gap: 8px; margin-bottom: 20px; }
    .industry-filter button, .category-filter button { padding: 6px 14px; border: 1px solid #d1d5db; background: white; border-radius: 20px; cursor: pointer; font-size: 13px; }
    .industry-filter button.active, .category-filter button.active { background: #4f46e5; color: white; border-color: #4f46e5; }
    .templates-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
    .template-card { padding: 24px; border: 2px solid #e5e7eb; border-radius: 12px; }
    .template-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .template-icon { font-size: 28px; }
    .official-badge { font-size: 11px; padding: 3px 8px; background: #dbeafe; color: #2563eb; border-radius: 4px; }
    .template-card h3 { margin: 0 0 8px; font-size: 16px; }
    .template-card p { font-size: 13px; color: #6b7280; margin: 0 0 12px; }
    .template-meta { display: flex; gap: 8px; margin-bottom: 16px; }
    .template-meta span { font-size: 11px; padding: 3px 8px; background: #f3f4f6; border-radius: 4px; }
    .btn-apply { width: 100%; padding: 10px; background: #4f46e5; color: white; border: none; border-radius: 8px; cursor: pointer; }
    .extensions-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
    .extension-card { padding: 20px; border: 1px solid #e5e7eb; border-radius: 12px; }
    .ext-header { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; }
    .ext-icon { font-size: 32px; }
    .ext-info h4 { margin: 0; }
    .ext-version { font-size: 11px; color: #9ca3af; }
    .extension-card p { font-size: 13px; color: #6b7280; margin: 0 0 12px; }
    .ext-stats { display: flex; gap: 16px; font-size: 12px; color: #6b7280; margin-bottom: 12px; }
    .btn-install { width: 100%; padding: 8px; background: #10b981; color: white; border: none; border-radius: 6px; cursor: pointer; }
    .installed-row { display: flex; justify-content: space-between; align-items: center; padding: 16px; background: #f9fafb; border-radius: 8px; margin-bottom: 8px; }
    .installed-info h4 { margin: 0; }
    .installed-info span { font-size: 12px; color: #6b7280; }
    .btn-uninstall { padding: 6px 14px; background: white; border: 1px solid #fecaca; color: #dc2626; border-radius: 6px; cursor: pointer; }
    .empty-state { text-align: center; color: #9ca3af; padding: 32px; }
  `]
})
export class MarketplaceComponent implements OnInit {
  private templateService = inject(TemplateService);

  activeTab = signal<'templates' | 'extensions' | 'installed'>('templates');
  templates = signal<IndustryTemplate[]>([]);
  extensions = signal<MarketplaceExtension[]>([]);
  installedExtensions = signal<MarketplaceExtension[]>([]);
  selectedIndustry = signal('');
  selectedCategory = signal('');

  ngOnInit() {
    this.templateService.getAll().subscribe(t => this.templates.set(t));
    this.templateService.getMarketplace().subscribe(e => this.extensions.set(e));
    this.templateService.getInstalled('current-org').subscribe(i => this.installedExtensions.set(i));
  }

  filterByIndustry(industry: string) {
    this.selectedIndustry.set(industry);
    if (industry) {
      this.templateService.getByIndustry(industry).subscribe(t => this.templates.set(t));
    } else {
      this.templateService.getAll().subscribe(t => this.templates.set(t));
    }
  }

  filterByCategory(category: string) {
    this.selectedCategory.set(category);
    this.templateService.getMarketplace(category || undefined).subscribe(e => this.extensions.set(e));
  }

  applyTemplate(templateId: string) {
    this.templateService.applyTemplate(templateId, 'current-project').subscribe();
  }

  installExtension(extensionId: string) {
    this.templateService.installExtension(extensionId, 'current-org').subscribe();
  }

  uninstallExtension(extensionId: string) {
    this.templateService.uninstallExtension(extensionId, 'current-org').subscribe(() => {
      this.installedExtensions.update(list => list.filter(e => e.id !== extensionId));
    });
  }

  getTemplateIcon(icon: string): string {
    const map: Record<string, string> = {
      'code': '💻', 'columns': '📊', 'megaphone': '📢', 'server': '🖥️', 'users': '👥'
    };
    return map[icon] || '📋';
  }
}
