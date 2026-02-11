import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, AuthUser } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private currentUserSubject = new BehaviorSubject<AuthUser | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) { }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('http://localhost:8080/api/auth/signin', credentials)
      .pipe(
        tap(response => {
          this.setSession(response);
        })
      );
  }

  logout(): Observable<void> {
    return this.http.post<void>('http://localhost:8080/api/auth/signout', {})
      .pipe(
        tap(() => {
          this.clearSession();
        })
      );
  }

  clearSession(): void {
    if (this.isBrowser) {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
    }
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    try {
      const decoded: any = jwtDecode(token);
      const expirationDate = decoded.exp * 1000;
      const now = new Date().getTime();

      if (now >= expirationDate) {
        this.clearSession();
        return false;
      }

      return true;
    } catch (error) {
      this.clearSession();
      return false;
    }
  }

  getToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.roles?.includes(role) ?? false;
  }

  isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  private setSession(authResult: LoginResponse): void {
    const user: AuthUser = {
      username: authResult.username,
      roles: authResult.roles
    };

    if (this.isBrowser) {
      localStorage.setItem(this.TOKEN_KEY, authResult.token);
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }

    this.currentUserSubject.next(user);
  }

  private getUserFromStorage(): AuthUser | null {
    if (!this.isBrowser) {
      return null;
    }

    const userStr = localStorage.getItem(this.USER_KEY);
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  }
}