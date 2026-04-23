import { Component, inject, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

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

  services: any[] = [];
  categoryName = 'All Services';
  selectedCategoryId = '';
  
  // Dynamic filter data
  cities: string[] = [];
  areas: string[] = [];
  categoryFilters: any = null;
  loadingFilters = false;

  // Professional Category Data
  categories = [
    { id: 'pg', name: 'PGs/Hostels', icon: '🏠', count: '1247' },
    { id: 'food', name: 'Food Services', icon: '🍽️', count: '856' },
    { id: 'electrician', name: 'Electricians', icon: '⚡', count: '432' },
    { id: 'plumber', name: 'Plumbers', icon: '🔧', count: '389' },
    { id: 'cleaner', name: 'Cleaners', icon: '🧹', count: '567' },
    { id: 'laundry', name: 'Laundry', icon: '👔', count: '312' }
  ];

  filters = {
    keyword: '',
    categoryId: '',
    city: '',
    area: '',
    minPrice: null as number | null,
    maxPrice: null as number | null,
    minRating: null as number | null,
    sortType: 'newest',
    page: 0,
    size: 12,
    // Category-specific filters
    categorySpecific: {} as any
  };

  ngOnInit() {
    this.loadCities();
    
    // Listen to query parameters
    this.route.queryParams.subscribe(params => {
      this.filters.categoryId = params['categoryId'] || '';
      this.selectedCategoryId = this.filters.categoryId;
      
      // Load category-specific filters when category changes
      if (this.filters.categoryId) {
        this.loadCategoryFilters(this.filters.categoryId);
      } else {
        this.categoryFilters = null;
      }
      
      this.fetchFilteredResults();
    });
  }
  
  // Load cities from backend
  loadCities() {
    this.http.get<string[]>('/api/v1/locations/cities').subscribe({
      next: (data) => {
        this.cities = data;
      },
      error: (err) => {
        console.error('Error loading cities:', err);
        // Fallback to hardcoded values
        this.cities = ['Chennai', 'Bangalore'];
      }
    });
  }
  
  // Load areas when city changes
  onCityChange() {
    this.filters.area = '';
    this.areas = [];
    
    if (this.filters.city) {
      this.http.get<string[]>('/api/v1/locations/areas', {
        params: { city: this.filters.city }
      }).subscribe({
        next: (data) => {
          this.areas = data;
        },
        error: (err) => {
          console.error('Error loading areas:', err);
          this.areas = [];
        }
      });
    }
    
    this.fetchFilteredResults();
  }
  
  // Load category-specific filters
  loadCategoryFilters(categoryId: string) {
    this.loadingFilters = true;
    this.http.get<any>(`/api/v1/listings/category-filters/${categoryId}`).subscribe({
      next: (data) => {
        this.categoryFilters = data;
        
        // Initialize category-specific filter values
        this.filters.categorySpecific = {};
        if (data.filters && data.filters.length > 0) {
          data.filters.forEach((filter: any) => {
            this.filters.categorySpecific[filter.key] = null;
          });
        }
        
        // Update price range from category config
        if (data.priceRange) {
          this.filters.minPrice = null;
          this.filters.maxPrice = null;
        }
        
        this.loadingFilters = false;
      },
      error: (err) => {
        console.error('Error loading category filters:', err);
        this.categoryFilters = null;
        this.loadingFilters = false;
      }
    });
  }

  // Navbar specific function
  filterByCategory(id: string) {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { categoryId: id || null },
      queryParamsHandling: 'merge'
    });
  }

  // Search function
  onSearch() {
    this.filters.page = 0;
    this.fetchFilteredResults();
  }

  setRating(rating: number | null) {
    this.filters.minRating = rating;
    this.fetchFilteredResults();
  }
  
  // Handle category-specific filter change
  onCategoryFilterChange() {
    this.filters.page = 0;
    this.fetchFilteredResults();
  }
  
  // Handle checkbox changes for category filters
  onCheckboxChange(filterKey: string, value: string, event: any) {
    if (!this.filters.categorySpecific[filterKey]) {
      this.filters.categorySpecific[filterKey] = [];
    }
    
    const index = this.filters.categorySpecific[filterKey].indexOf(value);
    if (event.target.checked) {
      if (index === -1) {
        this.filters.categorySpecific[filterKey].push(value);
      }
    } else {
      if (index > -1) {
        this.filters.categorySpecific[filterKey].splice(index, 1);
      }
    }
    
    this.onCategoryFilterChange();
  }

  fetchFilteredResults() {
    let params = new HttpParams();
    
    // Add basic filters
    Object.entries(this.filters).forEach(([key, value]) => {
      if (key === 'categorySpecific') return; // Skip category-specific
      if (value !== null && value !== '') {
        params = params.set(key, value.toString());
      }
    });
    
    // Add category-specific filters
    if (this.filters.categorySpecific) {
      Object.entries(this.filters.categorySpecific).forEach(([key, value]) => {
        if (value !== null && value !== '') {
          params = params.set(key, value.toString());
        }
      });
    }

    this.http.get<any>('/api/v1/listings/filter', { params }).subscribe({
      next: (res) => {
        this.services = res.content;
        // Set Header Title based on category
        const currentCat = this.categories.find(c => c.id === this.filters.categoryId);
        this.categoryName = currentCat ? currentCat.name : 'All Services';
      },
      error: (err) => {
        console.error('Backend error:', err);
        this.loadMockData();
      }
    });
  }

  loadMockData() {
    this.services = [
      { sid: '1', name: 'Sunshine PG for Women', price: 8500, categoryName: 'PGs', trustScore: 94, verified: true, area: 'Adyar', city: 'Chennai', rating: 4.8, reviews: 127 },
      { sid: '2', name: 'Royal Boys Hostel', price: 7000, categoryName: 'Hostels', trustScore: 88, verified: true, area: 'Velachery', city: 'Chennai', rating: 4.5, reviews: 89 }
    ];
  }
}