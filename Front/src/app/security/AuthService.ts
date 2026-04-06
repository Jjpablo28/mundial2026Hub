import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';

export interface AuthResponse {
  token: string;
  role: string;
}
export interface AuthorityPayload {
  authority: string;
}
export interface JwtTokenPayload {
  user: string;
  sub?: string;
  authorities?: AuthorityPayload[]; // <-- Así viene en el token
  exp?: number;
  iat?: number;
}


@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenKey = 'authToken';
  isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  constructor( private router: Router,private http: HttpClient) {

  }
  createAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    if (token) {
      return new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });
    }
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }


  logout(): void {
    this.removeToken();
    localStorage.removeItem('role');
    localStorage.removeItem('uploadedFileName');
    localStorage.removeItem('response');
    this.router.navigate(['/']);
    this.isAuthenticatedSubject.next(false);
  }

  saveToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }
  removeToken(): void {
    localStorage.removeItem(this.tokenKey);
  }

  hasToken(): boolean {
    const token = this.getToken();
    if (!token) return false;

    const payload = this.getDecodedToken();
    if (!payload || !payload.exp) return false;

    const tiempoActualEnSegundos = Math.floor(Date.now() / 1000);

    const esValido = payload.exp > tiempoActualEnSegundos;

    if (!esValido) {
      console.error("El token ha expirado");
    }

    return esValido;
  }

  getRole(): string | null {
    const payload = this.getDecodedToken();

    // Si no hay payload o no tiene authorities, retorna null
    if (!payload || !payload.authorities || payload.authorities.length === 0) {
      return null;
    }

    // Extrae el primer rol del arreglo (ej: "ROLE_TEMPORAL")
    return payload.authorities[0].authority;
  }

  getDecodedToken(): JwtTokenPayload | null {
    const token = this.getToken();
    if (token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        const parsedPayload = JSON.parse(jsonPayload);
        console.log('CONTENIDO REAL DEL TOKEN:', parsedPayload); // <-- AGREGA ESTO

        return parsedPayload as JwtTokenPayload;
      } catch (error) {
        console.error('Error al decodificar el token:', error);
        return null;
      }
    }
    return null;
  }
}
