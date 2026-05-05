import { Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { environment } from '../../../../environments/environment';

interface DashboardStats {
  vendorName: string;
  activeListings: number;
  totalViews: number;
  totalReviews: number;
  averageRating: number;
}

interface Listing {
  sid: string;
  name: string;
  description: string;
  price: number;
  categoryName: string;
  city: string;
  area: string;
  primaryImageUrl: string | null;
  active?: boolean;
}

interface Category { cid: string; name: string; }

interface ServiceForm {
  name: string;
  description: string;
  price: number | null;
  categoryId: string;
  address: string;
  city: string;
  area: string;
  latitude: number | null;
  longitude: number | null;
  features: string[];
  images: { url: string; isPrimary: boolean }[];
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  authService = inject(AuthService);

  activeTab: 'dashboard' | 'listings' | 'add' | 'edit' = 'dashboard';

  stats: DashboardStats | null = null;
  statsLoading = true;

  listings: Listing[] = [];
  listingsLoading = false;

  categories: Category[] = [];
  editingSid = '';

  serviceForm: ServiceForm = this.emptyForm();
  formSubmitting = false;
  formError = '';
  formSuccess = '';

  newFeature = '';
  newImageUrl = '';
  deleteConfirmSid = '';
  Math = Math;

  ngOnInit() {
    this.loadDashboard();
    this.loadCategories();
  }

  private emptyForm(): ServiceForm {
    return {
      name: '', description: '', price: null, categoryId: '',
      address: '', city: '', area: '', latitude: null, longitude: null,
      features: [], images: []
    };
  }

  loadDashboard() {
    this.statsLoading = true;
    this.http.get<DashboardStats>(`${environment.apiUrl}/vendor/dashboard`).subscribe({
      next: (res) => { this.stats = res; this.statsLoading = false; },
      error: () => { this.statsLoading = false; }
    });
  }

  loadCategories() {
    this.http.get<Category[]>(`${environment.apiUrl}/categories`).subscribe({
      next: (res) => { this.categories = res; },
      error: () => {
        this.categories = [
          { cid: '', name: '— Select category —' }
        ];
      }
    });
  }

  switchTab(tab: 'dashboard' | 'listings' | 'add' | 'edit') {
    this.activeTab = tab;
    this.formError = '';
    this.formSuccess = '';
    if (tab === 'listings') this.loadListings();
    if (tab === 'add') { this.serviceForm = this.emptyForm(); this.editingSid = ''; }
  }

  loadListings() {
    this.listingsLoading = true;
    this.http.get<Listing[]>(`${environment.apiUrl}/vendor/services`).subscribe({
      next: (res) => { this.listings = res; this.listingsLoading = false; },
      error: () => { this.listingsLoading = false; }
    });
  }

  openEditForm(listing: Listing) {
    this.editingSid = listing.sid;
    this.serviceForm = {
      name: listing.name,
      description: listing.description,
      price: listing.price,
      categoryId: '',
      address: '',
      city: listing.city || '',
      area: listing.area || '',
      latitude: null,
      longitude: null,
      features: [],
      images: listing.primaryImageUrl
        ? [{ url: listing.primaryImageUrl, isPrimary: true }]
        : []
    };
    this.activeTab = 'edit';
    this.formError = '';
    this.formSuccess = '';
  }

  addFeature() {
    const f = this.newFeature.trim();
    if (f && !this.serviceForm.features.includes(f)) {
      this.serviceForm.features.push(f);
    }
    this.newFeature = '';
  }

  removeFeature(i: number) { this.serviceForm.features.splice(i, 1); }

  addImage() {
    const url = this.newImageUrl.trim();
    if (url) {
      const isPrimary = this.serviceForm.images.length === 0;
      this.serviceForm.images.push({ url, isPrimary });
    }
    this.newImageUrl = '';
  }

  removeImage(i: number) {
    this.serviceForm.images.splice(i, 1);
    if (this.serviceForm.images.length > 0) this.serviceForm.images[0].isPrimary = true;
  }

  setPrimary(i: number) {
    this.serviceForm.images.forEach((img, idx) => img.isPrimary = idx === i);
  }

  buildPayload() {
    const f = this.serviceForm;
    const PLACEHOLDER = 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600&q=80';
    const images = f.images.length > 0
      ? f.images
      : [{ url: PLACEHOLDER, isPrimary: true }];
    return {
      name: f.name,
      description: f.description,
      price: f.price,
      active: true,
      category: f.categoryId ? { cid: f.categoryId } : undefined,
      location: {
        address: f.address || '',
        city: f.city,
        area: f.area,
        latitude: f.latitude,
        longitude: f.longitude
      },
      features: f.features.map(n => ({ name: n })),
      images
    };
  }

  submitService() {
    if (!this.serviceForm.name.trim() || !this.serviceForm.price) {
      this.formError = 'Name and price are required.';
      return;
    }
    this.formSubmitting = true;
    this.formError = '';
    const payload = this.buildPayload();

    if (this.activeTab === 'edit' && this.editingSid) {
      this.http.put(`${environment.apiUrl}/vendor/service/${this.editingSid}`, payload).subscribe({
        next: () => {
          this.formSuccess = 'Service updated successfully!';
          this.formSubmitting = false;
          setTimeout(() => this.switchTab('listings'), 1500);
        },
        error: () => { this.formError = 'Update failed. Please try again.'; this.formSubmitting = false; }
      });
    } else {
      this.http.post(`${environment.apiUrl}/vendor/service/add`, payload).subscribe({
        next: () => {
          this.formSuccess = 'Service created successfully!';
          this.formSubmitting = false;
          setTimeout(() => this.switchTab('listings'), 1500);
        },
        error: () => { this.formError = 'Creation failed. Please try again.'; this.formSubmitting = false; }
      });
    }
  }

  toggleStatus(listing: Listing) {
    this.http.patch(`${environment.apiUrl}/vendor/service/${listing.sid}/status`, {}).subscribe({
      next: (res: any) => {
        listing.active = !listing.active;
      },
      error: () => {}
    });
  }

  confirmDelete(sid: string) { this.deleteConfirmSid = sid; }
  cancelDelete() { this.deleteConfirmSid = ''; }

  deleteService(sid: string) {
    this.http.delete(`${environment.apiUrl}/vendor/service/${sid}`).subscribe({
      next: () => {
        this.listings = this.listings.filter(l => l.sid !== sid);
        this.deleteConfirmSid = '';
        this.loadDashboard();
      },
      error: () => { this.deleteConfirmSid = ''; }
    });
  }
}
