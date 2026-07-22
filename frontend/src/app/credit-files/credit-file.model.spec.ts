import {
  aiDecisionLabel,
  creditHistoryLabel,
  decisionFactorLabel,
  riskLevelLabel,
  statusLabel
} from './credit-file.model';

describe('credit-file.model labels', () => {
  describe('statusLabel', () => {
    it('translates a known status', () => {
      expect(statusLabel('DRAFT')).toBe('Nouveau');
      expect(statusLabel('APPROVED')).toBe('Approuvé');
    });

    it('falls back to the raw value for an unknown status', () => {
      expect(statusLabel('WHATEVER' as never)).toBe('WHATEVER');
    });
  });

  describe('riskLevelLabel', () => {
    it('translates each level', () => {
      expect(riskLevelLabel('LOW')).toBe('Faible');
      expect(riskLevelLabel('MEDIUM')).toBe('Moyen');
      expect(riskLevelLabel('HIGH')).toBe('Élevé');
    });

    it('returns null when absent', () => {
      expect(riskLevelLabel(null)).toBeNull();
      expect(riskLevelLabel(undefined)).toBeNull();
    });
  });

  describe('aiDecisionLabel', () => {
    it('maps the decision to agent-facing wording', () => {
      expect(aiDecisionLabel('ACCEPTABLE')).toBe('Favorable');
      expect(aiDecisionLabel('RISKY')).toBe('À surveiller');
      expect(aiDecisionLabel('REJECTED')).toBe('Défavorable');
    });

    it('returns null when absent', () => {
      expect(aiDecisionLabel(null)).toBeNull();
    });
  });

  describe('decisionFactorLabel', () => {
    it('maps a model column name to readable French', () => {
      expect(decisionFactorLabel('Credit_History')).toBe('Historique de crédit');
      expect(decisionFactorLabel('Loan_Amount_Term')).toBe('Durée du prêt');
    });

    it('returns the raw feature name when it has no label', () => {
      expect(decisionFactorLabel('Some_Unknown_Feature')).toBe('Some_Unknown_Feature');
    });
  });

  describe('creditHistoryLabel', () => {
    it('translates a known history', () => {
      expect(creditHistoryLabel('GOOD')).toBe('Bon — aucun incident de paiement');
    });

    it('returns null for an empty value and echoes an unknown one', () => {
      expect(creditHistoryLabel(null)).toBeNull();
      expect(creditHistoryLabel('LEGACY_TEXT')).toBe('LEGACY_TEXT');
    });
  });
});
