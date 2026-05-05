import { Component, inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe, CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { environment } from '../../../../environments/environment';

interface Post {
  postId?: string;
  title: string;
  description: string;
  image?: string;
  category?: string;
  authorFullName: string;
  time: string;
}

interface ApiCategory { cid: string; name: string; }

@Component({
  selector: 'app-community',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, RouterLink],
  templateUrl: './community.html',
  styleUrls: ['./community.css']
})
export class CommunityComponent implements OnInit {
  private http = inject(HttpClient);
  authService = inject(AuthService);

  allPosts: Post[] = [];
  posts: Post[] = [];
  loading = false;
  activeCategory = '';

  communityCategories: { id: string; label: string }[] = [
    { id: '', label: '✨ All Posts' },
    { id: 'General', label: '💬 General' }
  ];

  showModal = false;
  editingPost: Post | null = null;
  submitting = false;
  showToast = '';
  deleteConfirmId = '';

  form = { title: '', description: '', image: '', category: 'General' };

  ngOnInit() {
    this.loadCategories();
    this.fetchPosts();
  }

  loadCategories() {
    this.http.get<ApiCategory[]>(`${environment.apiUrl}/categories`).subscribe({
      next: (res) => {
        this.communityCategories = [
          { id: '', label: '✨ All Posts' },
          { id: 'General', label: '💬 General' },
          ...res.map(cat => ({ id: cat.name, label: this.catLabel(cat.name) }))
        ];
      },
      error: () => {}
    });
  }

  private catLabel(name: string): string {
    const icons: Record<string, string> = {
      pg: '🏠', hostel: '🏠', food: '🍽️', electrician: '⚡',
      plumber: '🔧', clean: '🧹', laundry: '👔', transport: '🚗',
      beauty: '💆', salon: '✂️', tutor: '📚', repair: '🔩'
    };
    const lower = name.toLowerCase();
    for (const [k, icon] of Object.entries(icons)) {
      if (lower.includes(k)) return `${icon} ${name}`;
    }
    return `🔵 ${name}`;
  }

  fetchPosts() {
    this.loading = true;
    this.http.get<Post[]>(`${environment.apiUrl}/community`).subscribe({
      next: (res) => {
        this.allPosts = res;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.allPosts = [
          { postId: '1', title: 'Best PG in Gachibowli?', description: 'Looking for good PG options near Cyber Gateway. Any recommendations for working professionals?', category: 'PG/Hostel', authorFullName: 'Ravi Kumar', time: new Date().toISOString() },
          { postId: '2', title: 'CleanPro Services Review', description: 'Just used CleanPro for a deep clean — absolutely fantastic! Super professional team, thorough work, and great pricing. Highly recommend!', category: 'Reviews', authorFullName: 'Priya Sharma', time: new Date().toISOString() },
          { postId: '3', title: 'Reliable plumber needed in Kondapur', description: 'Had a pipe burst last night. Looking for emergency plumbing services. Anyone have contacts?', category: 'Help', authorFullName: 'Arjun Mehta', time: new Date().toISOString() },
        ];
        this.applyFilter();
        this.loading = false;
      }
    });
  }

  setCategory(id: string) {
    this.activeCategory = id;
    this.applyFilter();
  }

  private applyFilter() {
    this.posts = this.activeCategory
      ? this.allPosts.filter(p => p.category === this.activeCategory)
      : [...this.allPosts];
  }

  get isLoggedIn(): boolean { return !!this.authService.getRole(); }
  get myFullName(): string { return this.authService.getFullName(); }

  isMyPost(post: Post): boolean {
    return this.isLoggedIn && !!this.myFullName && post.authorFullName === this.myFullName;
  }

  openCreate() {
    this.editingPost = null;
    this.form = { title: '', description: '', image: '', category: 'General' };
    this.showModal = true;
  }

  openEdit(post: Post) {
    this.editingPost = post;
    this.form = { title: post.title, description: post.description, image: post.image || '', category: post.category || 'General' };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.editingPost = null;
    this.form = { title: '', description: '', image: '', category: 'General' };
  }

  isFormValid(): boolean {
    return this.form.title.trim().length > 0 && this.form.description.trim().length > 0;
  }

  submitPost() {
    if (!this.isFormValid()) return;
    this.submitting = true;
    const payload = {
      title: this.form.title,
      description: this.form.description,
      image: this.form.image || undefined,
      category: this.form.category
    };

    if (this.editingPost) {
      this.http.put<Post>(`${environment.apiUrl}/community/${this.editingPost.postId}`, payload).subscribe({
        next: (res) => {
          const idx = this.allPosts.findIndex(p => p.postId === this.editingPost?.postId);
          if (idx !== -1) this.allPosts[idx] = res;
          this.applyFilter();
          this.submitting = false;
          this.closeModal();
          this.showToast = 'Post updated!';
          setTimeout(() => { this.showToast = ''; }, 3000);
        },
        error: () => { this.submitting = false; }
      });
    } else {
      this.http.post<Post>(`${environment.apiUrl}/community`, payload).subscribe({
        next: (res) => {
          this.allPosts.unshift(res);
          this.applyFilter();
          this.submitting = false;
          this.closeModal();
          this.showToast = 'Post shared!';
          setTimeout(() => { this.showToast = ''; }, 3000);
        },
        error: () => { this.submitting = false; }
      });
    }
  }

  confirmDelete(postId: string) { this.deleteConfirmId = postId; }
  cancelDelete() { this.deleteConfirmId = ''; }

  deletePost(postId: string) {
    this.http.delete(`${environment.apiUrl}/community/${postId}`).subscribe({
      next: () => {
        this.allPosts = this.allPosts.filter(p => p.postId !== postId);
        this.applyFilter();
        this.deleteConfirmId = '';
        this.showToast = 'Post deleted.';
        setTimeout(() => { this.showToast = ''; }, 2500);
      },
      error: () => { this.deleteConfirmId = ''; }
    });
  }

  getInitial(name: string): string { return (name || '?').charAt(0).toUpperCase(); }

  getCategoryLabel(cat?: string): string {
    if (!cat || cat === 'General') return '💬 General';
    const found = this.communityCategories.find(c => c.id === cat);
    return found ? found.label : `🔵 ${cat}`;
  }
}
