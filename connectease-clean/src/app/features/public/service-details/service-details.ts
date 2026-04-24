import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth';

interface ServiceImage { url: string; isPrimary: boolean; }
interface Review { rid?: string; userName: string; profileImage: string | null; review: string; score: number; }
interface ServiceDetail {
  sid: string;
  name: string;
  description: string;
  price: number;
  totalViews: number;
  vendorName: string;
  vendorId: string | null;
  categoryName: string | null;
  averageRating: number;
  reviews: Review[];
  images: ServiceImage[];
  features: string[];
}

@Component({
  selector: 'app-service-details',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './service-details.html',
  styleUrls: ['./service-details.css']
})
export class ServiceDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  authService = inject(AuthService);

  serviceId = '';
  service: ServiceDetail | null = null;
  loading = true;
  error = '';

  activeImageIndex = 0;
  newReview = { review: '', score: 0 };
  hoverScore = 0;
  submittingReview = false;
  reviewSuccess = false;
  reviewError = '';

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) { this.serviceId = id; this.loadService(); }
  }

  loadService() {
    this.loading = true;
    this.http.get<ServiceDetail>(`/api/services/${this.serviceId}`).subscribe({
      next: (res) => {
        this.service = res;
        this.loading = false;
        this.http.post(`/api/services/${this.serviceId}/view`, {}).subscribe();
      },
      error: () => { this.error = 'Service not found.'; this.loading = false; }
    });
  }

  get primaryImage(): string {
    if (!this.service?.images?.length) return 'assets/placeholder.jpg';
    return this.service.images[this.activeImageIndex]?.url || this.service.images[0]?.url || 'assets/placeholder.jpg';
  }

  setScore(s: number) { this.newReview.score = s; }

  getStars(rating: number): ('full' | 'half' | 'empty')[] {
    return Array.from({ length: 5 }, (_, i) => {
      if (i < Math.floor(rating)) return 'full';
      if (i < rating) return 'half';
      return 'empty';
    });
  }

  submitReview() {
    if (!this.authService.getRole()) { this.router.navigate(['/login']); return; }
    if (!this.newReview.score || !this.newReview.review.trim()) return;
    this.submittingReview = true;
    this.reviewError = '';
    this.http.post(`/api/services/${this.serviceId}/reviews`, this.newReview, { responseType: 'text' }).subscribe({
      next: () => {
        this.reviewSuccess = true;
        this.newReview = { review: '', score: 0 };
        this.loadService();
        this.submittingReview = false;
        setTimeout(() => { this.reviewSuccess = false; }, 3000);
      },
      error: () => {
        this.reviewError = 'Failed to submit review. Please try again.';
        this.submittingReview = false;
        setTimeout(() => { this.reviewError = ''; }, 3000);
      }
    });
  }

  deleteReview(rid: string) {
    this.http.delete(`/api/ratings/${rid}`).subscribe({
      next: () => { this.loadService(); },
      error: () => {}
    });
  }

  get isOwnService(): boolean {
    return !!this.service?.vendorId && this.service.vendorId === this.authService.getUid();
  }

  get priceLabel(): string {
    const cat = (this.service?.categoryName || '').toLowerCase();
    if (cat.includes('pg') || cat.includes('hostel') || cat.includes('room')) return 'per room/month';
    return 'per service';
  }

  isMyReview(rev: Review): boolean {
    const name = this.authService.getFullName();
    return !!name && rev.userName === name;
  }

  startChat() {
    if (!this.authService.getRole()) { this.router.navigate(['/login']); return; }
    if (this.service?.vendorId) {
      this.router.navigate(['/chats'], { queryParams: { with: this.service.vendorId } });
    }
  }
}
