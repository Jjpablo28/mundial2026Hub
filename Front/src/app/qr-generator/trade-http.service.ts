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

  // ✅ CORRECCIÓN: Se envía como Query Param (?userId=15)
  createSession(userId: number): Observable<QrResponse> {
    return this.http.post<QrResponse>(`${this.apiUrl}/create?userId=${userId}`, {});
  }

  // ✅ CORRECCIÓN: Se envían ambos datos como Query Params para coincidir con el backend
  joinSession(uuid: string, userId: number): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/join?sessionUuid=${uuid}&userBId=${userId}`,
      {},
      { responseType: 'text' } // El backend retorna un String plano, no JSON
    );
  }
}
