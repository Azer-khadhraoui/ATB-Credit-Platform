import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-credit-file-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './credit-file-form.component.html',
  styleUrl: './credit-file-form.component.scss'
})
export class CreditFileFormComponent {
  private readonly fb = new FormBuilder();

  creditTypes = ['Crédit consommation', 'Crédit immobilier', 'Crédit auto', 'Crédit professionnel'];
  guaranteeTypes = ['Hypothèque', 'Caution personnelle', 'Nantissement', 'Aucune'];
  agentDecisions = ['Favorable', 'Réservé', 'Défavorable'];
  statuses = ['Nouveau', "En cours d'examen", 'Analysé', 'Approuvé', 'Rejeté'];

  form = this.fb.group({
    reference: ['', [Validators.required]],
    clientId: ['', [Validators.required]],
    creditType: ['', [Validators.required]],
    loanAmount: [null, [Validators.required]],
    loanDurationMonths: [null, [Validators.required]],
    loanPurpose: ['', [Validators.required]],
    requestDate: ['', [Validators.required]],

    monthlyIncome: [null, [Validators.required]],
    monthlyExpenses: [null],
    otherCommitments: [null],

    guaranteeType: [''],
    guaranteeDescription: [''],
    guaranteeValue: [null],

    agentDecision: [''],
    status: ['Nouveau'],
    comments: ['', [Validators.maxLength(500)]]
  });

  get resteAVivre(): number | null {
    const income = Number(this.form.value.monthlyIncome ?? 0);
    const expenses = Number(this.form.value.monthlyExpenses ?? 0);
    const commitments = Number(this.form.value.otherCommitments ?? 0);
    if (!this.form.value.monthlyIncome) {
      return null;
    }
    return income - expenses - commitments;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    console.log({ ...this.form.value, resteAVivre: this.resteAVivre });
  }
}
