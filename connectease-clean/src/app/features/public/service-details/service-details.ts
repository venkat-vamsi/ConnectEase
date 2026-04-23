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
  selectedImage: string | null = null;
  loading = true;
  error: string | null = null;

  ngOnInit() {
    const serviceId = this.route.snapshot.paramMap.get('id');
    if (serviceId) {
      this.loadServiceDetails(serviceId);
    } else {
      this.error = 'Service ID is missing.';
      this.loading = false;
    }
  }

  loadServiceDetails(serviceId: string) {
    this.loading = true;
    this.error = null;
    this.selectedImage = null;

    this.http.get<any>(`/api/services/${serviceId}`).subscribe({
      next: (data) => {
        // Ensure all properties exist with default values
        this.service = {
          sid: data.sid || '',
          name: data.name || 'Service Name Not Available',
          description: data.description || null,
          price: data.price || 0,
          totalViews: data.totalViews || 0,
          vendorName: data.vendorName || null,
          vendorEmail: data.vendorEmail || null,
          vendorPhone: data.vendorPhone || null,
          averageRating: data.averageRating || 0,
          reviews: data.reviews || [],
          images: data.images || [],
          city: data.city || null,
          area: data.area || null,
          fullAddress: data.fullAddress || null,
          categoryId: data.categoryId || null,
          categoryName: data.categoryName || null,
          features: data.features || [],
          verified: data.verified || false
        };
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