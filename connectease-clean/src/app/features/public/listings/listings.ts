import { Component, inject, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../environments/environment';

interface Listing {
  sid: string;
  name: string;
  description: string;
  price: number;
  categoryName: string;
  city: string;
  area: string;
  primaryImageUrl: string | null;
  averageRating?: number;
  totalViews?: number;
}

interface Category {
  cid: string;
  name: string;
}

@Component({
  selector: 'app-listings',
  standalone: true,
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './listings.html',
  styleUrls: ['./listings.css']
})
export class ListingsComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  services: Listing[] = [];
  totalElements = 0;
  loading = false;
  selectedCategoryId = '';

  cities: string[] = [];
  areas: string[] = [];

  private readonly iconMap: Record<string, string> = {
    pg: '🏠', food: '🍽️', electrician: '⚡',
    electric: '⚡', plumbing: '🔧', clean: '🧹', laundry: '👔'
  };

  categories: { id: string; name: string; icon: string }[] = [
    { id: '', name: 'All Services', icon: '🏘️' }
  ];

  filters = {
    categoryId: '',
    city: '',
    area: '',
    minPrice: null as number | null,
    maxPrice: null as number | null,
    minRating: null as number | null,
    sortType: 'rating',
    page: 0,
    size: 12
  };

  vendorMode = false;
  vendorIdFilter = '';

  ngOnInit() {
    this.loadCities();
    this.loadCategories();
    this.route.queryParams.subscribe(params => {
      const vendorId = params['vendor'] || '';
      if (vendorId) {
        this.vendorMode = true;
        this.vendorIdFilter = vendorId;
        this.fetchVendorServices(vendorId);
      } else {
        this.vendorMode = false;
        this.vendorIdFilter = '';
        this.filters.categoryId = params['categoryId'] || '';
        this.selectedCategoryId = this.filters.categoryId;
        this.fetchFilteredResults();
      }
    });
  }

  fetchVendorServices(vendorId: string) {
    this.loading = true;
    this.http.get<Listing[]>(`${environment.apiUrl}/services/vendor/${vendorId}`).subscribe({
      next: (res) => {
        this.services = res;
        this.totalElements = res.length;
        this.loading = false;
      },
      error: () => {
        this.services = [];
        this.loading = false;
      }
    });
  }

  getPriceUnit(categoryName: string): string {
    const lower = (categoryName || '').toLowerCase();
    if (lower.includes('pg') || lower.includes('hostel') || lower.includes('room')) return '/room·month';
    return '/service';
  }

  loadCategories() {
    this.http.get<Category[]>(`${environment.apiUrl}/categories`).subscribe({
      next: (res) => {
        this.categories = [
          { id: '', name: 'All Services', icon: '🏘️' },
          ...res.map(cat => ({ id: cat.cid, name: cat.name, icon: this.getCategoryIcon(cat.name) }))
        ];
      },
      error: () => {}
    });
  }

  private getCategoryIcon(name: string): string {
    const lower = name.toLowerCase();
    for (const [key, icon] of Object.entries(this.iconMap)) {
      if (lower.includes(key)) return icon;
    }
    return this.iconMap['default'];
  }

  loadCities() {
    this.http.get<string[]>(`${environment.apiUrl}/locations/cities`).subscribe({
      next: (res) => { this.cities = res; },
      error: () => { this.cities = ['Hyderabad', 'Chennai', 'Bangalore', 'Mumbai', 'Pune']; }
    });
  }

  onCityChange() {
    this.filters.area = '';
    this.areas = [];
    if (this.filters.city) {
      this.http.get<string[]>(`${environment.apiUrl}/locations/cities/${this.filters.city}/areas`).subscribe({
        next: (res) => { this.areas = res; },
        error: () => { this.areas = []; }
      });
    }
    this.fetchFilteredResults();
  }

  filterByCategory(id: string) {
    this.filters.page = 0;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { categoryId: id || null },
      queryParamsHandling: 'merge'
    });
  }

  setRating(rating: number | null) {
    this.filters.minRating = rating;
    this.filters.page = 0;
    this.fetchFilteredResults();
  }

  applyFilter() {
    this.filters.page = 0;
    this.fetchFilteredResults();
  }

  get totalPages(): number {
    return Math.ceil(this.totalElements / this.filters.size);
  }

  getPageNumbers(): number[] {
    const total = this.totalPages;
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);
    const cur = this.filters.page;
    const pages: number[] = [0];
    const start = Math.max(1, cur - 2);
    const end = Math.min(total - 2, cur + 2);
    if (start > 1) pages.push(-1);
    for (let i = start; i <= end; i++) pages.push(i);
    if (end < total - 2) pages.push(-1);
    pages.push(total - 1);
    return pages;
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages || page === this.filters.page) return;
    this.filters.page = page;
    this.fetchFilteredResults();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  fetchFilteredResults() {
    this.loading = true;
    let params = new HttpParams();
    Object.entries(this.filters).forEach(([key, value]) => {
      if (value !== null && value !== '') params = params.set(key, value.toString());
    });

    this.http.get<any>(`${environment.apiUrl}/v1/listings/filter`, { params }).subscribe({
      next: (res) => {
        this.services = res.content || [];
        this.totalElements = res.totalElements || 0;
        this.loading = false;
      },
      error: () => {
        this.services = [];
        this.loading = false;
      }
    });
  }

  getStarArray(rating: number = 0): ('full' | 'empty')[] {
    return Array.from({ length: 5 }, (_, i) => i < Math.round(rating) ? 'full' : 'empty');
  }
}
