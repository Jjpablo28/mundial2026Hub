import { Injectable } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TradeWebSocketService {

  private stompClient: Client | null = null;
  private notifications$ = new Subject<string>();
  private errors$ = new Subject<string>();

  private currentSessionUuid: string = '';
  private currentUserId: number = 0;
  private apiUrl = 'https://mundial2026hub.onrender.com/';

  constructor(private http: HttpClient) {}

  getStickerInfo(stickerId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}sticker/{id}?idSticker=${stickerId}`);
  }

  getStickersId(userId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}user-stickers/user/${userId}`);
  }

  setSessionData(uuid: string, userId: number): void {
    this.currentSessionUuid = uuid;
    this.currentUserId = userId;
  }

  getSessionUuid(): string {
    return this.currentSessionUuid;
  }

  getMyUserId(): number {
    return this.currentUserId;
  }

  connect(token: string, userId: number, onConnectedCallback?: (status: string) => void): void {
    // ✅ Si ya hay una conexión activa, desconectamos primero
    if (this.stompClient && this.stompClient.active) {
      this.stompClient.deactivate().then(() => {
        this.stompClient = null;
        this.doConnect(token, userId, onConnectedCallback);
      });
    } else {
      this.stompClient = null;
      this.doConnect(token, userId, onConnectedCallback);
    }
  }

  private doConnect(token: string, userId: number, onConnectedCallback?: (status: string) => void): void {
    // ✅ Usando @stomp/stompjs con Client moderno — igual que el proyecto del amigo
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(this.apiUrl + 'ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => console.log('[STOMP DEBUG]', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('[STOMP] ✅ Conectado:', frame);
      if (onConnectedCallback) onConnectedCallback("CONNECTED");

      // Suscripción a notificaciones del intercambio
      this.stompClient?.subscribe(
        `/user/${userId}/queue/trade-notifications`,
        (msg: Message) => {
          console.log('[STOMP] 📨 Notificación recibida:', msg.body);
          this.notifications$.next(msg.body);
        }
      );

      // Suscripción a errores del intercambio
      this.stompClient?.subscribe(
        `/user/${userId}/queue/trade-errors`,
        (msg: Message) => {
          console.log('[STOMP] ⚠️ Error recibido:', msg.body);
          this.errors$.next(msg.body);
        }
      );
    };

    this.stompClient.onStompError = (frame) => {
      console.error('[STOMP] ❌ Error de broker:', frame.headers['message']);
    };

    this.stompClient.onDisconnect = () => {
      console.log('[STOMP] 🔌 Desconectado');
    };

    // ✅ activate() en lugar de connect() — API moderna de @stomp/stompjs
    this.stompClient.activate();
  }

  sendProposal(payload: any): void {
    if (this.stompClient && this.stompClient.active) {
      this.stompClient.publish({
        destination: '/app/trade/propose',
        body: JSON.stringify(payload)
      });
    } else {
      console.warn('[STOMP] ⚠️ Intento de envío sin conexión activa.');
    }
  }

  getNotifications(): Observable<string> {
    return this.notifications$.asObservable();
  }

  getErrors(): Observable<string> {
    return this.errors$.asObservable();
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }
}
