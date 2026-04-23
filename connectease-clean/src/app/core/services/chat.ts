import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client, Message, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private client!: Client;
  public messages$ = new Subject<any>();
  public currentSessionId = '';
  public isChatOpen = false;
  private sessionSub?: StompSubscription;

  initConnection() {
    if (this.client) {
      this.client.deactivate();
    }
    this.client = new Client({
      webSocketFactory: () => new SockJS(window.location.origin + '/ws-chat'),
      reconnectDelay: 5000,
      onConnect: () => {
        this.isChatOpen = true;
        if (this.currentSessionId) {
          this._doSubscribe(this.currentSessionId);
        }
      },
      onDisconnect: () => { this.isChatOpen = false; }
    });
    this.client.activate();
  }

  subscribeToSession(sessionId: string) {
    this.currentSessionId = sessionId;
    if (this.client?.connected) {
      this._doSubscribe(sessionId);
    } else if (!this.isChatOpen) {
      // Not connected at all — initiate connection (onConnect will subscribe)
      this.initConnection();
    }
    // If connected=false but isChatOpen=true → reconnecting, onConnect will subscribe
  }

  private _doSubscribe(sessionId: string) {
    this.sessionSub?.unsubscribe();
    this.sessionSub = this.client.subscribe(
      `/topic/session/${sessionId}`,
      (msg: Message) => { this.messages$.next(JSON.parse(msg.body)); }
    );
  }

  sendMessage(content: string) {
    if (this.client?.connected && this.currentSessionId) {
      this.client.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify({ sessionId: this.currentSessionId, content })
      });
    }
  }

  closeChat() {
    this.sessionSub?.unsubscribe();
    this.isChatOpen = false;
    if (this.client) this.client.deactivate();
  }
}
