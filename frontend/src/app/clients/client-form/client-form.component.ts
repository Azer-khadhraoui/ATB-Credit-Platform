import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ClientService } from '../client.service';
import {
  EDUCATION_LEVEL_OPTIONS,
  EMPLOYMENT_TYPE_OPTIONS,
  GENDER_OPTIONS,
  MARITAL_STATUS_OPTIONS,
  PROPERTY_AREA_OPTIONS
} from '../client.model';

@Component({
  selector: 'app-client-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './client-form.component.html',
  styleUrl: './client-form.component.scss'
})
export class ClientFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly clientService = inject(ClientService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly genders = GENDER_OPTIONS;
  readonly maritalStatuses = MARITAL_STATUS_OPTIONS;
  readonly employmentTypes = EMPLOYMENT_TYPE_OPTIONS;
  readonly educationLevels = EDUCATION_LEVEL_OPTIONS;
  readonly propertyAreas = PROPERTY_AREA_OPTIONS;

  private readonly clientId = this.route.snapshot.paramMap.get('id');
  readonly isEditMode = !!this.clientId;
  readonly pageTitle = computed(() => (this.isEditMode ? 'Modifier le client' : 'Ajouter un client'));

  readonly loading = signal(this.isEditMode);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  form = this.fb.group({
    cin: ['', [Validators.required]],
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    birthDate: ['', [Validators.required]],
    gender: ['', [Validators.required]],
    maritalStatus: ['', [Validators.required]],
    dependents: [0 as number | null, [Validators.required, Validators.min(0)]],
    educationLevel: ['', [Validators.required]],

    phone: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    address: ['', [Validators.required]],
    city: ['', [Validators.required]],
    propertyArea: ['', [Validators.required]],

    profession: [''],
    employer: [''],
    employmentType: ['', [Validators.required]],
    monthlyIncome: [null as number | null, [Validators.required, Validators.min(0)]],
    monthlyExpenses: [null as number | null, [Validators.min(0)]]
  });

  constructor() {
    if (this.isEditMode) {
      this.loadClient();
    }
  }

  isInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  private loadClient(): void {
    this.clientService.getById(this.clientId!).subscribe({
      next: (client) => {
        this.form.patchValue({
          cin: client.cin,
          firstName: client.firstName,
          lastName: client.lastName,
          birthDate: client.birthDate?.substring(0, 10),
          gender: client.gender,
          maritalStatus: client.maritalStatus,
          dependents: client.dependents ?? 0,
          educationLevel: client.educationLevel,
          phone: client.phone,
          email: client.email,
          address: client.address,
          city: client.city,
          propertyArea: client.propertyArea,
          profession: client.profession ?? '',
          employer: client.employer ?? '',
          employmentType: client.employmentType,
          monthlyIncome: client.monthlyIncome,
          monthlyExpenses: client.monthlyExpenses ?? null
        });
        this.form.get('cin')?.disable();
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Impossible de charger le client.');
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
    const payload = {
      cin: value.cin!,
      firstName: value.firstName!,
      lastName: value.lastName!,
      birthDate: value.birthDate!,
      gender: value.gender as any,
      maritalStatus: value.maritalStatus as any,
      dependents: value.dependents!,
      educationLevel: value.educationLevel as any,
      phone: value.phone!,
      email: value.email!,
      address: value.address!,
      city: value.city!,
      propertyArea: value.propertyArea as any,
      profession: value.profession || undefined,
      employer: value.employer || undefined,
      employmentType: value.employmentType as any,
      monthlyIncome: value.monthlyIncome!,
      monthlyExpenses: value.monthlyExpenses ?? undefined
    };

    const request$ = this.isEditMode
      ? this.clientService.update(this.clientId!, payload)
      : this.clientService.create(payload);

    request$.subscribe({
      next: () => this.router.navigate(['/app/clients']),
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? "L'enregistrement a échoué.");
      }
    });
  }
}
