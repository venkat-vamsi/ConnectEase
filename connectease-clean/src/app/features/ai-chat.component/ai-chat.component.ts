import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { AiChatService, ChatTurn, ListingCardDTO } from '../../core/services/ai-chat.service';

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

    this.messages.push({ sender: 'user', text: query });
    this.userInput = '';
    this.isLoading = true;

    this.currentCards = [];

    const prior = this.messages.slice(1, -1);
    const history: ChatTurn[] = prior.slice(-6).map(m => ({
      role: (m.sender === 'user' ? 'user' : 'model') as 'user' | 'model',
      text: m.text
    }));

    this.aiService.askAssistant(query, history).subscribe({
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

  // --- NEW: Helper method to format AI markdown text to HTML ---
  formatMessage(text: string): string {
    if (!text) return '';
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // Converts **text** to bold
      .replace(/\*(.*?)\*/g, '<em>$1</em>')             // Converts *text* to italic
      .replace(/\n/g, '<br>');                          // Converts newlines to actual line breaks
  }

  private scrollToBottom() {
    setTimeout(() => {
      const chatContainer = document.getElementById('chat-history');
      if (chatContainer) chatContainer.scrollTop = chatContainer.scrollHeight;
    }, 100);
  }
}