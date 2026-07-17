import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-client-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './client-form.component.html',
  styleUrl: './client-form.component.scss'
})
export class ClientFormComponent {
  private readonly fb = new FormBuilder();

  genders = ['Homme', 'Femme'];
  maritalStatuses = ['Célibataire', 'Marié(e)', 'Divorcé(e)', 'Veuf / Veuve'];
  employmentTypes = ['Permanent', 'Contractuel', 'Indépendant', 'Sans emploi', 'Retraité'];

  form = this.fb.group({
    cin: ['', [Validators.required]],
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    birthDate: ['', [Validators.required]],
    gender: ['', [Validators.required]],
    maritalStatus: ['', [Validators.required]],

    phone: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    address: ['', [Validators.required]],
    city: ['', [Validators.required]],

    profession: [''],
    employer: [''],
    employmentType: ['', [Validators.required]],
    monthlyIncome: [null, [Validators.required]],
    monthlyExpenses: [null]
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    console.log(this.form.value);
  }
}
