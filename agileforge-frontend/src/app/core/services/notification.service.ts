import { Injectable, signal, computed, NgZone, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notification } from '../models/notification.model';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private readonly apiUrl = `${environment.apiUrl}/notifications`;
  private eventSource: EventSource | null = null;

  private notifications = signal<Notification[]>([]);
  private unreadCount = signal<number>(0);

  readonly allNotifications = this.notifications.asReadonly();
  readonly unread = computed(() => this.notifications().filter(n => !n.read));
  readonly unreadBadgeCount = this.unreadCount.asReadonly();

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private ngZone: NgZone
  ) {}

  connect(): void {
    const token = this.authService.getAccessToken();
    if (!token || this.eventSource) return;

    this.eventSource = new EventSource(`${this.apiUrl}/stream?token=${token}`);

    this.eventSource.addEventListener('connected', (event: MessageEvent) => {
      this.ngZone.run(() => {
        const data = JSON.parse(event.data);
        this.unreadCount.set(data.unreadCount);
      });
    });

    this.eventSource.addEventListener('notification', (event: MessageEvent) => {
      this.ngZone.run(() => {
        const notification: Notification = JSON.parse(event.data);
        this.notifications.update(list => [notification, ...list]);
        this.unreadCount.update(count => count + 1);
      });
    });

    this.eventSource.addEventListener('count', (event: MessageEvent) => {
      this.ngZone.run(() => {
        const data = JSON.parse(event.data);
        this.unreadCount.set(data.unreadCount);
      });
    });

    this.eventSource.onerror = () => {
      this.disconnect();
      setTimeout(() => this.connect(), 5000);
    };
  }

  disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }

  loadNotifications(page = 0, size = 20): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}?page=${page}&size=${size}`);
  }

  loadUnread(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/unread`);
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/unread/count`);
  }

  markAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/read-all`, {});
  }

  markLocalAsRead(id: string): void {
    this.notifications.update(list =>
      list.map(n => n.id === id ? { ...n, read: true } : n)
    );
    this.unreadCount.update(count => Math.max(0, count - 1));
  }

  markAllLocalAsRead(): void {
    this.notifications.update(list => list.map(n => ({ ...n, read: true })));
    this.unreadCount.set(0);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
