import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <aside class="sidebar">
      <div class="sidebar-header">
        <h1 class="logo">AgileForge</h1>
      </div>

      <nav class="sidebar-nav">
        <a routerLink="/dashboard" routerLinkActive="active" class="nav-item">
          <span class="nav-icon">📊</span>
          <span class="nav-label">Dashboard</span>
        </a>
        <a routerLink="/board" routerLinkActive="active" class="nav-item">
          <span class="nav-icon">📋</span>
          <span class="nav-label">Board</span>
        </a>
        <a routerLink="/backlog" routerLinkActive="active" class="nav-item">
          <span class="nav-icon">📝</span>
          <span class="nav-label">Backlog</span>
        </a>
        <a routerLink="/sprints" routerLinkActive="active" class="nav-item">
          <span class="nav-icon">🏃</span>
          <span class="nav-label">Sprints</span>
        </a>
        <a routerLink="/ai" routerLinkActive="active" class="nav-item">
          <span class="nav-icon">🤖</span>
          <span class="nav-label">AI Assistant</span>
        </a>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info">
          <a routerLink="/profile" class="user-name-link">{{ authService.user()?.firstName }} {{ authService.user()?.lastName }}</a>
          <button (click)="authService.logout()" class="logout-btn">Logout</button>
        </div>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar {
      width: 250px;
      height: 100vh;
      background: #1a1d23;
      color: #e1e4e8;
      display: flex;
      flex-direction: column;
      position: fixed;
      left: 0;
      top: 0;
      z-index: 100;
    }
    .sidebar-header {
      padding: 20px;
      border-bottom: 1px solid #2d3139;
    }
    .logo {
      font-size: 1.4rem;
      font-weight: 700;
      color: #58a6ff;
      margin: 0;
    }
    .sidebar-nav {
      flex: 1;
      padding: 12px 0;
    }
    .nav-item {
      display: flex;
      align-items: center;
      padding: 12px 20px;
      color: #8b949e;
      text-decoration: none;
      transition: all 0.2s;
      gap: 12px;
    }
    .nav-item:hover { background: #21262d; color: #e1e4e8; }
    .nav-item.active { background: #21262d; color: #58a6ff; border-left: 3px solid #58a6ff; }
    .nav-icon { font-size: 1.2rem; }
    .nav-label { font-size: 0.9rem; }
    .sidebar-footer {
      padding: 16px 20px;
      border-top: 1px solid #2d3139;
    }
    .user-info { display: flex; flex-direction: column; gap: 8px; }
    .user-name-link { font-size: 0.85rem; color: #8b949e; text-decoration: none; }
    .user-name-link:hover { color: #58a6ff; }
    .logout-btn {
      background: none;
      border: 1px solid #30363d;
      color: #8b949e;
      padding: 6px 12px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.8rem;
    }
    .logout-btn:hover { color: #f85149; border-color: #f85149; }
  `]
})
export class SidebarComponent {
  authService = inject(AuthService);
}
