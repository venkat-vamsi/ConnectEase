import { Component, OnInit, inject, HostListener } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent implements OnInit {
  authService = inject(AuthService);
  theme = inject(ThemeService);
  private router = inject(Router);

  isLoggedIn = false;
  showDropdown = false;
  mobileOpen = false;

  ngOnInit() {
    this.authService.isLoggedIn$.subscribe(status => { this.isLoggedIn = status; });
  }

  isVendor(): boolean { return localStorage.getItem('role') === 'vendor'; }
  getInitial(): string { return (localStorage.getItem('fullName') || 'U').charAt(0).toUpperCase(); }
  getFullName(): string { return localStorage.getItem('fullName') || 'User'; }
  getImage(): string { return this.authService.getImage(); }
  onImageError() { this.authService.setImage(''); }

  toggleDropdown() { this.showDropdown = !this.showDropdown; }
  toggleMobile() { this.mobileOpen = !this.mobileOpen; }
  closeMobile() { this.mobileOpen = false; }
  toggleTheme() { this.theme.toggle(); }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.profile-container')) this.showDropdown = false;
  }

  @HostListener('window:resize')
  onResize() {
    if (window.innerWidth > 900) this.mobileOpen = false;
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
