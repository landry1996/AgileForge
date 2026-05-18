import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <h2>Create your account</h2>
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-row">
            <div class="form-group">
              <label for="firstName">First Name</label>
              <input id="firstName" type="text" formControlName="firstName">
            </div>
            <div class="form-group">
              <label for="lastName">Last Name</label>
              <input id="lastName" type="text" formControlName="lastName">
            </div>
          </div>
          <div class="form-group">
            <label for="email">Email</label>
            <input id="email" type="email" formControlName="email" placeholder="you&#64;example.com">
          </div>
          <div class="form-group">
            <label for="password">Password</label>
            <input id="password" type="password" formControlName="password" placeholder="Min. 8 characters">
          </div>
          @if (error) {
            <div class="error-message">{{ error }}</div>
          }
          <button type="submit" [disabled]="form.invalid || loading" class="btn-primary">
            {{ loading ? 'Creating account...' : 'Create account' }}
          </button>
        </form>
        <p class="auth-link">Already have an account? <a routerLink="/auth/login">Sign in</a></p>
      </div>
    </div>
  `,
  styles: [`
    .auth-container {
      min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #0d1117;
    }
    .auth-card {
      background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 40px; width: 100%; max-width: 440px;
    }
    h2 { color: #e1e4e8; text-align: center; margin-bottom: 24px; }
    .form-row { display: flex; gap: 12px; }
    .form-row .form-group { flex: 1; }
    .form-group { margin-bottom: 16px; }
    label { display: block; color: #8b949e; font-size: 0.85rem; margin-bottom: 6px; }
    input {
      width: 100%; padding: 10px 12px; background: #0d1117; border: 1px solid #30363d;
      border-radius: 6px; color: #e1e4e8; font-size: 0.9rem; box-sizing: border-box;
    }
    input:focus { border-color: #58a6ff; outline: none; }
    .btn-primary {
      width: 100%; padding: 12px; background: #238636; color: white; border: none;
      border-radius: 6px; font-size: 0.9rem; font-weight: 600; cursor: pointer; margin-top: 8px;
    }
    .btn-primary:hover { background: #2ea043; }
    .btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
    .error-message { color: #f85149; font-size: 0.85rem; margin-bottom: 12px; }
    .auth-link { text-align: center; color: #8b949e; font-size: 0.85rem; margin-top: 16px; }
    .auth-link a { color: #58a6ff; text-decoration: none; }
  `]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  loading = false;
  error = '';

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';

    this.authService.register(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Registration failed';
      }
    });
  }
}
