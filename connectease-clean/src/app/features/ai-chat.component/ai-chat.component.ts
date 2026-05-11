import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
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
  private sanitizer = inject(DomSanitizer);

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

  // Helper method to format AI markdown text to HTML
  formatMessage(text: string): SafeHtml {
    if (!text) return this.sanitizer.bypassSecurityTrustHtml('');
    
    let formatted = text;
    
    // Convert markdown headings (###, ##, #) to HTML headings
    formatted = formatted.replace(/^### (.*?)$/gm, '<h3>$1</h3>');
    formatted = formatted.replace(/^## (.*?)$/gm, '<h2>$1</h2>');
    formatted = formatted.replace(/^# (.*?)$/gm, '<h1>$1</h1>');
    
    // Convert bold text (**text** or __text__)
    formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    formatted = formatted.replace(/__(.*?)__/g, '<strong>$1</strong>');
    
    // Convert italic text (*text* or _text_)
    formatted = formatted.replace(/\*(.*?)\*/g, '<em>$1</em>');
    formatted = formatted.replace(/_(.*?)_/g, '<em>$1</em>');
    
    // Convert bullet points (* item) to list items
    formatted = formatted.replace(/^\* (.*?)$/gm, '<li>$1</li>');
    // Wrap consecutive list items in <ul> tags
    formatted = formatted.replace(/(<li>.*?<\/li>)/s, '<ul>$1</ul>');
    // Clean up multiple <ul> tags
    formatted = formatted.replace(/<\/ul>\n<ul>/g, '');
    
    // Convert numbered lists (1. item) to ordered list items
    formatted = formatted.replace(/^\d+\. (.*?)$/gm, '<li>$1</li>');
    
    // Convert line breaks
    formatted = formatted.replace(/\n/g, '<br>');
    
    // Add some styling to headings for better appearance
    formatted = formatted.replace(/<h3>/g, '<h3 style="margin-top: 12px; margin-bottom: 8px; font-weight: bold;">');
    formatted = formatted.replace(/<h2>/g, '<h2 style="margin-top: 16px; margin-bottom: 10px; font-weight: bold;">');
    formatted = formatted.replace(/<h1>/g, '<h1 style="margin-top: 20px; margin-bottom: 12px; font-weight: bold;">');
    formatted = formatted.replace(/<ul>/g, '<ul style="margin-left: 20px; margin-top: 8px; margin-bottom: 8px;">');
    
    return this.sanitizer.bypassSecurityTrustHtml(formatted);
  }

  private scrollToBottom() {
    setTimeout(() => {
      const chatContainer = document.getElementById('chat-history');
      if (chatContainer) chatContainer.scrollTop = chatContainer.scrollHeight;
    }, 100);
  }
}