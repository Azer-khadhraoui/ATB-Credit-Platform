export type Role = 'ADMIN' | 'AGENT_CREDIT';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  matricule: string;
  email: string;
  role: Role;
  createdAt: string;
  updatedAt: string;
}
