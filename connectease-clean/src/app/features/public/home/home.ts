import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent {
  private router = inject(Router);
  categories = [
    { id: 'cat-1', name: 'PGs/Hostels', icon: '🏠', count: '1247+', bg: '#3B82F6' },
    { id: 'cat-2', name: 'Food Services', icon: '🍽️', count: '856+', bg: '#F97316' },
    { id: 'cat-3', name: 'Electricians', icon: '⚡', count: '432+', bg: '#EAB308' },
    { id: 'cat-4', name: 'Plumbers', icon: '🔧', count: '389+', bg: '#06B6D4' },
    { id: 'cat-5', name: 'Cleaners', icon: '🧹', count: '567+', bg: '#22C55E' },
    { id: 'cat-6', name: 'Laundry', icon: '👔', count: '312+', bg: '#A855F7' }
  ];

  startAiSearch(query: string) {
    if (!query.trim()) return;
    this.router.navigate(['/ai-chat'], { queryParams: { q: query } });
  }
}