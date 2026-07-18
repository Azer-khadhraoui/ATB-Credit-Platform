import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../users/user.service';
import { resolvePhotoUrl } from '../../users/user.model';
import { AuthService } from '../../core/auth/auth.service';

function optionalPasswordMatch(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (!password && !confirmPassword) {
    return null;
  }
  return password === confirmPassword ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss'
})
export class SettingsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  private readonly userId = this.authService.currentUser()?.id ?? '';
  private currentRole: 'ADMIN' | 'AGENT_CREDIT' = 'AGENT_CREDIT';

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly needsRelogin = signal(false);
  readonly matricule = signal('');
  readonly roleLabel = signal('');
  readonly photoUrl = signal<string | null>(null);
  readonly uploadingPhoto = signal(false);
  readonly photoError = signal<string | null>(null);

  form = this.fb.group(
    {
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.minLength(8)]],
      confirmPassword: ['']
    },
    { validators: optionalPasswordMatch }
  );

  constructor() {
    this.loadProfile();
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
        const cacheBusted = user.photoUrl ? `${user.photoUrl}?t=${Date.now()}` : null;
        this.photoUrl.set(resolvePhotoUrl(cacheBusted));
        this.authService.updateCurrentSession(this.userId, { photoUrl: cacheBusted });
        input.value = '';
      },
      error: (err) => {
        this.uploadingPhoto.set(false);
        this.photoError.set(err?.error?.message ?? "L'envoi de la photo a échoué.");
        input.value = '';
      }
    });
  }

  logoutAndReconnect(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private loadProfile(): void {
    if (!this.userId) {
      this.loading.set(false);
      this.needsRelogin.set(true);
      return;
    }

    this.userService.getById(this.userId).subscribe({
      next: (user) => {
        this.matricule.set(user.matricule);
        this.currentRole = user.role;
        this.roleLabel.set(user.role === 'ADMIN' ? 'Administrateur' : 'Agent de crédit');
        this.photoUrl.set(resolvePhotoUrl(user.photoUrl));
        this.form.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Impossible de charger votre profil.');
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
    this.successMessage.set(null);

    const value = this.form.getRawValue();
    const password = value.password?.trim();

    this.userService
      .update(this.userId, {
        firstName: value.firstName!,
        lastName: value.lastName!,
        email: value.email!,
        role: this.currentRole,
        ...(password ? { password } : {})
      })
      .subscribe({
        next: (user) => {
          this.saving.set(false);
          this.successMessage.set('Profil mis à jour avec succès.');
          this.authService.updateCurrentSession(this.userId, { fullName: `${user.firstName} ${user.lastName}` });
          this.form.patchValue({ password: '', confirmPassword: '' });
        },
        error: (err) => {
          this.saving.set(false);
          this.errorMessage.set(err?.error?.message ?? "La mise à jour a échoué.");
        }
      });
  }
}
