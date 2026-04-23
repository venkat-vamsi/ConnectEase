import { Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth';

interface UserProfile {
  uid: string;
  fullName: string;
  email: string;
  phoneNo: string;
  image: string | null;
  role: string;
  createdAt: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  authService = inject(AuthService);

  profile: UserProfile | null = null;
  loading = true;
  activeSection: 'profile' | 'password' | 'danger' = 'profile';

  editForm = { fullName: '', phoneNo: '', image: '' };
  editSubmitting = false;
  editSuccess = '';
  editError = '';

  pwForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
  pwSubmitting = false;
  pwSuccess = '';
  pwError = '';

  deleteConfirm = false;

  ngOnInit() {
    const uid = this.authService.getUid();
    if (!uid) { this.router.navigate(['/login']); return; }
    this.loadProfile(uid);
  }

  loadProfile(uid: string) {
    this.loading = true;
    this.http.get<UserProfile>(`/api/users/${uid}`).subscribe({
      next: (res) => {
        this.profile = res;
        this.editForm = { fullName: res.fullName, phoneNo: res.phoneNo || '', image: res.image || '' };
        localStorage.setItem('fullName', res.fullName);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  saveProfile() {
    const uid = this.authService.getUid();
    if (!uid || !this.profile) return;
    this.editSubmitting = true;
    this.editError = '';
    const payload: any = {};
    if (this.editForm.fullName !== this.profile.fullName) payload.fullName = this.editForm.fullName;
    if (this.editForm.phoneNo !== this.profile.phoneNo) payload.phoneNo = this.editForm.phoneNo;
    if (this.editForm.image !== this.profile.image) payload.image = this.editForm.image;

    this.http.put<UserProfile>(`/api/users/${uid}`, payload).subscribe({
      next: (res) => {
        this.profile = res;
        localStorage.setItem('fullName', res.fullName);
        this.editSuccess = 'Profile updated successfully!';
        this.editSubmitting = false;
        setTimeout(() => { this.editSuccess = ''; }, 3000);
      },
      error: () => {
        this.editError = 'Failed to update profile.';
        this.editSubmitting = false;
      }
    });
  }

  changePassword() {
    if (!this.pwForm.oldPassword || !this.pwForm.newPassword) {
      this.pwError = 'All fields are required.'; return;
    }
    if (this.pwForm.newPassword !== this.pwForm.confirmPassword) {
      this.pwError = 'New passwords do not match.'; return;
    }
    if (this.pwForm.newPassword.length < 6) {
      this.pwError = 'Password must be at least 6 characters.'; return;
    }
    const uid = this.authService.getUid();
    this.pwSubmitting = true;
    this.pwError = '';
    this.http.put(`/api/users/${uid}/password`, {
      oldPassword: this.pwForm.oldPassword,
      newPassword: this.pwForm.newPassword
    }).subscribe({
      next: () => {
        this.pwSuccess = 'Password changed successfully!';
        this.pwForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
        this.pwSubmitting = false;
        setTimeout(() => { this.pwSuccess = ''; }, 3000);
      },
      error: () => {
        this.pwError = 'Current password is incorrect.';
        this.pwSubmitting = false;
      }
    });
  }

  deleteAccount() {
    const uid = this.authService.getUid();
    this.http.delete(`/api/users/${uid}`).subscribe({
      next: () => {
        this.authService.logout().subscribe(() => {
          this.router.navigate(['/']);
        });
      },
      error: () => {}
    });
  }
}
