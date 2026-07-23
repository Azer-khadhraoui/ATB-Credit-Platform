import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.scss'
})
export class SignupComponent {
  /** How long the exit transition plays before the route actually changes. */
  private static readonly TRANSITION_MS = 450;

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly leaving = signal(false);

  signupForm: FormGroup = this.fb.group(
    {
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      matricule: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    },
    { validators: passwordsMatchValidator }
  );

  isInvalid(controlName: string): boolean {
    const control = this.signupForm.get(controlName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  showMismatch(): boolean {
    const confirm = this.signupForm.get('confirmPassword');
    return (
      this.signupForm.hasError('passwordsMismatch') &&
      !!confirm?.value &&
      (confirm.touched || confirm.dirty)
    );
  }

  onSubmit(): void {
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const value = this.signupForm.getRawValue();

    this.authService
      .signup({
        firstName: value.firstName,
        lastName: value.lastName,
        matricule: value.matricule,
        email: value.email,
        password: value.password,
        role: 'AGENT_CREDIT'
      })
      .subscribe({
        next: () => {
          this.leaving.set(true);
          setTimeout(() => this.router.navigate(['/login']), SignupComponent.TRANSITION_MS);
        },
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(err?.error?.message ?? "Échec de la création du compte. Réessayez.");
        }
      });
  }

  /** Plays the card's exit transition before switching to the login route. */
  goToLogin(event: Event): void {
    event.preventDefault();
    this.leaving.set(true);
    setTimeout(() => this.router.navigate(['/login']), SignupComponent.TRANSITION_MS);
  }
}
