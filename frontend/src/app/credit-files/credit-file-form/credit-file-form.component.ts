import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CreditFileService } from '../credit-file.service';
import { CREDIT_TYPE_OPTIONS, GUARANTEE_TYPE_OPTIONS, CREDIT_HISTORY_OPTIONS, STATUS_OPTIONS, RiskLevel, riskLevelLabel } from '../credit-file.model';
import { ClientService } from '../../clients/client.service';
import { Client } from '../../clients/client.model';

@Component({
  selector: 'app-credit-file-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './credit-file-form.component.html',
  styleUrl: './credit-file-form.component.scss'
})
export class CreditFileFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly creditFileService = inject(CreditFileService);
  private readonly clientService = inject(ClientService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly creditTypes = CREDIT_TYPE_OPTIONS;
  readonly guaranteeTypes = GUARANTEE_TYPE_OPTIONS;
  readonly creditHistories = CREDIT_HISTORY_OPTIONS;
  readonly statuses = STATUS_OPTIONS;

  private readonly creditFileId = this.route.snapshot.paramMap.get('id');
  readonly isEditMode = !!this.creditFileId;
  readonly pageTitle = computed(() => (this.isEditMode ? 'Modifier le dossier de crédit' : 'Ajouter un dossier de crédit'));

  readonly clients = signal<Client[]>([]);
  readonly loading = signal(this.isEditMode);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly riskScore = signal<number | null>(null);
  readonly riskLevel = signal<RiskLevel | null>(null);
  readonly riskLevelText = computed(() => riskLevelLabel(this.riskLevel()) ?? 'En attente');

  form = this.fb.group({
    clientId: ['', [Validators.required]],
    creditType: ['', [Validators.required]],
    loanAmount: [null as number | null, [Validators.required, Validators.min(0)]],
    loanDurationMonths: [null as number | null, [Validators.required, Validators.min(1)]],
    loanPurpose: ['', [Validators.required]],

    interestRate: [null as number | null, [Validators.min(0)]],
    downPayment: [null as number | null, [Validators.min(0)]],
    existingCredits: [null as number | null, [Validators.min(0)]],
    monthlyInstallment: [null as number | null, [Validators.min(0)]],

    guaranteeType: [''],
    creditHistory: [''],

    agentDecision: [''],
    status: ['DRAFT'],
    comments: ['', [Validators.maxLength(500)]]
  });

  constructor() {
    this.clientService.getAll().subscribe({
      next: (clients) => this.clients.set(clients),
      error: () => this.clients.set([])
    });

    if (this.isEditMode) {
      this.loadCreditFile();
    }
  }

  isInvalid(controlName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  private loadCreditFile(): void {
    this.creditFileService.getById(this.creditFileId!).subscribe({
      next: (creditFile) => {
        this.form.patchValue({
          clientId: creditFile.clientId,
          creditType: creditFile.creditType,
          loanAmount: creditFile.loanAmount,
          loanDurationMonths: creditFile.loanDurationMonths,
          loanPurpose: creditFile.loanPurpose,
          interestRate: creditFile.interestRate ?? null,
          downPayment: creditFile.downPayment ?? null,
          existingCredits: creditFile.existingCredits ?? null,
          monthlyInstallment: creditFile.monthlyInstallment ?? null,
          guaranteeType: creditFile.guaranteeType ?? '',
          creditHistory: creditFile.creditHistory ?? '',
          agentDecision: creditFile.agentDecision ?? '',
          status: creditFile.status,
          comments: creditFile.comments ?? ''
        });
        this.form.get('clientId')?.disable();
        this.riskScore.set(creditFile.riskScore ?? null);
        this.riskLevel.set(creditFile.riskLevel ?? null);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Impossible de charger le dossier.');
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
      creditType: value.creditType!,
      loanAmount: value.loanAmount!,
      loanDurationMonths: value.loanDurationMonths!,
      loanPurpose: value.loanPurpose!,
      interestRate: value.interestRate ?? undefined,
      downPayment: value.downPayment ?? undefined,
      existingCredits: value.existingCredits ?? undefined,
      monthlyInstallment: value.monthlyInstallment ?? undefined,
      guaranteeType: value.guaranteeType || undefined,
      creditHistory: value.creditHistory || undefined,
      agentDecision: value.agentDecision || undefined,
      status: value.status || undefined,
      comments: value.comments || undefined
    };

    const request$ = this.isEditMode
      ? this.creditFileService.update(this.creditFileId!, payload)
      : this.creditFileService.create({ ...payload, clientId: value.clientId! });

    request$.subscribe({
      next: () => this.router.navigate(['/app/credit-files']),
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? "L'enregistrement a échoué.");
      }
    });
  }
}
