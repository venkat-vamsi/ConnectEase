import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private client!: Client;
  public messages$ = new Subject<any>();
  public currentSessionId = '';
  public isChatOpen = false;

  initConnection() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:9090/ws-chat/websocket'),
      reconnectDelay: 5000,
      onConnect: () => {
        if (this.currentSessionId) {
          this.client.subscribe(`/topic/session/${this.currentSessionId}`, (msg: Message) => {
            this.messages$.next(JSON.parse(msg.body));
          });
        }
      }
    });
    this.client.activate();
  }

  startChat(vendorId: string) {
    this.http.get<any>(`/api/chat/start/${vendorId}`, { withCredentials: true }).subscribe(res => {
      this.currentSessionId = res.sessionId;
      this.isChatOpen = true;
      this.initConnection();
    });
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
    this.isChatOpen = false;
    if(this.client) this.client.deactivate();
  }
}