export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  roles: string[];
}

export interface AuthUser {
  username: string;
  roles: string[];
}

export function hasRole(user: AuthUser | null, role: string): boolean {
  return user?.roles?.includes(role) ?? false;
}

export function isAdmin(user: AuthUser | null): boolean {
  return hasRole(user, 'ROLE_ADMIN');
}