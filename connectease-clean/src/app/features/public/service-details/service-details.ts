import { Component, inject, OnInit } from '@angular/core';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-service-details',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './service-details.html',
  styleUrls: ['./service-details.css']
})
export class ServiceDetailsComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);

  service: any = null;
  loading = true;
  error: string | null = null;

  ngOnInit() {
    const serviceId = this.route.snapshot.paramMap.get('id');
    this.loadServiceDetails(serviceId);
  }

  loadServiceDetails(serviceId: string) {
    this.loading = true;
    this.error = null;

    this.http.get<any>(`/api/services/${serviceId}`).subscribe({
      next: (data) => {
        this.service = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading service details:', err);
        this.error = 'Failed to load service details. Please try again.';
        this.loading = false;
      }
    });
  }
}