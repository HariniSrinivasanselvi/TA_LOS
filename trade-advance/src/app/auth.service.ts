import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';

export interface CurrentUserInfo {
  authenticated: boolean;
  id?: number;
  email?: string;
  displayName?: string;
  roles?: string[];
}

export interface MenuItem {
  code: string;
  displayName: string;
  route: string;
}

export const DEALER_ROLE = 'DLR0001';
export const ADMIN_ROLE = 'AD0001';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  readonly api = '/api';

  private userRequest$?: Observable<CurrentUserInfo>;

  fetchCurrentUser(): Observable<CurrentUserInfo> {
    if (!this.userRequest$) {
      this.userRequest$ = this.http
        .get<CurrentUserInfo>(`${this.api}/auth/user`)
        .pipe(shareReplay(1));
    }
    return this.userRequest$;
  }

  fetchMenus(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.api}/menus`);
  }

  login(): void {
    window.location.href = `${this.api}/login?returnTo=${encodeURIComponent(window.location.pathname)}`;
  }

  logout(): void {
    window.location.href = `${this.api}/logout`;
  }
}
