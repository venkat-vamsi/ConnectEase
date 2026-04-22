import { Component, inject, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router'; // Added Router
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common'; // Required for @if and @for

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
  private router = inject(Router); // Injected Router

  services: any[] = [];
  categoryName = 'All Services';
  selectedCategoryId = ''; // Tracks active tab for UI highlight

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
    categoryId: '',
    city: '',
    area: '',
    minPrice: null as number | null,
    maxPrice: null as number | null,
    minRating: null as number | null,
    sortType: 'newest',
    page: 0,
    size: 10
  };

  ngOnInit() {
    // Listen to query parameters to handle external navigation or refreshes
    this.route.queryParams.subscribe(params => {
      this.filters.categoryId = params['categoryId'] || '';
      this.selectedCategoryId = this.filters.categoryId;
      this.fetchFilteredResults();
    });
  }

  // Navbar specific function
  filterByCategory(id: string) {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { categoryId: id || null }, // Use null to remove the param for 'All Services'
      queryParamsHandling: 'merge' // Keeps city/area/price filters intact
    });
  }

  setRating(rating: number | null) {
    this.filters.minRating = rating;
    this.fetchFilteredResults();
  }

  fetchFilteredResults() {
    let params = new HttpParams();
    
    // Add all active filters to request
    Object.entries(this.filters).forEach(([key, value]) => {
      if (value !== null && value !== '') {
        params = params.set(key, value.toString());
      }
    });

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