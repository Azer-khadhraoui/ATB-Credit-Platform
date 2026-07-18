export type CreditStatus = 'DRAFT' | 'IN_REVIEW' | 'ANALYZED' | 'APPROVED' | 'REJECTED';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';
export type AIDecision = 'ACCEPTABLE' | 'RISKY' | 'REJECTED';

export interface CreditFile {
  id: string;
  clientId: string;
  clientFullName?: string | null;
  clientCin?: string | null;
  createdBy: string;
  creditType: string;
  loanAmount: number;
  loanDurationMonths: number;
  loanPurpose: string;
  interestRate?: number | null;
  downPayment?: number | null;
  existingCredits?: number | null;
  monthlyInstallment?: number | null;
  creditHistory?: string | null;
  guaranteeType?: string | null;
  status: CreditStatus;
  riskScore?: number | null;
  riskLevel?: RiskLevel | null;
  aiDecision?: AIDecision | null;
  agentDecision?: string | null;
  comments?: string | null;
  createdAt: string;
  updatedAt: string;
}

export const CREDIT_TYPE_OPTIONS = [
  'Crédit consommation',
  'Crédit immobilier',
  'Crédit auto',
  'Crédit professionnel'
];

export const GUARANTEE_TYPE_OPTIONS = ['Hypothèque', 'Caution personnelle', 'Nantissement', 'Aucune'];

export const STATUS_OPTIONS: { value: CreditStatus; label: string }[] = [
  { value: 'DRAFT', label: 'Nouveau' },
  { value: 'IN_REVIEW', label: "En cours d'examen" },
  { value: 'ANALYZED', label: 'Analysé' },
  { value: 'APPROVED', label: 'Approuvé' },
  { value: 'REJECTED', label: 'Rejeté' }
];

export function statusLabel(value: CreditStatus): string {
  return STATUS_OPTIONS.find((o) => o.value === value)?.label ?? value;
}

const RISK_LABELS: Record<RiskLevel, string> = {
  LOW: 'Faible',
  MEDIUM: 'Moyen',
  HIGH: 'Élevé'
};

export function riskLevelLabel(value?: RiskLevel | null): string | null {
  return value ? RISK_LABELS[value] : null;
}
