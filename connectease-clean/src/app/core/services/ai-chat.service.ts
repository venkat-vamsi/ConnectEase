import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';

export interface ListingCardDTO {
  sid: number;
  name: string;
  description: string;
  price: number;
  categoryName?: string;
  city?: string;
  area?: string;
  primaryImageUrl?: string;
}

export interface AIChatResponse {
  aiMessage: string;
  cards: ListingCardDTO[];
}

@Injectable({ providedIn: 'root' })
export class AiChatService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8081/api/v1/ai-chat/ask'; // Ensure this matches your Spring Boot controller

  askAssistant(query: string): Observable<AIChatResponse> {
    return this.http.post<AIChatResponse>(this.apiUrl, { query }).pipe(
      catchError(err => {
        console.error('AI Service Error:', err);
        // Fallback response so the UI doesn't crash
        return of({
          aiMessage: "I'm having a bit of trouble connecting to my servers right now. Please try again in a moment!",
          cards: []
        });
      })
    );
  }
}