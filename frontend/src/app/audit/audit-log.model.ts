export type AuditAction = 'LOGIN' | 'CREATE' | 'UPDATE' | 'DELETE' | 'ANALYZE_WITH_AI';

export interface AuditLog {
  id: string;
  userId: string;
  userFullName?: string | null;
  action: AuditAction;
  entity: string;
  entityId: string;
  description: string;
  createdAt: string;
}

const ACTION_LABELS: Record<AuditAction, string> = {
  LOGIN: 'Connexion',
  CREATE: 'Création',
  UPDATE: 'Modification',
  DELETE: 'Suppression',
  ANALYZE_WITH_AI: 'Analyse IA'
};

const ENTITY_LABELS: Record<string, string> = {
  User: 'Utilisateur',
  Client: 'Client',
  CreditFile: 'Dossier de crédit'
};

export function actionLabel(value: AuditAction): string {
  return ACTION_LABELS[value] ?? value;
}

export function entityLabel(value: string): string {
  return ENTITY_LABELS[value] ?? value;
}
