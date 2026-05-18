import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../core/services/notification.service';
import { SearchService } from '../core/services/search.service';
import { AuthService } from '../core/services/auth.service';
import { Notification } from '../core/models/notification.model';
import { Ticket } from '../core/models/ticket.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [FormsModule],
  template: `
    <header class="app-header">
      <div class="header-search">
        <input
          type="text"
          class="search-input"
          placeholder="Search tickets... (Ctrl+K)"
          [(ngModel)]="searchQuery"
          (input)="onSearchInput()"
          (focus)="showSearchResults = true"
          (keydown.escape)="showSearchResults = false"
          (keydown.enter)="performSearch()"
        />
        @if (showSearchResults && searchResults().length > 0) {
          <div class="search-dropdown">
            @for (ticket of searchResults(); track ticket.id) {
              <a class="search-result-item" (click)="navigateToTicket(ticket)">
                <span class="result-key">{{ ticket.fullKey }}</span>
                <span class="result-title">{{ ticket.title }}</span>
                <span class="result-status" [attr.data-status]="ticket.status">{{ ticket.status }}</span>
              </a>
            }
            @if (searchResults().length >= 5) {
              <div class="search-more" (click)="navigateToSearch()">
                View all results
              </div>
            }
          </div>
        }
      </div>

      <div class="header-actions">
        <div class="notification-bell" (click)="toggleNotifications()">
          <span class="bell-icon">&#128276;</span>
          @if (notificationService.unreadBadgeCount() > 0) {
            <span class="badge">{{ notificationService.unreadBadgeCount() }}</span>
          }
        </div>

        @if (showNotifications) {
          <div class="notification-dropdown">
            <div class="notification-header">
              <h4>Notifications</h4>
              @if (notificationService.unreadBadgeCount() > 0) {
                <button class="mark-all-btn" (click)="markAllRead()">Mark all read</button>
              }
            </div>
            <div class="notification-list">
              @for (notification of notifications(); track notification.id) {
                <div class="notification-item" [class.unread]="!notification.read"
                     (click)="handleNotificationClick(notification)">
                  <div class="notification-icon">{{ getNotificationIcon(notification.type) }}</div>
                  <div class="notification-content">
                    <div class="notification-title">{{ notification.title }}</div>
                    <div class="notification-message">{{ notification.message }}</div>
                    <div class="notification-time">{{ getRelativeTime(notification.createdAt) }}</div>
                  </div>
                </div>
              } @empty {
                <div class="notification-empty">No notifications</div>
              }
            </div>
          </div>
        }
      </div>
    </header>
  `,
  styles: [`
    .app-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 32px;
      background: #161b22;
      border-bottom: 1px solid #21262d;
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .header-search { position: relative; flex: 1; max-width: 500px; }
    .search-input {
      width: 100%;
      padding: 8px 16px;
      background: #0d1117;
      border: 1px solid #30363d;
      border-radius: 6px;
      color: #e1e4e8;
      font-size: 0.9rem;
      outline: none;
    }
    .search-input:focus { border-color: #58a6ff; box-shadow: 0 0 0 3px rgba(88,166,255,0.1); }
    .search-input::placeholder { color: #484f58; }
    .search-dropdown {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: #1c2128;
      border: 1px solid #30363d;
      border-radius: 6px;
      margin-top: 4px;
      max-height: 400px;
      overflow-y: auto;
      box-shadow: 0 8px 24px rgba(0,0,0,0.4);
    }
    .search-result-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 16px;
      cursor: pointer;
      text-decoration: none;
      color: #e1e4e8;
      border-bottom: 1px solid #21262d;
    }
    .search-result-item:hover { background: #21262d; }
    .result-key { color: #58a6ff; font-weight: 600; font-size: 0.85rem; min-width: 80px; }
    .result-title { flex: 1; font-size: 0.85rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .result-status {
      font-size: 0.7rem;
      padding: 2px 8px;
      border-radius: 10px;
      background: #21262d;
      color: #8b949e;
    }
    .search-more {
      padding: 10px 16px;
      text-align: center;
      color: #58a6ff;
      cursor: pointer;
      font-size: 0.85rem;
    }
    .search-more:hover { background: #21262d; }
    .header-actions { display: flex; align-items: center; gap: 16px; position: relative; }
    .notification-bell {
      position: relative;
      cursor: pointer;
      font-size: 1.3rem;
      padding: 6px;
      border-radius: 6px;
    }
    .notification-bell:hover { background: #21262d; }
    .bell-icon { filter: grayscale(1); }
    .badge {
      position: absolute;
      top: 0;
      right: 0;
      background: #f85149;
      color: white;
      font-size: 0.65rem;
      min-width: 16px;
      height: 16px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
    }
    .notification-dropdown {
      position: absolute;
      top: 100%;
      right: 0;
      width: 380px;
      background: #1c2128;
      border: 1px solid #30363d;
      border-radius: 8px;
      margin-top: 8px;
      box-shadow: 0 8px 24px rgba(0,0,0,0.4);
      z-index: 100;
    }
    .notification-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      border-bottom: 1px solid #21262d;
    }
    .notification-header h4 { margin: 0; color: #e1e4e8; font-size: 0.9rem; }
    .mark-all-btn {
      background: none;
      border: none;
      color: #58a6ff;
      font-size: 0.8rem;
      cursor: pointer;
    }
    .mark-all-btn:hover { text-decoration: underline; }
    .notification-list { max-height: 400px; overflow-y: auto; }
    .notification-item {
      display: flex;
      gap: 12px;
      padding: 12px 16px;
      border-bottom: 1px solid #21262d;
      cursor: pointer;
    }
    .notification-item:hover { background: #21262d; }
    .notification-item.unread { background: #161b22; border-left: 3px solid #58a6ff; }
    .notification-icon { font-size: 1.2rem; margin-top: 2px; }
    .notification-content { flex: 1; min-width: 0; }
    .notification-title { font-size: 0.85rem; color: #e1e4e8; font-weight: 500; }
    .notification-message { font-size: 0.8rem; color: #8b949e; margin-top: 2px; }
    .notification-time { font-size: 0.7rem; color: #484f58; margin-top: 4px; }
    .notification-empty { padding: 24px; text-align: center; color: #484f58; font-size: 0.85rem; }
  `]
})
export class HeaderComponent implements OnInit, OnDestroy {
  notificationService = inject(NotificationService);
  private searchService = inject(SearchService);
  private authService = inject(AuthService);
  private router = inject(Router);

