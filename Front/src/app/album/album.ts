import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlbumService, Player, UserStickerResponse } from './album.service';
import { AuthService } from '../security/AuthService';
import { HomeComponentService } from '../home.component/home.component.service';

type View = 'packs' | 'album';


type PackPhase = 'idle' | 'loading' | 'revealed';

@Component({
  selector: 'app-album',
  standalone: false,
  templateUrl: './album.html',
  styleUrls: ['./album.scss']
})
export class Album implements OnInit {

  public currentView: View = 'packs';
  public currentTeamId: number = 22;

  private globalInventory: UserStickerResponse[] = [];
  public isLoadingInventory = false;

  public packCount = 0;
  public packPhase: PackPhase = 'idle';
  public newStickers: UserStickerResponse[] = [];
  public currentCardIndex = 0;
  public packError = '';

  public teamIds: number[] = [];
  private teamNamesMap: Record<number, string> = {};
  public allTeamPlayers: Player[] = [];
  private stickerMap = new Map<number, Player>();

  constructor(
    private albumService: AlbumService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private homeService: HomeComponentService
  ) {}



  ngOnInit(): void {

    this.loadAllStickers();
    this.loadTeams();
    this.loadGlobalInventory();
    this.setPacks();
  }


  private loadAllStickers(): void {
    this.albumService.getAllStickers().subscribe({
      next: (players) => {
        this.allTeamPlayers = players;
        this.stickerMap = new Map(players.map(p => [p.id, p]));

        const fromPlayers = [...new Set(players.map(p => p.teamId))].sort((a, b) => a - b);
        if (this.teamIds.length === 0) this.teamIds = fromPlayers;
      },
      error: (err) => console.error('Error cargando stickers:', err)
    });
  }

  private loadTeams(): void {
    this.albumService.getAllTeams().subscribe({
      next: (teams) => {
        this.teamIds = teams.map(t => t.id);
        teams.forEach(t => (this.teamNamesMap[t.id] = t.name));
      },
      error: (err) => console.error('Error cargando equipos:', err)
    });
  }

  private loadGlobalInventory(): void {
    this.isLoadingInventory = true;
    this.albumService.getAllUserStickers().subscribe({
      next: (data) => {
        this.globalInventory = data;
        this.isLoadingInventory = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error cargando inventario:', err);
        this.isLoadingInventory = false;
      }
    });
  }

  public setPacks(): void {
    const id = this.authService.getUserId();
    this.homeService.getUserById(id).subscribe({
      next: (data) => {
        this.packCount = data.packets;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error obteniendo sobres:', err)
    });
  }

  public loadTeamStickers(teamId: number): void {
    this.currentTeamId = teamId;
    this.currentView = 'album';
  }

  public getStickerPlayer(stickerId: number): Player | undefined {
    return this.stickerMap.get(stickerId);
  }

  public getStickerQuantity(playerStickerId: number): number {
    return this.globalInventory.find(i => i.stickerId === playerStickerId)?.quantity ?? 0;
  }

  public getPlayersByPosition(position: string): Player[] {
    return this.allTeamPlayers.filter(
      p => p.position === position && p.teamId === this.currentTeamId
    );
  }

  public getTeamName(teamId: number): string {
    return this.teamNamesMap[teamId] ?? `Equipo ${teamId}`;
  }

  public getCollectedCount(): number {
    return this.allTeamPlayers.filter(
      p => p.teamId === this.currentTeamId && this.getStickerQuantity(p.id) > 0
    ).length;
  }

  public getProgressPercent(): number {
    const total = this.allTeamPlayers.filter(p => p.teamId === this.currentTeamId).length;
    return total > 0 ? Math.round((this.getCollectedCount() / total) * 100) : 0;
  }


  public onPackClick(): void {
    if (this.packPhase !== 'idle') return;
    if (this.packCount <= 0) {
      this.packError = 'No tienes sobres disponibles. ¡Compra más!';
      return;
    }

    this.packError = '';
    this.packPhase = 'loading';

    this.albumService.openPack().subscribe({
      next: (response) => {
        this.newStickers = response.data;
        this.currentCardIndex = 0;
        this.mergeNewStickersIntoInventory(response.data);
        this.packPhase = 'revealed';
        this.setPacks(); // ← agrega esto
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.setPacks(); // ← agrega esto (en lugar de packCount++)
        const msg = err?.error?.message?.includes('sobres disponibles')
          ? 'No tienes sobres disponibles. ¡Compra más!'
          : 'No se pudo abrir el sobre. Intenta de nuevo.';
        this.packError = msg;
        this.packPhase = 'idle';
        this.cdr.detectChanges();
      }
    });
  }


  private mergeNewStickersIntoInventory(newItems: UserStickerResponse[]): void {
    for (const item of newItems) {
      const existing = this.globalInventory.find(i => i.stickerId === item.stickerId);
      if (existing) {
        existing.quantity = item.quantity; // el servidor devuelve la cantidad total actualizada
      } else {
        this.globalInventory.push({ ...item });
      }
    }
  }

  public resetPack(): void {
    this.packPhase = 'idle';
    this.newStickers = [];
    this.currentCardIndex = 0;
    this.packError = '';
  }

  public nextCard(): void {
    if (this.currentCardIndex < this.newStickers.length - 1) this.currentCardIndex++;
  }

  public prevCard(): void {
    if (this.currentCardIndex > 0) this.currentCardIndex--;
  }



  public buyPack(): void {
    this.albumService.buyPack().subscribe({
      next: (data) => {
        if (data.success) {
          this.setPacks();
          this.authService.cargarDatosIniciales();
        }
      },
      error: (err) => console.error('Error comprando sobre:', err)
    });
  }
}
