import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { AiChatService, ListingCardDTO } from '../../core/services/ai-chat.service';

interface ChatMessage {
  sender: 'ai' | 'user';
  text: string;
}

@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ai-chat.component.html',
  styleUrls: ['./ai-chat.component.css']
})
export class AiChatComponent implements OnInit {
  private aiService = inject(AiChatService);
  private route = inject(ActivatedRoute);

  userInput: string = '';
  isLoading: boolean = false;
  
  messages: ChatMessage[] = [
    { sender: 'ai', text: "Hi! I'm your Chennai migration assistant. I'm here to help you find the perfect accommodation or service in Chennai. What are you looking for?" }
  ];
  
  currentCards: ListingCardDTO[] = [];

  ngOnInit() {
    // If they came from the Home page search bar, grab the query and run it instantly
    this.route.queryParams.subscribe(params => {
      const initialQuery = params['q'];
      if (initialQuery) {
        this.sendMessage(initialQuery);
      }
    });
  }

  sendMessage(overrideQuery?: string) {
    const query = overrideQuery || this.userInput;
    if (!query.trim() || this.isLoading) return;

    // 1. Add user message to UI
    this.messages.push({ sender: 'user', text: query });
    this.userInput = '';
    this.isLoading = true;

    // 2. Clear old cards while loading
    this.currentCards = [];

    // 3. Call Spring Boot
    this.aiService.askAssistant(query).subscribe({
      next: (response) => {
        this.messages.push({ sender: 'ai', text: response.aiMessage });
        this.currentCards = response.cards;
        this.isLoading = false;
        this.scrollToBottom();
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  // Helper to keep chat scrolled down
  private scrollToBottom() {
    setTimeout(() => {
      const chatContainer = document.getElementById('chat-history');
      if (chatContainer) chatContainer.scrollTop = chatContainer.scrollHeight;
    }, 100);
  }
}