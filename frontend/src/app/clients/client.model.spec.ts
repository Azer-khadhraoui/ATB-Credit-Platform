import {
  educationLevelLabel,
  employmentTypeLabel,
  genderLabel,
  maritalStatusLabel,
  propertyAreaLabel
} from './client.model';

describe('client.model labels', () => {
  it('translates gender', () => {
    expect(genderLabel('MALE')).toBe('Homme');
    expect(genderLabel('FEMALE')).toBe('Femme');
  });

  it('translates education level', () => {
    expect(educationLevelLabel('GRADUATE')).toBe('Diplômé (bac+ / supérieur)');
    expect(educationLevelLabel('NOT_GRADUATE')).toBe('Non diplômé');
  });

  it('translates property area', () => {
    expect(propertyAreaLabel('URBAN')).toBe('Urbaine');
    expect(propertyAreaLabel('SEMIURBAN')).toBe('Semi-urbaine');
    expect(propertyAreaLabel('RURAL')).toBe('Rurale');
  });

  it('translates marital status', () => {
    expect(maritalStatusLabel('SINGLE')).toBe('Célibataire');
    expect(maritalStatusLabel('MARRIED')).toBe('Marié(e)');
  });

  it('translates employment type', () => {
    expect(employmentTypeLabel('PERMANENT')).toBe('Permanent');
    expect(employmentTypeLabel('SELF_EMPLOYED')).toBe('Indépendant');
  });

  it('falls back to the raw value when unmapped', () => {
    expect(genderLabel('X' as never)).toBe('X');
    expect(maritalStatusLabel('X' as never)).toBe('X');
  });
});
