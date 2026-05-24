import { Injectable } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {Subject, Observable, BehaviorSubject} from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TradeWebSocketService {

  private stompClient: Client | null = null;
  private notifications$ = new BehaviorSubject<string>('');
  private errors$ = new Subject<string>();

  private currentSessionUuid: string = '';
  private currentUserId: number = 0;

  // ✅ Guardamos token y userId como variables de clase
  // para que estén disponibles en reconexiones automáticas
  private currentToken: string = '';
  private connectedUserId: number = 0;

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
    // ✅ Guardamos para reconexiones
    this.currentToken = token;
    this.connectedUserId = userId;

    if (this.stompClient && this.stompClient.active) {
      this.stompClient.deactivate().then(() => {
        this.stompClient = null;
        this.doConnect(onConnectedCallback);
      });
    } else {
      this.stompClient = null;
      this.doConnect(onConnectedCallback);
    }
  }

  private doConnect(onConnectedCallback?: (status: string) => void): void {
    // ✅ Usamos las variables de clase en lugar de parámetros
    // Así la reconexión automática también usa los valores correctos
    const token = this.currentToken;
    const userId = this.connectedUserId;

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
      console.log('[STOMP] ✅ Conectado como userId:', userId);
      if (onConnectedCallback) onConnectedCallback("CONNECTED");

      // ✅ FIX: Suscripción con userId explícito en la ruta
      this.stompClient?.subscribe(
        `/user/${userId}/queue/trade-notifications`,
        (msg: Message) => {
          console.log('[STOMP] 📨 Notificación recibida:', msg.body);
          this.notifications$.next(msg.body);
        }
      );

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
