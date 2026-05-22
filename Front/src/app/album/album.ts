import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlbumService, Player, UserStickerResponse } from './album.service';

@Component({
  selector: 'app-album',
  standalone: false,
  templateUrl: './album.html',
  styleUrls: ['./album.scss']
})
export class Album implements OnInit {

  public currentTeamId: number = 22;
  private userInventory: UserStickerResponse[] = [];

  // Lista maestra fija de los 25 jugadores para armar las casillas
  public allTeamPlayers: Player[] = [
    { "id": 1, "playerName": "T. Hayakawa", "rarity": "RARE", "teamId": 22, "position": "Goalkeeper", "cost": 30, "imageUrl": "https://media.api-sports.io/football/players/304782.png" },
    { "id": 2, "playerName": "L. Kokubo", "rarity": "RARE", "teamId": 22, "position": "Goalkeeper", "cost": 30, "imageUrl": "https://media.api-sports.io/football/players/162482.png" },
    { "id": 3, "playerName": "T. Nozawa", "rarity": "RARE", "teamId": 22, "position": "Goalkeeper", "cost": 30, "imageUrl": "https://media.api-sports.io/football/players/109381.png" },
    { "id": 4, "playerName": "T. Ando", "rarity": "COMMON", "teamId": 22, "position": "Defender", "cost": 10, "imageUrl": "https://media.api-sports.io/football/players/307929.png" },
    { "id": 5, "playerName": "D. Hashioka", "rarity": "COMMON", "teamId": 22, "position": "Defender", "cost": 10, "imageUrl": "https://media.api-sports.io/football/players/33095.png" },
    { "id": 6, "playerName": "K. Itakura", "rarity": "COMMON", "teamId": 22, "position": "Defender", "cost": 10, "imageUrl": "https://media.api-sports.io/football/players/38114.png" },
    { "id": 7, "playerName": "H. Mochizuki", "rarity": "COMMON", "teamId": 22, "position": "Defender", "cost": 10, "imageUrl": "https://media.api-sports.io/football/players/455193.png" },
    { "id": 8, "playerName": "Y. Nagatomo", "rarity": "COMMON", "teamId": 22, "position": "Defender", "cost": 10, "imageUrl": "https://media.api-sports.io/football/players/440.png" },
    { "id": 9, "playerName": "A. Seko", "rarity": "COMMON", "teamId": 22, "position": "Defender", "cost": 10, "imageUrl": "https://media.api-sports.io/football/players/33165.png" },
    { "id": 10, "playerName": "Y. Sugawara", "rarity": "COMMON", "teamId": 22, "position": "Defender", "cost": 10, "imageUrl": "https://media.api-sports.io/football/players/32887.png" },
    { "id": 11, "playerName": "J. Suzuki", "rarity": "COMMON", "teamId": 22, "position": "Defender", "cost": 10, "imageUrl": "https://media.api-sports.io/football/players/351014.png" },
    { "id": 12, "playerName": "R. Dōan", "rarity": "EPIC", "teamId": 22, "position": "Midfielder", "cost": 40, "imageUrl": "https://media.api-sports.io/football/players/2598.png" },
    { "id": 13, "playerName": "W. Endo", "rarity": "EPIC", "teamId": 22, "position": "Midfielder", "cost": 40, "imageUrl": "https://media.api-sports.io/football/players/8500.png" },
    { "id": 14, "playerName": "J. Fujita", "rarity": "EPIC", "teamId": 22, "position": "Midfielder", "cost": 40, "imageUrl": "https://media.api-sports.io/football/players/146586.png" },
    { "id": 15, "playerName": "D. Kamada", "rarity": "EPIC", "teamId": 22, "position": "Midfielder", "cost": 40, "imageUrl": "https://media.api-sports.io/football/players/2601.png" },
    { "id": 16, "playerName": "S. Kitano", "rarity": "EPIC", "teamId": 22, "position": "Midfielder", "cost": 40, "imageUrl": "https://media.api-sports.io/football/players/288073.png" },
    { "id": 17, "playerName": "K. Sano", "rarity": "EPIC", "teamId": 22, "position": "Midfielder", "cost": 40, "imageUrl": "https://media.api-sports.io/football/players/33889.png" },
    { "id": 18, "playerName": "R. Sato", "rarity": "EPIC", "teamId": 22, "position": "Midfielder", "cost": 40, "imageUrl": "https://media.api-sports.io/football/players/542861.png" },
    { "id": 19, "playerName": "A. Tanaka", "rarity": "EPIC", "teamId": 22, "position": "Midfielder", "cost": 40, "imageUrl": "https://media.api-sports.io/football/players/32966.png" },
    { "id": 20, "playerName": "K. Goto", "rarity": "LEGENDARY", "teamId": 22, "position": "Attacker", "cost": 50, "imageUrl": "https://media.api-sports.io/football/players/375930.png" },
    { "id": 21, "playerName": "J. Ito", "rarity": "LEGENDARY", "teamId": 22, "position": "Attacker", "cost": 50, "imageUrl": "https://media.api-sports.io/football/players/1942.png" },
    { "id": 22, "playerName": "T. Kubo", "rarity": "LEGENDARY", "teamId": 22, "position": "Attacker", "cost": 50, "imageUrl": "https://media.api-sports.io/football/players/32862.png" },
    { "id": 23, "playerName": "S. Machino", "rarity": "LEGENDARY", "teamId": 22, "position": "Attacker", "cost": 50, "imageUrl": "https://media.api-sports.io/football/players/106851.png" },
    { "id": 24, "playerName": "D. Maeda", "rarity": "LEGENDARY", "teamId": 22, "position": "Attacker", "cost": 50, "imageUrl": "https://media.api-sports.io/football/players/33224.png" },
    { "id": 25, "playerName": "T. Minamino", "rarity": "LEGENDARY", "teamId": 22, "position": "Attacker", "cost": 50, "imageUrl": "https://media.api-sports.io/football/players/1101.png" }
  ];

  constructor(private albumService: AlbumService) { }

  ngOnInit(): void {
    this.loadTeamStickers(this.currentTeamId);
  }

  public loadTeamStickers(teamId: number): void {
    this.currentTeamId = teamId;
    this.albumService.getUserStickersByTeam(teamId).subscribe({
      next: (data) => {
        this.userInventory = data;
      },
      error: (err) => {
        console.error('Error cargando inventario de cromos:', err);
      }
    });
  }

  public getStickerQuantity(playerStickerId: number): number {
    const record = this.userInventory.find(item => item.sticker.id === playerStickerId);
    return record ? record.quantity : 0;
  }

  public getPlayersByPosition(position: 'Goalkeeper' | 'Defender' | 'Midfielder' | 'Attacker'): Player[] {
    return this.allTeamPlayers.filter(player => player.position === position);
  }
}
