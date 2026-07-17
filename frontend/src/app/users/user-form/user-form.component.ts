import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UserService } from '../user.service';
import { resolvePhotoUrl } from '../user.model';

function optionalPasswordMatch(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (!password && !confirmPassword) {
    return null;
  }
  return password === confirmPassword ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss'
})
export class UserFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly matricule = signal('');
  readonly photoUrl = signal<string | null>(null);
  readonly uploadingPhoto = signal(false);
  readonly photoError = signal<string | null>(null);

  form = this.fb.group(
    {
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      role: ['', [Validators.required]],
      password: ['', [Validators.minLength(8)]],
      confirmPassword: ['']
    },
    { validators: optionalPasswordMatch }
  );

  private userId = '';

  constructor() {
    this.userId = this.route.snapshot.paramMap.get('id') ?? '';
    this.loadUser();
  }

  isInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  showMismatch(): boolean {
    const confirm = this.form.get('confirmPassword');
    return this.form.hasError('passwordsMismatch') && !!confirm?.value && (confirm.touched || confirm.dirty);
  }

  get formInitials(): string {
    const first = this.form.get('firstName')?.value?.charAt(0) ?? '';
    const last = this.form.get('lastName')?.value?.charAt(0) ?? '';
    return (first + last).toUpperCase() || 'U';
  }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.photoError.set(null);
    this.uploadingPhoto.set(true);

    this.userService.uploadPhoto(this.userId, file).subscribe({
      next: (user) => {
        this.uploadingPhoto.set(false);
        // Cache-bust so the freshly uploaded image replaces the old one immediately.
        const resolved = resolvePhotoUrl(user.photoUrl);
        this.photoUrl.set(resolved ? `${resolved}?t=${Date.now()}` : null);
        input.value = '';
      },
      error: (err) => {
        this.uploadingPhoto.set(false);
        this.photoError.set(err?.error?.message ?? "L'envoi de la photo a échoué.");
        input.value = '';
      }
    });
  }

  private loadUser(): void {
    if (!this.userId) {
      this.loading.set(false);
      this.errorMessage.set('Utilisateur introuvable.');
      return;
    }

    this.userService.getById(this.userId).subscribe({
      next: (user) => {
        this.matricule.set(user.matricule);
        this.photoUrl.set(resolvePhotoUrl(user.photoUrl));
        this.form.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email,
          role: user.role
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? "Impossible de charger l'utilisateur.");
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const value = this.form.getRawValue();
    const password = value.password?.trim();

    this.userService
      .update(this.userId, {
        firstName: value.firstName!,
        lastName: value.lastName!,
        email: value.email!,
        role: value.role as 'ADMIN' | 'AGENT_CREDIT',
        ...(password ? { password } : {})
      })
      .subscribe({
        next: () => this.router.navigate(['/app/users']),
        error: (err) => {
          this.saving.set(false);
          this.errorMessage.set(err?.error?.message ?? "La modification a échoué.");
        }
      });
  }
}
