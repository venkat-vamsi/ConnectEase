import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { ChatService } from '../../core/services/chat';

@Component({
  selector: 'app-chat',
  standalone: true,
  templateUrl: './chat.html',
  styleUrls: ['./chat.css']
})
export class ChatComponent implements OnInit, OnDestroy {
  chatService = inject(ChatService);
  chatHistory: any[] = [];
  sub: any;

  ngOnInit() {
    this.sub = this.chatService.messages$.subscribe(msg => {
      this.chatHistory.push(msg);
    });
  }

  send(text: string) {
    if (text.trim()) {
      this.chatService.sendMessage(text);
    }
  }

  close() {
    this.chatService.closeChat();
  }

  ngOnDestroy() {
    if (this.sub) this.sub.unsubscribe();
  }
}