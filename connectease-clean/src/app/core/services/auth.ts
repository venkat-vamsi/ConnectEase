import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  private authStatus = new BehaviorSubject<boolean>(!!localStorage.getItem('role'));
  isLoggedIn$ = this.authStatus.asObservable();

  updateAuthStatus(status: boolean) { this.authStatus.next(status); }
  getUid(): string { return localStorage.getItem('uid') || ''; }
  getRole(): string { return localStorage.getItem('role') || ''; }
  getFullName(): string { return localStorage.getItem('fullName') || ''; }

  signup(data: any) {
    return this.http.post('/api/auth/signup', data);
  }

  login(credentials: any) {
    return this.http.post('/api/auth/signin', credentials).pipe(
      tap((res: any) => {
        if (res.status === 'success') {
          localStorage.setItem('role', res.role);
          localStorage.setItem('uid', res.uid);
          localStorage.setItem('fullName', res.fullName || '');
          this.updateAuthStatus(true);
        }
      })
    );
  }

  logout() {
    return this.http.post('/api/auth/logout', {}).pipe(
      tap(() => {
        localStorage.removeItem('role');
        localStorage.removeItem('uid');
        localStorage.removeItem('fullName');
        this.updateAuthStatus(false);
      })
    );
  }
}