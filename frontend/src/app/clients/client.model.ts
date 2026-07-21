export type Gender = 'MALE' | 'FEMALE';
export type MaritalStatus = 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
export type EmploymentType = 'PERMANENT' | 'CONTRACT' | 'SELF_EMPLOYED' | 'UNEMPLOYED' | 'RETIRED';
export type EducationLevel = 'GRADUATE' | 'NOT_GRADUATE';
export type PropertyArea = 'URBAN' | 'SEMIURBAN' | 'RURAL';

export interface Client {
  id: string;
  cin: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  gender: Gender;
  maritalStatus: MaritalStatus;
  dependents: number;
  educationLevel: EducationLevel;
  phone: string;
  email: string;
  address: string;
  city: string;
  propertyArea: PropertyArea;
  profession?: string;
  employer?: string;
  employmentType: EmploymentType;
  monthlyIncome: number;
  monthlyExpenses?: number;
  createdAt: string;
  updatedAt: string;
}

export const GENDER_OPTIONS: { value: Gender; label: string }[] = [
  { value: 'MALE', label: 'Homme' },
  { value: 'FEMALE', label: 'Femme' }
];

export const MARITAL_STATUS_OPTIONS: { value: MaritalStatus; label: string }[] = [
  { value: 'SINGLE', label: 'Célibataire' },
  { value: 'MARRIED', label: 'Marié(e)' },
  { value: 'DIVORCED', label: 'Divorcé(e)' },
  { value: 'WIDOWED', label: 'Veuf / Veuve' }
];

export const EMPLOYMENT_TYPE_OPTIONS: { value: EmploymentType; label: string }[] = [
  { value: 'PERMANENT', label: 'Permanent' },
  { value: 'CONTRACT', label: 'Contractuel' },
  { value: 'SELF_EMPLOYED', label: 'Indépendant' },
  { value: 'UNEMPLOYED', label: 'Sans emploi' },
  { value: 'RETIRED', label: 'Retraité' }
];

export const EDUCATION_LEVEL_OPTIONS: { value: EducationLevel; label: string }[] = [
  { value: 'GRADUATE', label: 'Diplômé (bac+ / supérieur)' },
  { value: 'NOT_GRADUATE', label: 'Non diplômé' }
];

export const PROPERTY_AREA_OPTIONS: { value: PropertyArea; label: string }[] = [
  { value: 'URBAN', label: 'Urbaine' },
  { value: 'SEMIURBAN', label: 'Semi-urbaine' },
  { value: 'RURAL', label: 'Rurale' }
];

export function genderLabel(value: Gender): string {
  return GENDER_OPTIONS.find((o) => o.value === value)?.label ?? value;
}

export function educationLevelLabel(value: EducationLevel): string {
  return EDUCATION_LEVEL_OPTIONS.find((o) => o.value === value)?.label ?? value;
}

export function propertyAreaLabel(value: PropertyArea): string {
  return PROPERTY_AREA_OPTIONS.find((o) => o.value === value)?.label ?? value;
}

export function maritalStatusLabel(value: MaritalStatus): string {
  return MARITAL_STATUS_OPTIONS.find((o) => o.value === value)?.label ?? value;
}

export function employmentTypeLabel(value: EmploymentType): string {
  return EMPLOYMENT_TYPE_OPTIONS.find((o) => o.value === value)?.label ?? value;
}
