import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
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
  let component: CreditFileDetailComponent;

  beforeEach(() => {
    const serviceStub = {
      getById: () => of(makeCreditFile()),
      analyze: () => of(makeCreditFile())
    };
    const routeStub = { snapshot: { paramMap: { get: () => 'file-1' } } };

    TestBed.configureTestingModule({
      imports: [CreditFileDetailComponent],
      providers: [
        { provide: CreditFileService, useValue: serviceStub },
        { provide: ActivatedRoute, useValue: routeStub }
      ]
    });

    component = TestBed.createComponent(CreditFileDetailComponent).componentInstance;
  });

  describe('weightedFactors', () => {
    it('returns an empty list when there are no factors', () => {
      expect(component.weightedFactors(makeCreditFile({ decisionFactors: [] }))).toEqual([]);
      expect(component.weightedFactors(makeCreditFile({ decisionFactors: null }))).toEqual([]);
    });

    it('scales each bar against the strongest factor and labels it', () => {
      const cf = makeCreditFile({
        decisionFactors: [
          { feature: 'Credit_History', impact: -2.0, reducesRisk: false },
          { feature: 'Loan_Amount_Term', impact: 1.0, reducesRisk: true },
          { feature: 'Married', impact: 0.5, reducesRisk: true }
        ]
      });

      const result = component.weightedFactors(cf);

      // The strongest |impact| (2.0) fills the bar; the others are its fractions.
      expect(result.map((f) => f.width)).toEqual([100, 50, 25]);
      // Model column names are replaced by readable labels.
      expect(result[0].label).toBe('Historique de crédit');
      // The direction flag is carried through untouched.
      expect(result[0].reducesRisk).toBeFalse();
    });

    it('produces zero width when every factor has zero impact', () => {
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
      expect(component.hasAnalysis(makeCreditFile({ riskScore: 12 }))).toBeTrue();
      expect(component.hasAnalysis(makeCreditFile({ aiDecision: 'ACCEPTABLE' }))).toBeTrue();
    });

    it('is false on an unanalysed file', () => {
      expect(component.hasAnalysis(makeCreditFile())).toBeFalse();
    });
  });
});
