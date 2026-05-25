import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AlbumService, Player, UserStickerResponse} from './album.service';
import {AuthService} from '../security/AuthService';
import {HomeComponentService} from '../home.component/home.component.service';

type View = 'packs' | 'album';
type PackPhase = 'idle' | 'shaking' | 'opening' | 'revealed';

@Component({
  selector: 'app-album',
  standalone: false,
  templateUrl: './album.html',
  styleUrls: ['./album.scss']
})
export class Album implements OnInit {

  public currentView: View = 'packs';

  // ===== ÁLBUM =====
  public currentTeamId: number = 22;

  // ✅ Inventario global: se carga UNA vez al iniciar, cubre todos los equipos
  private globalInventory: UserStickerResponse[] = [];
  public isLoadingInventory: boolean = false;

  // ===== SOBRES =====
  public packCount: number = 0;
  public packPhase: PackPhase = 'idle';
  public newStickers: UserStickerResponse[] = [];
  public currentCardIndex: number = 0;
  public packError: string = '';


  public teamIds: number[] = [];
  private teamNamesMap: Record<number, string> = {};


  public allTeamPlayers: Player[] = [];
  private stickerMap: Map<number, Player> = new Map();

  constructor(private albumService: AlbumService,
              private authService: AuthService,
              private cdr: ChangeDetectorRef,
              private homeService: HomeComponentService) {
  }


  // ===========================
  // INVENTARIO GLOBAL
  // ===========================

  private loadAllStickers(): void {
    this.albumService.getAllStickers().subscribe({
      next: (players) => {
        this.allTeamPlayers = players;
        this.stickerMap = new Map(players.map(p => [p.id, p]));

        // ✅ Extrae teamIds únicos desde la API (sin hardcodear)
        this.teamIds = [...new Set(players.map(p => p.teamId))].sort((a, b) => a - b);
      },
      error: (err) => console.error('Error cargando stickers:', err)
    });
  }

  private loadTeams(): void {
    this.albumService.getAllTeams().subscribe({
      next: (teams) => {
        this.teamIds = teams.map(t => t.id);
        teams.forEach(t => {
          this.teamNamesMap[t.id] = t.name;
        });
      },
      error: (err) => console.error('Error cargando equipos:', err)
    });
  }

  ngOnInit(): void {
    this.loadAllStickers();
    this.loadGlobalInventory();
    this.loadTeams();
    this.setPacks()// ← agrega esta línea
  }

// Helper para enriquecer un sticker del sobre con su Player data
  public getStickerPlayer(stickerId: number): Player | undefined {
    return this.stickerMap.get(stickerId);
  }

  private loadGlobalInventory(): void {
    this.isLoadingInventory = true;
    this.albumService.getAllUserStickers().subscribe({
      next: (data) => {
        this.globalInventory = data;
        this.isLoadingInventory = false;
      },
      error: (err) => {
        console.error('Error cargando inventario global:', err);
        this.isLoadingInventory = false;
      }
    });
  }

  // ===========================
  // ÁLBUM
  // ===========================

  // ✅ Ya no hace HTTP — solo cambia el equipo activo y filtra el master list
  public loadTeamStickers(teamId: number): void {
    this.currentTeamId = teamId;
    this.currentView = 'album';
  }

  public setPacks() {
    let id: number | null = this.authService.getUserId();

    this.homeService.getUserById(id).subscribe({
      next: (data) => {
        this.packCount = data.packets;
        this.cdr.detectChanges();

        console.log(this.packCount)
      },
      error: (err) => {

      }
    })
  }

  // ✅ Busca en el inventario global (cubre todos los equipos)
  public getStickerQuantity(playerStickerId: number): number {
    const record = this.globalInventory.find(item => item.stickerId === playerStickerId);
    return record ? record.quantity : 0;
  }

  public getPlayersByPosition(position: string): Player[] {
    return this.allTeamPlayers.filter(p => p.position === position && p.teamId === this.currentTeamId);
  }

  public getTeamName(teamId: number): string {
    return this.teamNamesMap[teamId] ?? `Equipo ${teamId}`;
  }

  public getCollectedCount(): number {
    return this.allTeamPlayers
      .filter(p => p.teamId === this.currentTeamId && this.getStickerQuantity(p.id) > 0)
      .length;
  }

  public getProgressPercent(): number {
    const total = this.allTeamPlayers.filter(p => p.teamId === this.currentTeamId).length;
    return total > 0 ? Math.round((this.getCollectedCount() / total) * 100) : 0;
  }

  // ===========================
  // SOBRES
  // ===========================

  public onPackClick(): void {
    if (this.packPhase !== 'idle') return;
    if (this.packCount <= 0) {
      this.packError = 'No tienes sobres disponibles. ¡Compra más!';
      return;
    }
    this.packError = '';
    this.packPhase = 'shaking';

    setTimeout(() => {
      this.packPhase = 'opening';
      this.albumService.openPack().subscribe({
        next: (response) => {
          this.newStickers = response.data;
          this.currentCardIndex = 0;
          this.packCount = Math.max(0, this.packCount - 1);

          // ✅ Refrescar inventario global para que el álbum refleje los nuevos cromos
          this.loadGlobalInventory();

          setTimeout(() => {
            this.packPhase = 'revealed';
          }, 700);
        },
        error: (err) => {
          console.error('Error abriendo sobre:', err);
          this.packError = 'No se pudo abrir el sobre. Intenta de nuevo.';
          this.packPhase = 'idle';
        }
      });
    }, 600);
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
          // ✅ Refresca desde el servidor en vez de confiar en data.cantity
          this.setPacks();
          this.authService.cargarDatosIniciales();
        }
      },
      error: (err) => {
        console.error('Error comprando sobre:', err);
      }
    });
  }
}
