import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

interface Category {
  id: string;
  name: string;
  description: string;
  count: string;
  art: 'pg' | 'food' | 'electric' | 'plumb' | 'clean' | 'laundry';
  tone: 'indigo' | 'amber' | 'emerald' | 'sky' | 'rose' | 'violet';
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent {
  private router = inject(Router);

  categories: Category[] = [
    { id: 'cat-1', name: 'PGs & Hostels',  description: 'Verified, safe stays close to your work and study hubs.', count: '1,247+', art: 'pg',       tone: 'indigo' },
    { id: 'cat-2', name: 'Food Services',  description: 'Home-style tiffins and trusted kitchens near you.',         count: '856+',   art: 'food',     tone: 'amber'  },
    { id: 'cat-3', name: 'Electricians',   description: 'Background-checked pros for fast, reliable repairs.',        count: '432+',   art: 'electric', tone: 'sky'    },
    { id: 'cat-4', name: 'Plumbers',       description: 'Same-day plumbing help with transparent pricing.',           count: '389+',   art: 'plumb',    tone: 'emerald'},
    { id: 'cat-5', name: 'Cleaners',       description: 'Deep cleaning and routine help, professionally done.',       count: '567+',   art: 'clean',    tone: 'violet' },
    { id: 'cat-6', name: 'Laundry',        description: 'Pickup, wash and ironing — delivered on schedule.',          count: '312+',   art: 'laundry',  tone: 'rose'   }
  ];

  highlights = [
    {
      key: 'verified',
      title: 'Verified & trusted',
      copy: 'Every vendor is background-checked, document-verified and rated by real residents.'
    },
    {
      key: 'ai',
      title: 'AI-guided discovery',
      copy: 'Describe what you need in plain language — our assistant matches you in seconds.'
    },
    {
      key: 'community',
      title: 'Real community stories',
      copy: 'Read first-hand experiences from people who already moved to Chennai.'
    }
  ];

  startAiSearch(query: string) {
    if (!query.trim()) return;
    this.router.navigate(['/ai-chat'], { queryParams: { q: query } });
  }
}
