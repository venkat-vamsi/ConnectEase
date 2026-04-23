import { Component, inject , OnInit} from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink,CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent {
  authService = inject(AuthService);
  private router = inject(Router);
  isLoggedIn = false;
  ngOnInit() {
    // Automatically updates 'isLoggedIn' whenever the service broadcasts a change
    this.authService.isLoggedIn$.subscribe(status => {
      this.isLoggedIn = status;
    });
  }

  // Checks if the logged-in user is a vendor
  isVendor(): boolean {
    return localStorage.getItem('role') === 'vendor';
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        localStorage.clear();
        this.router.navigate(['/login']);
      },
      error: () => {
        // Fallback if server call fails
        localStorage.clear();
        this.authService.updateAuthStatus(false);
        this.router.navigate(['/login']);
      }
    });
  }
}