import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  // Initialize status based on whether a role exists in storage
  private authStatus = new BehaviorSubject<boolean>(!!localStorage.getItem('role'));
  isLoggedIn$ = this.authStatus.asObservable();

  // Helper to update the status manually
  updateAuthStatus(status: boolean) {
    this.authStatus.next(status);
  }

  signup(data: any) {
    return this.http.post('/api/auth/signup', data);
  }

  login(credentials: any) {
    return this.http.post('/api/auth/signin', credentials).pipe(
      tap((res: any) => {
        if(res.status === 'success') {
          localStorage.setItem('role', res.role);
          this.updateAuthStatus(true); // Notify Navbar
        }
      })
    );
  }

  logout() {
    return this.http.post('/api/auth/logout', {}).pipe(
      tap(() => {
        localStorage.removeItem('role');
        this.updateAuthStatus(false); // Notify Navbar
      })
    );
  }
}