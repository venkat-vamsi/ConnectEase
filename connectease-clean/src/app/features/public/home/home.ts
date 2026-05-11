import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface Category {
  id: string;
  name: string;
  description: string;
  count: string;
  art: 'pg' | 'food' | 'electric' | 'plumb' | 'clean' | 'laundry';
  tone: 'indigo' | 'amber' | 'emerald' | 'sky' | 'rose' | 'violet';
  matchKeys: string[];
}

interface ApiCategory { cid: string; name: string; }

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {
  private router = inject(Router);
  private http = inject(HttpClient);

  categories: Category[] = [
    { id: '', name: 'PGs & Hostels',  description: 'Verified, safe stays close to your work and study hubs.', count: '1,247+', art: 'pg',       tone: 'indigo', matchKeys: ['pg', 'hostel', 'accommodation', 'stay', 'room'] },
    { id: '', name: 'Food Services',  description: 'Home-style tiffins and trusted kitchens near you.',         count: '856+',   art: 'food',     tone: 'amber',  matchKeys: ['food', 'tiffin', 'kitchen', 'meal'] },
    { id: '', name: 'Electricians',   description: 'Background-checked pros for fast, reliable repairs.',        count: '432+',   art: 'electric', tone: 'sky',    matchKeys: ['electric', 'electrician'] },
    { id: '', name: 'Plumbers',       description: 'Same-day plumbing help with transparent pricing.',           count: '389+',   art: 'plumb',    tone: 'emerald',matchKeys: ['plumb', 'plumber'] },
    { id: '', name: 'Cleaners',       description: 'Deep cleaning and routine help, professionally done.',       count: '567+',   art: 'clean',    tone: 'violet', matchKeys: ['clean', 'cleaning', 'cleaner'] },
    { id: '', name: 'Laundry',        description: 'Pickup, wash and ironing — delivered on schedule.',          count: '312+',   art: 'laundry',  tone: 'rose',   matchKeys: ['laundry', 'wash', 'iron'] }
  ];

  ngOnInit() {
    this.http.get<ApiCategory[]>(`${environment.apiUrl}/categories`).subscribe({
      next: (apiCats) => {
        this.categories = this.categories.map(card => {
          const match = apiCats.find(api => {
            const lower = (api.name || '').toLowerCase();
            return card.matchKeys.some(k => lower.includes(k));
          });
          return match ? { ...card, id: match.cid } : card;
        });
      },
      error: () => {}
    });
  }

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

  stats = [
    { value: '3,800+', label: 'Verified vendors' },
    { value: '8,500+', label: 'Residents helped' },
    { value: '4.8',    label: 'Average rating' }
  ];

  steps = [
    { n: '01', title: 'Tell us what you need',     copy: 'Type a request or pick a category — PG, food, electrician, anything.' },
    { n: '02', title: 'Get curated matches',       copy: 'Our AI filters trusted vendors in your area, sorted by ratings and fit.' },
    { n: '03', title: 'Chat, book, and settle in', copy: 'Message vendors directly, confirm details, and start your new chapter.' }
  ];

  startAiSearch(query: string) {
    if (!query.trim()) return;
    this.router.navigate(['/ai-chat'], { queryParams: { q: query } });
  }
}
