import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface QrResponse {
  uuid: string;
  qrBase64: string;
  qr_content: string;
}

@Injectable({
  providedIn: 'root'
})
export class TradeHttpService {
  private apiUrl = 'https://mundial2026hub.onrender.com/trade-session';

  constructor(private http: HttpClient) {}

  // El AuthInterceptor adjunta el Bearer Token automáticamente
  createSession(userId: number): Observable<QrResponse> {
    return this.http.post<QrResponse>(`${this.apiUrl}/create/${userId}`, {});
  }

  // ✅ NUEVO: El Usuario B se une a la sesión del Usuario A vía HTTP
  joinSession(uuid: string, userId: number): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/join/${uuid}/user/${userId}`,
      {},
      { responseType: 'text' } // El backend retorna un String plano, no JSON
    );
  }
}
