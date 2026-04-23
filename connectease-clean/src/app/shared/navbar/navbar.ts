import { Component, OnInit, inject, HostListener } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent implements OnInit {
  authService = inject(AuthService);
  private router = inject(Router);

  isLoggedIn = false;
  showDropdown = false;

  ngOnInit() {
    this.authService.isLoggedIn$.subscribe(status => { this.isLoggedIn = status; });
  }

  isVendor(): boolean { return localStorage.getItem('role') === 'vendor'; }
  getInitial(): string { return (localStorage.getItem('fullName') || 'U').charAt(0).toUpperCase(); }
  getFullName(): string { return localStorage.getItem('fullName') || 'User'; }

  toggleDropdown() { this.showDropdown = !this.showDropdown; }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.profile-container')) this.showDropdown = false;
  }

  logout() {
    this.showDropdown = false;
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => {
        localStorage.clear();
        this.authService.updateAuthStatus(false);
        this.router.navigate(['/login']);
      }
    });
  }
}
