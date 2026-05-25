import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AuthService} from '../security/AuthService';

export interface Player {
  id: number;
  playerName: string;
  rarity: 'COMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
  teamId: number;
  position: 'Goalkeeper' | 'Defender' | 'Midfielder' | 'Attacker';
  cost: number;
  imageUrl: string;
}

// ✅ CORREGIDO: la API devuelve stickerId plano, no objeto anidado
export interface UserStickerResponse {
  id: number;
  userId: number;
  stickerId: number;   // ← era sticker.id antes, ahora es stickerId directo
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

@Injectable({
  providedIn: 'root'
})
export class AlbumService {

  private baseUrl = 'https://mundial2026hub.onrender.com/user-stickers';
  private stickersUrl = 'https://mundial2026hub.onrender.com/sticker';

  constructor(
    private http: HttpClient,
    private authService: AuthService  // ✅ inyecta AuthService
  ) {
  }

  // ✅ userId dinámico desde el token
  private get userId(): number {
    return this.authService.getUserId() ?? 0;
  }

  public getAllTeams(): Observable<Team[]> {
    return this.http.get<Team[]>('https://mundial2026hub.onrender.com/teams');
  }

  public getAllStickers(): Observable<Player[]> {
    return this.http.get<Player[]>(this.stickersUrl);
  }

  public buyPack(): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/buy-pack/${this.userId}`, {});
  }

  public getUserStickersByTeam(teamId: number): Observable<UserStickerResponse[]> {
    return this.http.get<UserStickerResponse[]>(`${this.baseUrl}/user/${this.userId}/team/${teamId}`);
  }

  public getAllUserStickers(): Observable<UserStickerResponse[]> {
    return this.http.get<UserStickerResponse[]>(`${this.baseUrl}/user/${this.userId}`);
  }

  public openPack(): Observable<OpenPackResponse> {
    return this.http.post<OpenPackResponse>(`${this.baseUrl}/open-pack/${this.userId}`, {});
  }
}
