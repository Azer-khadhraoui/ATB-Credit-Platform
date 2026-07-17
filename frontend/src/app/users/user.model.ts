import { environment } from '../../environments/environment';

export type Role = 'ADMIN' | 'AGENT_CREDIT';

export function resolvePhotoUrl(photoUrl?: string | null): string | null {
  if (!photoUrl) {
    return null;
  }
  return photoUrl.startsWith('http') ? photoUrl : environment.serverUrl + photoUrl;
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  matricule: string;
  email: string;
  role: Role;
  photoUrl?: string | null;
  createdAt: string;
  updatedAt: string;
}
