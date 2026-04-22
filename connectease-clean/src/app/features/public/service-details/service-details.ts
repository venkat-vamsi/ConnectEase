import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-service-details',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './service-details.html',
  styleUrls: ['./service-details.css']
})
export class ServiceDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  
  serviceId: number | null = null;
  
  // Temporary mock data. Later, you will fetch this from the backend using the serviceId
  service = {
    name: 'Loading...',
    location: 'Chennai',
    price: 0,
    trustScore: 0,
    reviews: 0,
    rating: 0
  };

  ngOnInit() {
    // 1. Grab the ID from the URL
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.serviceId = parseInt(idParam, 10);
      this.loadServiceData(this.serviceId);
    }
  }

  loadServiceData(id: number) {
    // TODO: Inject your actual DataService here and call this.http.get(`/api/services/${id}`)
    // For now, let's mock it so the UI doesn't break
    this.service = {
      name: `Service #${id}`,
      location: 'Adyar, Chennai',
      price: 8500,
      trustScore: 94,
      reviews: 127,
      rating: 4.8
    };
  }
}