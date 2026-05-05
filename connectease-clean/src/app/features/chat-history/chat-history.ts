import { Component, OnInit, inject, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ChatService } from '../../core/services/chat';
import { AuthService } from '../../core/services/auth';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';

interface SessionSummary {
  sessionId: string;
  participantName: string;
  participantImage: string | null;
  participantId: string;
  lastMessage: string;
  startedAt: string;
  messageCount: number;
}

interface ChatMessage {
  messageId: string;
  sessionId?: string;
  senderId: string;
  senderName: string;
  senderImage: string | null;
  content: string;
  createdAt: string;
}

interface ActiveSession {
  sessionId: string;
  currentUserId: string;
  participantName: string;
  participantImage: string | null;
  messages: ChatMessage[];
}

@Component({
  selector: 'app-chat-history',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, RouterLink],
  templateUrl: './chat-history.html',
  styleUrls: ['./chat-history.css']
})
export class ChatHistoryComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  chatService = inject(ChatService);
  authService = inject(AuthService);

  sessions: SessionSummary[] = [];
  loading = true;
  activeSession: ActiveSession | null = null;
  activeSessionId = '';
  newMessage = '';

  private msgSub?: Subscription;

  ngOnInit() {
    this.loadSessions();
    this.msgSub = this.chatService.messages$.subscribe((msg: any) => {
      if (!this.activeSession || msg.sessionId !== this.activeSession.sessionId) return;
      // Dedup: skip if already have this exact messageId
      if (this.activeSession.messages.some(m => m.messageId === msg.messageId)) return;
      this.activeSession.messages.push(msg);
      this.scrollToBottom();
      const s = this.sessions.find(s => s.sessionId === msg.sessionId);
      if (s) { s.lastMessage = msg.content; }
    });
  }

  ngOnDestroy() {
    this.msgSub?.unsubscribe();
  }

  loadSessions() {
    this.loading = true;
    this.http.get<SessionSummary[]>(`${environment.apiUrl}/chat/sessions`).subscribe({
      next: (res) => {
        this.sessions = res;
        this.loading = false;
        const vendorId = this.route.snapshot.queryParamMap.get('with');
        if (vendorId) { this.openChatSession(vendorId); }
      },
      error: () => {
        this.sessions = [];
        this.loading = false;
        const vendorId = this.route.snapshot.queryParamMap.get('with');
        if (vendorId) { this.openChatSession(vendorId); }
      }
    });
  }

  openSession(session: SessionSummary) {
    session.messageCount = 0;
    this.openChatSession(session.participantId);
  }

  private openChatSession(participantId: string) {
    this.http.get<any>(`${environment.apiUrl}/chat/start/${participantId}`).subscribe({
      next: (res) => {
        const myUid = res.currentUserId || this.authService.getUid();
        // Sync UID to localStorage if it was missing
        if (myUid && !this.authService.getUid()) {
          localStorage.setItem('uid', myUid);
        }
        this.activeSession = {
          sessionId: res.sessionId,
          currentUserId: myUid,
          participantName: res.participantName,
          participantImage: res.participantImage,
          messages: res.messages || []
        };
        this.activeSessionId = res.sessionId;
        this.chatService.subscribeToSession(res.sessionId);
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: () => {}
    });
  }

  sendMessage() {
    const content = this.newMessage.trim();
    if (!content || !this.activeSession) return;
    this.newMessage = '';
    // Send via HTTP POST — cookie auth is reliable; backend broadcasts via WebSocket to both parties
    this.http.post<any>(
      `${environment.apiUrl}/chat/${this.activeSession.sessionId}/messages`,
      { content }
    ).subscribe({ error: () => {} });
    const s = this.sessions.find(s => s.sessionId === this.activeSession?.sessionId);
    if (s) { s.lastMessage = content; }
  }

  closeActiveSession() {
    this.activeSession = null;
    this.activeSessionId = '';
  }

  isMine(msg: ChatMessage): boolean {
    const myUid = this.activeSession?.currentUserId || this.authService.getUid();
    return !!myUid && msg.senderId === myUid;
  }

  getInitial(name: string): string {
    return (name || '?').charAt(0).toUpperCase();
  }

  private scrollToBottom() {
    setTimeout(() => {
      const el = document.getElementById('chat-msgs');
      if (el) el.scrollTop = el.scrollHeight;
    }, 50);
  }
}
