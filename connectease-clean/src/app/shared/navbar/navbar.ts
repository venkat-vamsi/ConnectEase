import { Component, inject } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent {
  authService = inject(AuthService);
  private router = inject(Router);

  // Checks if the logged-in user is a vendor
  isVendor(): boolean {
    return localStorage.getItem('role') === 'vendor';
  }

  logout() {
    this.authService.logout().subscribe(() => {
      localStorage.clear();
      this.router.navigate(['/login']);
    });
  }
}