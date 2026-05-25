import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { HomeComponentService } from '../home.component/home.component.service';

@Component({
  selector: 'app-match',
  standalone: false,
  templateUrl: './match.component.html',
  styleUrls: ['./match.component.scss']
})
export class MatchComponent implements OnInit, OnDestroy {

  // Variables para la lista (Columna Izquierda)
  public partidos: any[] = [];
  public partidosFiltrados: any[] = [];
  public searchTerm: string = '';
  public selectedPhase: string = 'TODAS';
  public fasesDisponibles: string[] = [];

  // Variables de la tabla de posiciones (Columna Derecha)
  public grupoDelPartido: any = null;
  public isLoadingStandings: boolean = false;

  // Variables para el detalle (Columna Derecha)
  public match: any = null;
  public isLoading: boolean = false;
  private subscription: Subscription = new Subscription();

  // Pestañas
  public activeTab: string = 'Partido';
  public tabs: string[] = ['Partido', 'Alineaciones', 'Estadísticas',  'Jugadores'];

  constructor(
    private homeService: HomeComponentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // 1. Cargar la lista de todos los partidos al iniciar la pantalla
    this.cargarPartidos();

    // 2. Escuchar cuando el usuario hace clic en una tarjeta de la lista
    this.subscription = this.homeService.partidoSeleccionado$.subscribe(id => {
      if (id) {
        this.loadMatchDetails(id);
      } else {
        this.match = null;
        this.grupoDelPartido = null;
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarPartidos() {
    this.homeService.getMatches().subscribe({
      next: (res) => {
        this.partidos = res;
        this.extraerFases();
        this.aplicarFiltros();

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando la lista de partidos', err);
        this.isLoading = false;
      }
    });
  }

  extraerFases() {
    const fasesSet = new Set(this.partidos.map(p => p.stage).filter(f => f));
    this.fasesDisponibles = Array.from(fasesSet) as string[];
  }

  aplicarFiltros() {
    this.partidosFiltrados = this.partidos.filter(p => {
      const nombreLocal = (p.homeTeam?.name || 'Por definir').toLowerCase();
      const nombreVisita = (p.awayTeam?.name || 'Por definir').toLowerCase();
      const busqueda = this.searchTerm.toLowerCase();

      const coincideBusqueda = nombreLocal.includes(busqueda) || nombreVisita.includes(busqueda);
      const coincideFase = this.selectedPhase === 'TODAS' || p.stage === this.selectedPhase;

      return coincideBusqueda && coincideFase;
    });
  }

  onSearchChange(event: any) {
    this.searchTerm = event.target.value;
    this.aplicarFiltros();
  }

  onPhaseChange(event: any) {
    this.selectedPhase = event.target.value;
    this.aplicarFiltros();
  }

  traducirFase(fase: string): string {
    const traducciones: any = {
      'GROUP_STAGE': 'Fase de Grupos',
      'LAST_16': 'Octavos de Final',
      'LAST_32': 'Dieciseisavos de Final',
      'QUARTER_FINALS': 'Cuartos de Final',
      'SEMI_FINALS': 'Semifinales',
      'THIRD_PLACE': 'Tercer Puesto',
      'FINAL': 'Final'
    };
    return traducciones[fase] || fase;
  }

  verDetalles(partido: any) {
    if (partido && partido.id) {
      this.homeService.seleccionarPartido(partido.id);
    } else {
      alert("⚠️ Error: El partido no tiene ID.");
    }
  }

  loadMatchDetails(id: number | string) {
    if (!id) return;

    this.isLoading = true;
    this.match = null;
    this.grupoDelPartido = null; // Limpiamos la tabla del partido anterior
    this.activeTab = 'Partido';  // Reseteamos a la pestaña principal
    this.cdr.detectChanges();

    this.homeService.getMatchById(id).subscribe({
      next: (res) => {
        this.match = {
          ...res,
          group: res.group,
          homeTeam: {
            name: res?.homeTeam?.shortName || res?.homeTeam?.name || 'Local',
            crest: res?.homeTeam?.crest,
            country: res?.homeTeam?.tla
          },
          awayTeam: {
            name: res?.awayTeam?.shortName || res?.awayTeam?.name || 'Visitante',
            crest: res?.awayTeam?.crest,
            country: res?.awayTeam?.tla
          }
        };

        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        this.match = null;
        this.cdr.detectChanges();
      }
    });
  }

  selectTab(tab: string) {
    this.activeTab = tab;

    // Si entra a Estadísticas y aún no hemos cargado la tabla de este partido, la descargamos
    if (tab === 'Estadísticas' && !this.grupoDelPartido) {
      this.cargarGrupos();
    }
  }

  cargarGrupos() {
    // Verificamos si el partido tiene grupo o si la fase es de grupos
    if (!this.match || (!this.match.group && this.match.stage !== 'GROUP_STAGE')) {
      this.grupoDelPartido = null;
      return;
    }

    this.isLoadingStandings = true;
    this.homeService.getStandings().subscribe({
      next: (res) => {
        // Buscamos el grupo comparando nombres de forma limpia
        const encontrado = res.find(s =>
          s.group === this.match.group ||
          s.group.replace(' ', '_').toUpperCase() === this.match.group
        );

        if (encontrado) {
          this.grupoDelPartido = {
            ...encontrado,
            group: encontrado.group.replace('Group', 'Grupo')
          };
        }

        this.isLoadingStandings = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando posiciones', err);
        this.isLoadingStandings = false;
        this.cdr.detectChanges();
      }
    });
  }

  onClose() {
    this.homeService.seleccionarPartido(null);
  }

  ngOnDestroy(): void {
    if (this.subscription) this.subscription.unsubscribe();
  }
}