  searchQuery = '';
  showSearchResults = false;
  showNotifications = false;
  searchResults = signal<Ticket[]>([]);
  notifications = signal<Notification[]>([]);
  private searchTimeout: any;

  ngOnInit(): void {
    this.notificationService.connect();
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.notificationService.disconnect();
  }

  onSearchInput(): void {
    clearTimeout(this.searchTimeout);
    if (!this.searchQuery || this.searchQuery.length < 2) {
      this.searchResults.set([]);
      return;
    }
    this.searchTimeout = setTimeout(() => {
      this.searchService.searchTickets({ q: this.searchQuery, size: 5 }).subscribe({
        next: (result) => this.searchResults.set(result.items),
        error: () => this.searchResults.set([])
      });
    }, 300);
  }

  performSearch(): void {
    if (this.searchQuery) {
      this.showSearchResults = false;
      this.router.navigate(['/search'], { queryParams: { q: this.searchQuery } });
    }
  }

  navigateToTicket(ticket: Ticket): void {
    this.showSearchResults = false;
    this.searchQuery = '';
    this.router.navigate(['/tickets', ticket.id]);
  }

  navigateToSearch(): void {
    this.showSearchResults = false;
    this.router.navigate(['/search'], { queryParams: { q: this.searchQuery } });
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      this.loadNotifications();
    }
  }

  handleNotificationClick(notification: Notification): void {
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe();
      this.notificationService.markLocalAsRead(notification.id);
      this.notifications.update(list =>
        list.map(n => n.id === notification.id ? { ...n, read: true } : n)
      );
    }
    this.showNotifications = false;
    if (notification.referenceId && notification.referenceType === 'TICKET') {
      this.router.navigate(['/tickets', notification.referenceId]);
    }
  }

  markAllRead(): void {
    this.notificationService.markAllAsRead().subscribe();
    this.notificationService.markAllLocalAsRead();
    this.notifications.update(list => list.map(n => ({ ...n, read: true })));
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'TICKET_ASSIGNED': return '👤';
      case 'TICKET_STATUS_CHANGED': return '🔄';
      case 'TICKET_COMMENTED': return '💬';
      case 'TICKET_MENTIONED': return '@';
      case 'SPRINT_STARTED': return '🚀';
      case 'SPRINT_COMPLETED': return '✅';
      case 'PROJECT_MEMBER_ADDED': return '👥';
      case 'TICKET_DUE_SOON': return '⏰';
      default: return '🔔';
    }
  }

  getRelativeTime(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    return `${days}d ago`;
  }

  private loadNotifications(): void {
    this.notificationService.loadNotifications().subscribe({
      next: (list) => this.notifications.set(list),
      error: () => {}
    });
  }
}
