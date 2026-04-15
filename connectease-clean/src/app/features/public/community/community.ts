import { Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-community',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './community.html',
  styleUrls: ['./community.css']
})
export class CommunityComponent implements OnInit {
  private http = inject(HttpClient);
  posts: any[] = [];
  categories = ['All Posts', 'PG/Hostel', 'Food Service', 'Electrician', 'Plumber'];
  activeCategory = 'All Posts';

  ngOnInit() {
    this.http.get<any[]>('/api/community/posts').subscribe({
      next: (res) => this.posts = res,
      error: () => {
        this.posts = [{
          postId: '1', title: 'Amazing PG experience!', description: 'Staying at Royal Boys Hostel has been fantastic.', authorFullName: 'Arjun Mehta', time: new Date().toISOString()
        }];
      }
    });
  }

  filter(cat: string) { this.activeCategory = cat; }
}