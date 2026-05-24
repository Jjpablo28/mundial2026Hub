import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
  quantity: number;
  sticker: Player;
  obtainedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class AlbumService {

  private baseUrl = 'https://mundial2026hub.onrender.com/user-stickers';

  constructor(private http: HttpClient) { }

  public getUserStickersByTeam(teamId: number): Observable<UserStickerResponse[]> {
    return this.http.get<UserStickerResponse[]>(`${this.baseUrl}/user/6/team/${teamId}`);
  }
}
