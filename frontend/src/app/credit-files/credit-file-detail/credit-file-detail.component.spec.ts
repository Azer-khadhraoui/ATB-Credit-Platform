import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CreditFile } from '../credit-file.model';
import { CreditFileService } from '../credit-file.service';
import { CreditFileDetailComponent } from './credit-file-detail.component';

/** Minimal CreditFile satisfying the required fields; tests override what they care about. */
function makeCreditFile(overrides: Partial<CreditFile> = {}): CreditFile {
  return {
    id: 'file-1',
    clientId: 'client-1',
    createdBy: 'agent01',
    creditType: 'Crédit auto',
    loanAmount: 20000,
    loanDurationMonths: 48,
    loanPurpose: 'Voiture',
    status: 'ANALYZED',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    ...overrides
  };
}

describe('CreditFileDetailComponent', () => {
  let getById: jasmine.Spy;
  let analyze: jasmine.Spy;

  beforeEach(() => {
    getById = jasmine.createSpy('getById').and.returnValue(of(makeCreditFile()));
    analyze = jasmine.createSpy('analyze').and.returnValue(of(makeCreditFile()));

    TestBed.configureTestingModule({
      imports: [CreditFileDetailComponent],
      providers: [
        { provide: CreditFileService, useValue: { getById, analyze } },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => 'file-1' } } } }
      ]
    });
  });

  /** Instantiates the component; the constructor immediately calls getById. */
  function create(): CreditFileDetailComponent {
    return TestBed.createComponent(CreditFileDetailComponent).componentInstance;
  }

  describe('initial load', () => {
    it('exposes the loaded file and stops the spinner', () => {
      const component = create();
      expect(component.creditFile()?.id).toBe('file-1');
      expect(component.loading()).toBeFalse();
    });

    it('surfaces the error message when loading fails', () => {
      getById.and.returnValue(throwError(() => ({ error: { message: 'introuvable' } })));
      const component = create();
      expect(component.errorMessage()).toBe('introuvable');
      expect(component.loading()).toBeFalse();
    });
  });

  describe('label helpers', () => {
    it('delegates each label to the model', () => {
      const component = create();
      expect(component.statusLabel('DRAFT')).toBe('Nouveau');
      expect(component.riskLevelLabel('LOW')).toBe('Faible');
      expect(component.aiDecisionLabel('ACCEPTABLE')).toBe('Favorable');
      expect(component.creditHistoryLabel('GOOD')).toBe('Bon — aucun incident de paiement');
    });
  });

  describe('weightedFactors', () => {
    it('returns an empty list when there are no factors', () => {
      const component = create();
      expect(component.weightedFactors(makeCreditFile({ decisionFactors: [] }))).toEqual([]);
      expect(component.weightedFactors(makeCreditFile({ decisionFactors: null }))).toEqual([]);
    });

    it('scales each bar against the strongest factor and labels it', () => {
      const component = create();
      const cf = makeCreditFile({
        decisionFactors: [
          { feature: 'Credit_History', impact: -2.0, reducesRisk: false },
          { feature: 'Loan_Amount_Term', impact: 1.0, reducesRisk: true },
          { feature: 'Married', impact: 0.5, reducesRisk: true }
        ]
      });

      const result = component.weightedFactors(cf);

      expect(result.map((f) => f.width)).toEqual([100, 50, 25]);
      expect(result[0].label).toBe('Historique de crédit');
      expect(result[0].reducesRisk).toBeFalse();
    });

    it('produces zero width when every factor has zero impact', () => {
      const component = create();
      const cf = makeCreditFile({
        decisionFactors: [
          { feature: 'Gender', impact: 0, reducesRisk: false },
          { feature: 'Married', impact: 0, reducesRisk: true }
        ]
      });
      expect(component.weightedFactors(cf).every((f) => f.width === 0)).toBeTrue();
    });
  });

  describe('hasAnalysis', () => {
    it('is true once a score, level or decision is present', () => {
      const component = create();
      expect(component.hasAnalysis(makeCreditFile({ riskScore: 12 }))).toBeTrue();
      expect(component.hasAnalysis(makeCreditFile({ aiDecision: 'ACCEPTABLE' }))).toBeTrue();
    });

    it('is false on an unanalysed file', () => {
      const component = create();
      expect(component.hasAnalysis(makeCreditFile())).toBeFalse();
    });
  });

  describe('runAnalysis', () => {
    it('replaces the file with the analysed result and clears the spinner', () => {
      const component = create();
      analyze.and.returnValue(of(makeCreditFile({ riskScore: 42, riskLevel: 'MEDIUM' })));

      component.runAnalysis();

      expect(analyze).toHaveBeenCalledWith('file-1');
      expect(component.creditFile()?.riskScore).toBe(42);
      expect(component.analyzing()).toBeFalse();
      expect(component.analyzeError()).toBeNull();
    });

    it('reports the error and stops the spinner when analysis fails', () => {
      const component = create();
      analyze.and.returnValue(throwError(() => ({ error: { message: 'moteur indisponible' } })));

      component.runAnalysis();

      expect(component.analyzeError()).toBe('moteur indisponible');
      expect(component.analyzing()).toBeFalse();
    });
  });
});
