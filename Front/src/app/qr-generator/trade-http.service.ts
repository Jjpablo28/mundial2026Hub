import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface QrResponse {
  uuid: string;
  qrBase64: string;
  qr_content: string;
  /** Código alfanumérico corto (ej: "AB3X7Q") devuelto por el backend al crear la sesión. */
  shortCode: string;
}

@Injectable({
  providedIn: 'root'
})
export class TradeHttpService {
  private apiUrl = 'https://mundial2026hub.onrender.com/trade-session';

  constructor(private http: HttpClient) {}

  /** Crea una nueva sesión de intercambio y devuelve QR + shortCode. */
  createSession(userId: number): Observable<QrResponse> {
    return this.http.post<QrResponse>(`${this.apiUrl}/create?userId=${userId}`, {});
  }

  /**
   * Une al Usuario B a una sesión usando el UUID completo (escaneo de QR).
   * El backend retorna un String plano con el ID del Usuario A.
   */
  joinSession(uuid: string, userId: number): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/join?sessionUuid=${uuid}&userBId=${userId}`,
      {},
      { responseType: 'text' }
    );
  }

  /**
   * Une al Usuario B a una sesión usando el código alfanumérico corto (ej: "AB3X7Q").
   * Ideal para unirse de forma presencial sin escanear el QR.
   */
  joinByShortCode(shortCode: string, userId: number): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/join-by-code?shortCode=${shortCode.toUpperCase()}&userBId=${userId}`,
      {},
      { responseType: 'text' }
    );
  }

  /**
   * Une al Usuario B buscando la sesión PENDING más reciente del Usuario A por su correo.
   * Útil cuando el Usuario B conoce el email del anfitrión pero no tiene el QR ni el código.
   */
  joinByEmail(email: string, userId: number): Observable<string> {
    return this.http.post(
      `${this.apiUrl}/join-by-email?email=${encodeURIComponent(email)}&userBId=${userId}`,
      {},
      { responseType: 'text' }
    );
  }
}
