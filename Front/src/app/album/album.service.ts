import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, timer } from 'rxjs';
import { catchError, retryWhen, mergeMap, shareReplay, tap } from 'rxjs/operators';
import { AuthService } from '../security/AuthService';

export interface Player {
  id: number;
  playerName: string;
  rarity: 'COMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
  teamId: number;
  position: 'Goalkeeper' | 'Defender' | 'Midfielder' | 'Attacker';
  cost: number;
  imageUrl: string;
}

export interface UserStickerResponse {
  id: number;
  userId: number;
  stickerId: number;
  teamId: number;
  quantity: number;
  obtainedAt: string;
}

export interface Team {
  id: number;
  name: string;
  flagUrl?: string;
}

export interface OpenPackResponse {
  data: UserStickerResponse[];
  success: boolean;
  message: string;
}

/**
 * Operador de retry con backoff exponencial.
 * Reintenta hasta `maxRetries` veces con espera creciente entre intentos.
 * Ideal para errores transitorios como "Lock wait timeout exceeded".
 */
function retryWithBackoff(maxRetries = 3, baseDelayMs = 800) {
  return retryWhen((errors: Observable<any>) =>
    errors.pipe(
      mergeMap((error, attempt) => {
        // Solo reintenta en errores de servidor (5xx) o timeouts de DB
        const isRetryable =
          error?.status >= 500 ||
          error?.error?.message?.includes('Lock wait timeout') ||
          error?.error?.message?.includes('transaction');

        if (attempt >= maxRetries || !isRetryable) {
          return throwError(() => error);
        }

        const delay = baseDelayMs * Math.pow(2, attempt); // 800ms, 1600ms, 3200ms
        console.warn(`Reintentando en ${delay}ms (intento ${attempt + 1}/${maxRetries})…`);
        return timer(delay);
      })
    )
  );
}

@Injectable({
  providedIn: 'root'
})
export class AlbumService {

  private baseUrl = 'https://mundial2026hub.onrender.com/user-stickers';
  private stickersUrl = 'https://mundial2026hub.onrender.com/sticker';

  // ─── Caché de peticiones costosas ───────────────────────────────────────────
  // shareReplay(1) garantiza que múltiples suscriptores reutilicen la misma
  // respuesta sin disparar una segunda petición HTTP.
  private _allStickers$: Observable<Player[]> | null = null;
  private _allTeams$: Observable<Team[]> | null = null;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private get userId(): number {
    return this.authService.getUserId() ?? 0;
  }

  /** Equipos — se cachean indefinidamente (no cambian en runtime). */
  public getAllTeams(): Observable<Team[]> {
    if (!this._allTeams$) {
      this._allTeams$ = this.http
        .get<Team[]>('https://mundial2026hub.onrender.com/teams')
        .pipe(shareReplay(1));
    }
    return this._allTeams$;
  }

  /** Stickers — se cachean; llama a invalidateStickersCache() si necesitas refrescar. */
  public getAllStickers(): Observable<Player[]> {
    if (!this._allStickers$) {
      this._allStickers$ = this.http
        .get<Player[]>(this.stickersUrl)
        .pipe(shareReplay(1));
    }
    return this._allStickers$;
  }

  public invalidateStickersCache(): void {
    this._allStickers$ = null;
  }

  public buyPack(): Observable<any> {
    return this.http
      .post<any>(`${this.baseUrl}/buy-pack/${this.userId}`, {})
      .pipe(retryWithBackoff(2, 500)) as Observable<any>;
  }

  public getUserStickersByTeam(teamId: number): Observable<UserStickerResponse[]> {
    return this.http.get<UserStickerResponse[]>(
      `${this.baseUrl}/user/${this.userId}/team/${teamId}`
    );
  }

  public getAllUserStickers(): Observable<UserStickerResponse[]> {
    return this.http.get<UserStickerResponse[]>(
      `${this.baseUrl}/user/${this.userId}`
    );
  }

  /**
   * Abre un sobre con reintentos automáticos ante lock timeouts del servidor.
   */
  public openPack(): Observable<OpenPackResponse> {
    return this.http
      .post<OpenPackResponse>(`${this.baseUrl}/open-pack/${this.userId}`, {})
      .pipe(
        // ❌ Quita el retry — cada reintento agrava el lock
        // retryWithBackoff(3, 800)

        // ✅ Solo propaga el error limpiamente
        catchError(err => {
          const msg = err?.error?.message ?? '';
          if (msg.includes('Lock wait timeout')) {
            return throwError(() => ({
              ...err,
              userMessage: 'El servidor está ocupado. Espera unos segundos e intenta de nuevo.'
            }));
          }
          return throwError(() => err);
        })
      ) as Observable<OpenPackResponse>;
  }
}
