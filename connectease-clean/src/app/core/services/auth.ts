import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  private authStatus = new BehaviorSubject<boolean>(!!localStorage.getItem('role'));
  isLoggedIn$ = this.authStatus.asObservable();

  updateAuthStatus(status: boolean) { this.authStatus.next(status); }
  getUid(): string { return localStorage.getItem('uid') || ''; }
  getRole(): string { return localStorage.getItem('role') || ''; }
  getFullName(): string { return localStorage.getItem('fullName') || ''; }
  getImage(): string { return localStorage.getItem('image') || ''; }
  setImage(url: string) { localStorage.setItem('image', url || ''); }

  signup(data: any) {
    return this.http.post(`${environment.apiUrl}/auth/signup`, data);
  }

  login(credentials: any) {
    return this.http.post(`${environment.apiUrl}/auth/signin`, credentials).pipe(
      tap((res: any) => {
        if (res.status === 'success') {
          localStorage.setItem('role', res.role);
          localStorage.setItem('uid', res.uid);
          localStorage.setItem('fullName', res.fullName || '');
          localStorage.setItem('image', res.image || '');
          this.updateAuthStatus(true);
        }
      })
    );
  }

  logout() {
    return this.http.post(`${environment.apiUrl}/auth/logout`, {}).pipe(
      tap(() => {
        localStorage.removeItem('role');
        localStorage.removeItem('uid');
        localStorage.removeItem('fullName');
        localStorage.removeItem('image');
        this.updateAuthStatus(false);
      })
    );
  }
}