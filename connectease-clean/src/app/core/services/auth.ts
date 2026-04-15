import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  signup(data: any) {
    return this.http.post('/api/auth/signup', data);
  }

  login(credentials: any) {
    return this.http.post('/api/auth/signin', credentials).pipe(
      tap((res: any) => {
        if(res.status === 'success') localStorage.setItem('role', res.role);
      })
    );
  }

  logout() {
    return this.http.post('/api/auth/logout', {}).pipe(
      tap(() => localStorage.removeItem('role'))
    );
  }
}