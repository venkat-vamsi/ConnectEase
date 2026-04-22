import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-service-details',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './service-details.html',
  styleUrls: ['./service-details.css']
})
export class ServiceDetailsComponent {
  service = {
    name: 'Spice Garden Restaurant',
    location: 'Anna Nagar, Chennai',
    price: 150,
    trustScore: 89,
    reviews: 312,
    rating: 4.6
  };
}