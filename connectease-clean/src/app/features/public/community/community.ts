// import { Component, inject, OnInit } from '@angular/core';
// import { HttpClient } from '@angular/common/http';
// import { DatePipe } from '@angular/common';
// import { FormsModule } from '@angular/forms';

// interface Post {
//   postId: string;
//   title: string;
//   description: string;
//   category: string;
//   authorFullName: string;
//   time: string;
//   rating: number;
//   imageUrl?: string;
//   likes: number;
//   comments: number;
//   helpful: number;
// }

// interface NewPost {
//   title: string;
//   description: string;
//   category: string;
//   rating: number;
// }

// @Component({
//   selector: 'app-community',
//   standalone: true,
//   imports: [DatePipe, FormsModule],
//   templateUrl: './community.html',
//   styleUrls: ['./community.css']
// })
// export class CommunityComponent implements OnInit {
//   private http = inject(HttpClient);

//   posts: Post[] = [];
//   filteredPosts: Post[] = [];
//   categories = ['All Posts', 'PG/Hostel', 'Food Service', 'Electrician', 'Plumber', 'Cleaners', 'Laundry'];
//   postCategories = ['PG/Hostel', 'Food Service', 'Electrician', 'Plumber', 'Cleaners', 'Laundry'];
//   activeCategory = 'All Posts';

//   // Modal state
//   showModal = false;
//   submitting = false;
//   showToast = false;
//   imagePreview: string | null = null;
//   hoverRating = 0;

//   newPost: NewPost = { title: '', description: '', category: '', rating: 0 };

//   ngOnInit() {
//     this.http.get<Post[]>('/api/community').subscribe({
//       next: (res) => { this.posts = res; this.applyFilter(); },
//       error: () => {
//         this.posts = [
//           {
//             postId: '1', title: 'Amazing PG experience in Velachery!',
//             description: 'Staying at Royal Boys Hostel has been fantastic. The facilities are clean, the food is great, and the location is super convenient for IT folks.',
//             category: 'PG/Hostel', authorFullName: 'Arjun Mehta',
//             time: '2026-03-10', rating: 5, likes: 24, comments: 8, helpful: 38
//           },
//           {
//             postId: '2', title: 'Reliable electrician in Anna Nagar',
//             description: 'Quick Fix Electricians saved the day when my flat had a wiring issue. Fast response, transparent pricing, and very professional.',
//             category: 'Electrician', authorFullName: 'Priya Sharma',
//             time: '2026-02-20', rating: 4, likes: 16, comments: 3, helpful: 22
//           },
//           {
//             postId: '3', title: 'Best tiffin service near OMR',
//             description: 'Homely Bites delivers exactly what it says — homecooked Tamil meals right to your door. Highly recommended for anyone new to Chennai.',
//             category: 'Food Service', authorFullName: 'Rohit Kumar',
//             time: '2026-01-15', rating: 5, likes: 31, comments: 12, helpful: 45
//           }
//         ];
//         this.applyFilter();
//       }
//     });
//   }

//   filter(cat: string) {
//     this.activeCategory = cat;
//     this.applyFilter();
//   }

//   applyFilter() {
//     this.filteredPosts = this.activeCategory === 'All Posts'
//       ? [...this.posts]
//       : this.posts.filter(p => p.category === this.activeCategory);
//   }

//   openModal() { this.showModal = true; document.body.style.overflow = 'hidden'; }

//   closeModal() {
//     this.showModal = false;
//     document.body.style.overflow = '';
//     this.newPost = { title: '', description: '', category: '', rating: 0 };
//     this.imagePreview = null;
//     this.hoverRating = 0;
//   }

//   closeModalOnBackdrop(event: MouseEvent) { this.closeModal(); }

//   getCategoryIcon(cat: string): string {
//     const icons: Record<string, string> = {
//       'PG/Hostel': '🏠', 'Food Service': '🍱', 'Electrician': '⚡',
//       'Plumber': '🔧', 'Cleaners': '🧹', 'Laundry': '👕'
//     };
//     return icons[cat] || '📌';
//   }

//   getRatingLabel(r: number): string {
//     return ['', 'Poor', 'Fair', 'Good', 'Great', 'Excellent!'][r] || '';
//   }

//   getStars(rating: number = 0): string[] {
//     return Array.from({ length: 5 }, (_, i) => i < rating ? 'full' : 'empty');
//   }

//   onImageSelected(event: Event) {
//     const file = (event.target as HTMLInputElement).files?.[0];
//     if (!file) return;
//     const reader = new FileReader();
//     reader.onload = (e) => { this.imagePreview = e.target?.result as string; };
//     reader.readAsDataURL(file);
//   }

//   removeImage(event: MouseEvent) {
//     event.stopPropagation();
//     this.imagePreview = null;
//   }

//   isFormValid(): boolean {
//     return this.newPost.title.trim().length > 0
//       && this.newPost.description.trim().length > 0
//       && this.newPost.category.length > 0
//       && this.newPost.rating > 0;
//   }

//   submitPost() {
//     if (!this.isFormValid()) return;
//     this.submitting = true;

