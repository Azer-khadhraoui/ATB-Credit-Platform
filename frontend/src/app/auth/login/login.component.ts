import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  /** How long the exit transition plays before the route actually changes. */
  private static readonly TRANSITION_MS = 450;
  /** How long the post-login logo animation plays before landing on the dashboard. */
  private static readonly SUCCESS_ANIMATION_MS = 1200;

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly leaving = signal(false);
  readonly loginSuccess = signal(false);

  loginForm = this.fb.group({
    matricule: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  isInvalid(controlName: string): boolean {
    const control = this.loginForm.get(controlName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const { matricule, password } = this.loginForm.getRawValue();

    this.authService.login(matricule!, password!).subscribe({
      next: () => {
        this.loginSuccess.set(true);
        // Let the logo animation play before landing on the dashboard, instead of
        // navigating instantly and cutting it short.
        setTimeout(() => this.router.navigate(['/app/dashboard']), LoginComponent.SUCCESS_ANIMATION_MS);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Échec de la connexion. Vérifiez vos identifiants.');
      }
    });
  }

  /** Plays the card's exit transition before switching to the signup route. */
  goToSignup(event: Event): void {
    event.preventDefault();
    this.leaving.set(true);
    setTimeout(() => this.router.navigate(['/signup']), LoginComponent.TRANSITION_MS);
  }
}
