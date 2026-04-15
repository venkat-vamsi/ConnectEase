import { Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-listings',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './listings.html',
  styleUrls: ['./listings.css']
})
export class ListingsComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  
  services: any[] = [];
  categoryName = 'Services';

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const catId = params['categoryId'] || 'cat-1';
      this.http.get<any>(`/api/v1/listings/filter?categoryId=${catId}`).subscribe({
        next: (res) => this.services = res.content,
        error: () => this.loadMockData() // F1 Fallback
      });
    });
  }

  loadMockData() {
    this.services = [
      { sid: '1', name: 'Sunshine PG for Women', price: 8500, categoryName: 'PGs', trustScore: 94, verified: true, location: 'Adyar', rating: 4.8, reviews: 127 },
      { sid: '2', name: 'Royal Boys Hostel', price: 7000, categoryName: 'PGs', trustScore: 88, verified: true, location: 'Velachery', rating: 4.5, reviews: 89 }
    ];
  }
}