//     // Build the new post locally immediately (no API wait)
//     const newEntry: Post = {
//       postId: Date.now().toString(),
//       title: this.newPost.title,
//       description: this.newPost.description,
//       category: this.newPost.category,
//       rating: this.newPost.rating,
//       imageUrl: this.imagePreview || undefined,
//       authorFullName: 'You',
//       time: new Date().toISOString(),
//       likes: 0,
//       comments: 0,
//       helpful: 0
//     };

//     this.posts.unshift(newEntry);
//     this.applyFilter();
//     this.finishSubmit();

//     // Also try to sync with backend in the background (optional)
//     const payload = { ...this.newPost, image: this.imagePreview || undefined };
//     this.http.post<Post>('/api/community', payload).subscribe({
//       next: (created) => {
//         // Replace local entry with server response if successful
//         const idx = this.posts.findIndex(p => p.postId === newEntry.postId);
//         if (idx !== -1) { this.posts[idx] = created; this.applyFilter(); }
//       },
//       error: () => { /* local entry already shown, nothing to do */ }
//     });
//   }

//   private finishSubmit() {
//     this.submitting = false;
//     this.closeModal();
//     this.showToast = true;
//     setTimeout(() => { this.showToast = false; }, 3500);
//   }

//   markHelpful(post: Post) { post.helpful++; }
// }

import { Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Post {
  postId?: string;
  title: string;
  description: string;
  category: string;
  authorFullName: string;
  time: string;
  rating: number;
  image?: string; 
  likes: number;
  comments: number;
  helpful: number;
}

interface NewPost {
  title: string;
  description: string;
  category: string;
  rating: number;
}

@Component({
  selector: 'app-community',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule],
  templateUrl: './community.html',
  styleUrls: ['./community.css']
})
export class CommunityComponent implements OnInit {
  private http = inject(HttpClient);
  private API_URL = 'http://localhost:8081/api/community'; 

  posts: Post[] = [];
  filteredPosts: Post[] = [];
  categories = ['All Posts', 'PG/Hostel', 'Food Service', 'Electrician', 'Plumber', 'Cleaners', 'Laundry'];
  postCategories = ['PG/Hostel', 'Food Service', 'Electrician', 'Plumber', 'Cleaners', 'Laundry'];
  activeCategory = 'All Posts';

  // Modal & UI State
  showModal = false;
  submitting = false;
  showToast = false;
  imageUrlInput: string = ''; 
  hoverRating = 0;
  newPost: NewPost = { title: '', description: '', category: '', rating: 0 };

  ngOnInit() {
    this.fetchPosts();
  }

  fetchPosts() {
    this.http.get<Post[]>(this.API_URL).subscribe({
      next: (res) => {
        this.posts = res;
        this.applyFilter();
      },
      error: (err) => console.error('Backend connection error:', err)
    });
  }

  filter(cat: string) {
    this.activeCategory = cat;
    this.applyFilter();
  }

  applyFilter() {
    this.filteredPosts = this.activeCategory === 'All Posts'
      ? [...this.posts]
      : this.posts.filter(p => 
          p.category?.toLowerCase().trim() === this.activeCategory.toLowerCase().trim()
        );
  }

  openModal() {
    this.showModal = true;
    document.body.style.overflow = 'hidden';
  }

  closeModal() {
    this.showModal = false;
    document.body.style.overflow = '';
    this.resetForm();
  }

  resetForm() {
    this.newPost = { title: '', description: '', category: '', rating: 0 };
    this.imageUrlInput = '';
    this.hoverRating = 0;
  }

  submitPost() {
    if (!this.isFormValid()) return;
    this.submitting = true;

    const payload = {
      title: this.newPost.title,
      description: this.newPost.description,
      category: this.newPost.category,
      rating: this.newPost.rating,
      image: this.imageUrlInput // Sends the URL string to Spring Boot
    };

    this.http.post<Post>(this.API_URL, payload).subscribe({
      next: (savedPost) => {
        this.posts.unshift(savedPost);
        this.applyFilter();
        this.finishSubmit();
      },
      error: (err) => {
        console.error('Submission failed:', err);
        this.submitting = false;
        alert('Could not save post to database.');
      }
    });
  }

  private finishSubmit() {
    this.submitting = false;
    this.closeModal();
    this.showToast = true;
    setTimeout(() => { this.showToast = false; }, 3500);
  }

  isFormValid(): boolean {
    return this.newPost.title.trim().length > 0 && 
           this.newPost.description.trim().length > 0 &&
           this.newPost.category !== '' && 
           this.newPost.rating > 0;
  }

  // Helper Methods
  getCategoryIcon(cat: string): string {
    const icons: any = { 'PG/Hostel': '🏠', 'Food Service': '🍱', 'Electrician': '⚡', 'Plumber': '🔧', 'Cleaners': '🧹', 'Laundry': '👕' };
    return icons[cat] || '📌';
  }

  getRatingLabel(r: number): string {
    return ['', 'Poor', 'Fair', 'Good', 'Great', 'Excellent!'][r] || '';
  }

  getStars(rating: number = 0): string[] {
    return Array.from({ length: 5 }, (_, i) => i < rating ? 'full' : 'empty');
  }

  markHelpful(post: Post) {
    post.helpful = (post.helpful || 0) + 1;
  }
}