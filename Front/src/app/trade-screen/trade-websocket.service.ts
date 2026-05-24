import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

// ✅ SockJS de vuelta — funciona con setAllowedOriginPatterns(*) en el backend
import SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class TradeWebSocketService {
  private stompClient: any = null;
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

  // ✅ FIX: Eliminado el "style:" que era un label JS sin efecto
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
    // ✅ FIX: Siempre desconectar y limpiar antes de crear una nueva conexión
    // Evita que una sesión anterior con token viejo quede activa
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect(() => {
        this.stompClient = null;
        this.doConnect(token, userId, onConnectedCallback);
      });
    } else {
      this.stompClient = null;
      this.doConnect(token, userId, onConnectedCallback);
    }
  }

  private doConnect(token: string, userId: number, onConnectedCallback?: (status: string) => void): void {
    const socket = new SockJS(this.apiUrl + 'ws', null, {
      transports: ['websocket'] // ✅ Solo WebSocket, sin polling ni iframe
    });
    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = (msg: string) => console.log('[STOMP DEBUG]', msg);

    const headers = { 'Authorization': `Bearer ${token}` };

    this.stompClient.connect(headers, (frame: any) => {
      console.log('[STOMP] ✅ Conectado:', frame);
      if (onConnectedCallback) onConnectedCallback("CONNECTED");

      this.stompClient.subscribe(`/user/${userId}/queue/trade-notifications`, (msg: any) => {
        console.log('[STOMP] 📨 Notificación recibida:', msg.body);
        this.notifications$.next(msg.body);
      });

      this.stompClient.subscribe(`/user/${userId}/queue/trade-errors`, (msg: any) => {
        console.log('[STOMP] ⚠️ Error recibido:', msg.body);
        this.errors$.next(msg.body);
      });

    }, (error: any) => {
      console.error('[STOMP] ❌ Fallo de handshake:', error);
    });
  }

  sendProposal(payload: any): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send("/app/trade/propose", {}, JSON.stringify(payload));
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
    if (this.stompClient !== null) {
      this.stompClient.disconnect();
      this.stompClient = null;
    }
  }
}
