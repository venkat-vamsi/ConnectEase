import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent {
  stats = {
    trustScore: 94,
    totalViews: 1247,
    activeListings: 3,
    totalReviews: 127,
    responseRate: 98
  };
}