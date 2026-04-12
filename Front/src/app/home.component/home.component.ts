import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AuthService } from '../security/AuthService';
import { HomeComponentService } from './home.component.service';
import { RegistroService } from '../registro.component/registro.service';

export interface Team {
  name: string;
  crest: string;
  shortName: string;
  fifaRanking: string;
  groupName: string;
}

@Component({
  selector: 'app-home.component',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {

  constructor(private authService: AuthService,
              private homeService: HomeComponentService,
              private registroService: RegistroService,
              private cdr: ChangeDetectorRef) {
  }

  avatarUrls: string[] = [
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/2_hyyqec.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/4_cylwgo.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/1_hgwg6q.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/6_mnllri.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536871/9_hyeyqu.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536871/7_vckmsj.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/3_edl267.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536871/10_s4uxo8.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536870/5_zvitrg.png',
    'https://res.cloudinary.com/dnrzgy60o/image/upload/v1775536871/8_p1wx2e.png'
  ];

  public isLoading: boolean = true;
  public isModalOpen: boolean = false;
  public filteredTeams: any[] = [];
  public groups: string[] = ['Todos', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L'];
  public selectedGroup: string = 'Todos';

  public userRole: 'admin' | 'user' = 'user';
  public userName: string = '';
  public name: string = '';
  public email: string = '';
  public oldPassword: string = '';
  public newPassword: string = '';
  imageUrl: string = this.avatarUrls[0];
  public showOldPass: boolean = false;
  public showNewPass: boolean = false;

  public country: string = '';
  public city: string = '';
  public countries: string[] = [];
  public cities: string[] = [];
  public teams: Team[] = [];
  public allMatches: any[] = [];

  public isStadiumModalOpen: boolean = false;
  public isBracketModalOpen: boolean = false;

  // 🔥 NUEVAS VARIABLES PARA EL FIXTURE COMPLETO 🔥
  public isFixtureModalOpen: boolean = false;
  public fixtureGroups: { name: string, matches: any[] }[] = [];
  public fixtureKnockout: { name: string, matches: any[] }[] = [];

  public selectedStadium: any = null;
  public isDropdownOpen: boolean = false;
  public selectedTeam: Team | null = null;
  public viewingTeam: any = null;

  public menuActivo: string = 'dashboard';
  public activeBanner: number = 0;
  public banners = [
    { title: 'Copa Mundial 2026', sub: '48 equipos buscando la gloria.', img: 'assets/img/banner1.jpg' },
    { title: 'Sedes Confirmadas', sub: 'México, Canadá y EE.UU. listos.', img: 'assets/img/banner2.jpg' },
    { title: '¡Haz tu Polla!', sub: 'Compite con amigos y gana premios.', img: 'assets/img/banner3.jpg' }
  ];

  public stadiums = [
    { name: 'Estadio Azteca', city: 'Ciudad de México', capacity: '87,523', image: 'https://hotelesemporio.com/wp-content/uploads/2025/11/Estadio.jpg' },
    { name: 'Estadio Akron', city: 'Zapopan', capacity: '46,000', image: 'https://a2.espncdn.com/combiner/i?img=%2Fphoto%2F2025%2F0523%2Fr1497228_1296x729_16%2D9.jpg' },
    { name: 'Estadio BBVA', city: 'Guadalupe', capacity: '51,000', image: 'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/30/57/a8/85/el-estadio-bbva-casa.jpg?w=900&h=-1&s=1' },
    { name: 'BMO Field', city: 'Toronto', capacity: '45,000', image: 'https://static2.gensler.com/uploads/image/65575/project_BMO_large_01_1475515240.jpg' },
    { name: 'Estadio BC Place', city: 'Vancouver', capacity: '54,000', image: 'https://media.istockphoto.com/id/2148603554/es/foto/estadio-bc-place-en-vancouver.jpg?s=612x612&w=0&k=20&c=VVNdgwe-QYbCI8mpuYBWmE0IJbA_oQCuFBF7QZAfxxI=' },
    { name: 'MetLife Stadium', city: 'Nueva York/Nueva Jersey', capacity: '82,500', image: 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Metlife_stadium_%28Aerial_view%29.jpg/960px-Metlife_stadium_%28Aerial_view%29.jpg' },
    { name: 'AT&T Stadium', city: 'Arlington (Dallas)', capacity: '94,000', image: 'https://t4.ftcdn.net/jpg/05/49/05/97/360_F_549059713_s0ntRZTHF1yB2vkJHYbzOKR8ZbsOLJNf.jpg' },
    { name: 'Mercedes-Benz Stadium', city: 'Atlanta', capacity: '71,000', image: 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/Mercedes_Benz_Stadium_time_lapse_capture_2017-08-13.jpg/1280px-Mercedes_Benz_Stadium_time_lapse_capture_2017-08-13.jpg' },
    { name: 'SoFi Stadium', city: 'Inglewood (Los Ángeles)', capacity: '70,000', image: 'https://media-cdn.tripadvisor.com/media/attractions-splice-spp-674x446/12/37/0e/47.jpg' },
    { name: 'Hard Rock Stadium', city: 'Miami Gardens', capacity: '64,767', image: 'https://images.squarespace-cdn.com/content/v1/636134e7ed4537480ff09129/22451e28-6af1-41e6-8416-9dc05949b66f/1.png?format=2500w' },
    { name: 'Arrowhead Stadium', city: 'Kansas City', capacity: '76,416', image: 'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/10/de/7d/ac/photo5jpg.jpg?w=900&h=-1&s=1' },
    { name: 'NRG Stadium', city: 'Houston', capacity: '72,220', image: 'https://www.electricchoice.com/blog/nrg-stadium-energy/nrg-stadium-energy-featured.webp' },
    { name: 'Lincoln Financial Field', city: 'Filadelfia', capacity: '69,796', image: 'https://mckissack.com/wp-content/uploads/2022/12/IMG_0214_HDR-1030x687.jpg' },
    { name: 'Lumen Field', city: 'Seattle', capacity: '69,000', image: 'https://stadium.org/wp-content/uploads/2022/12/LumenFieldAerialB500.webp' },
    { name: 'Levi´s Stadium', city: 'Santa Clara (San Francisco)', capacity: '68,500', image: 'https://levisstadium.com/wp-content/uploads/2018/02/121717_LEVISSTADIUM_TW-800x400.jpg' },
    { name: 'Gillette Stadium', city: 'Foxborough (Boston)', capacity: '65,878', image: 'https://static.elcanaldelfutbol.com/static/news/Mundial-2026_TYTw0qz.large.jpg' },
  ];

  get upcomingMatches() {
    if (!this.allMatches || this.allMatches.length === 0) return [];
    return [...this.allMatches]
      .sort((a, b) => new Date(a.utcDate).getTime() - new Date(b.utcDate).getTime())
      .slice(0, 4);
  }

  setBanner(index: number) {
    this.activeBanner = index;
  }

  selectAvatar(url: string) {
    this.imageUrl = url;
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  cambiarMenu(menu: string): void {
    if (this.menuActivo === menu) {
      this.menuActivo = 'dashboard';
    } else {
      this.menuActivo = menu;
    }
  }

  get top10Teams() {
    if (!this.teams || this.teams.length === 0) return [];

    return [...this.teams]
      .filter(t => t.fifaRanking)
      .sort((a, b) => Number(a.fifaRanking) - Number(b.fifaRanking))
      .slice(0, 10);
  }

  openStadiumDetails(stadium: any) {
    this.selectedStadium = stadium;
    this.isStadiumModalOpen = true;
  }

  closeStadiumModal() {
    this.isStadiumModalOpen = false;
    this.selectedStadium = null;
  }

  openBracketModal() {
    this.isBracketModalOpen = true;
  }

  closeBracketModal() {
    this.isBracketModalOpen = false;
  }

  // 🔥 NUEVA LÓGICA DEL FIXTURE 🔥
  openFixtureModal() {
    const groupsMap = new Map<string, any[]>();
    const knockoutMap = new Map<string, any[]>();

    const stageNames: any = {
      'LAST_32': 'Dieciseisavos de Final',
      'LAST_16': 'Octavos de Final',
      'QUARTER_FINALS': 'Cuartos de Final',
      'SEMI_FINALS': 'Semifinales',
      'THIRD_PLACE': 'Tercer Puesto',
      'FINAL': 'Final'
    };

    // Procesamos todos los partidos cargados desde la API
    this.allMatches.forEach(m => {
      if (m.stage === 'GROUP_STAGE') {
        let gName = m.group ? m.group.replace('_', ' ') : 'Grupo Desconocido';
        if (!groupsMap.has(gName)) groupsMap.set(gName, []);
        groupsMap.get(gName)!.push(m);
      } else {
        let sName = stageNames[m.stage] || m.stage;
        if (!knockoutMap.has(sName)) knockoutMap.set(sName, []);
        knockoutMap.get(sName)!.push(m);
      }
    });

    // Ordenamos Grupos alfabéticamente
    this.fixtureGroups = Array.from(groupsMap, ([name, matches]) => ({ name, matches }))
      .sort((a, b) => a.name.localeCompare(b.name));

    // Ordenamos Fase Final cronológicamente según la jerarquía de las fases
    const order = ['Dieciseisavos de Final', 'Octavos de Final', 'Cuartos de Final', 'Semifinales', 'Tercer Puesto', 'Final'];
    this.fixtureKnockout = Array.from(knockoutMap, ([name, matches]) => ({ name, matches }))
      .sort((a, b) => order.indexOf(a.name) - order.indexOf(b.name));

    this.isFixtureModalOpen = true;
  }

  closeFixtureModal() {
    this.isFixtureModalOpen = false;
  }

  logout() {
    this.authService.logout();
  }

  deleteAccount() {
    let id: number | null = this.authService.getUserId();
    this.homeService.deleteAccount(id).subscribe({
      next: res => {
        alert('cuenta eliminada con exito');
        this.logout();
      },
      error: err => {
        console.log(err);
      }
    });
  }

  getUser() {
    let id: number | null = this.authService.getUserId();
    this.homeService.getUserById(id).subscribe({
      next: res => {
        this.email = res.email;
        this.name = res.name;
        this.country = res.country;
        this.city = res.city;
        this.imageUrl = res.avatarUrl;

        if (res.favoriteTeam && this.teams && this.teams.length > 0) {
          const equipoEncontrado = this.teams.find(t => t.name === res.favoriteTeam);
          this.cdr.detectChanges();
          if (equipoEncontrado) {
            this.selectedTeam = equipoEncontrado;
          } else {
            this.selectedTeam = { name: res.favoriteTeam, crest: '', shortName: '',fifaRanking:'',groupName:'' };
          }
        }
        this.onCountryChange(this.country);
        this.cdr.detectChanges();
        this.isLoading = false;
      },
      error: err => {
        this.isLoading = false;
        console.log(err);
      }
    });
  }

  closeModal() {
    this.isModalOpen = false;
    this.viewingTeam = null;
    this.cdr.detectChanges()
  }

  filterByGroup(group: string) {
    this.selectedGroup = group;

    if (group === 'Todos') {
      this.filteredTeams = [...this.teams];
    } else {
      this.filteredTeams = this.teams.filter(t => {
        if (!t.groupName) return false;
        return String(t.groupName).trim() === String(group).trim();
      });
    }
  }

  openTeamDetails(team: any) {
    this.viewingTeam = team;

    if (this.allMatches && this.allMatches.length > 0) {
      const teamMatches = this.allMatches.filter(m =>
        m.homeTeam?.tla === team.shortName || m.awayTeam?.tla === team.shortName
      );

      this.viewingTeam.matches = teamMatches.map(m => {
        const isHome = m.homeTeam?.tla === team.shortName;

        return {
          date: m.utcDate,
          rival: isHome ? m.awayTeam?.name : m.homeTeam?.name,
          rivalCrest: isHome ? m.awayTeam?.crest : m.homeTeam?.crest,
          phase: this.formatStage(m.stage, m.matchday)
        };
      });

    } else {
      this.viewingTeam.matches = [];
    }

    this.isModalOpen = true;
  }

  formatStage(stage: string, matchday: number): string {
    if (stage === 'GROUP_STAGE') return `Fase de Grupos - J${matchday}`;
    if (stage === 'LAST_32') return 'Dieciseisavos de Final';
    if (stage === 'LAST_16') return 'Octavos de Final';
    if (stage === 'QUARTER_FINALS') return 'Cuartos de Final';
    if (stage === 'SEMI_FINALS') return 'Semifinales';
    if (stage === 'FINAL') return 'Final';
    return stage || 'Amistoso';
  }

  selectTeam(team: Team) {
    this.selectedTeam = team;
    this.isDropdownOpen = false;
  }

  loadCountries() {
    this.registroService.getCountries().subscribe(res => {
      this.countries = res;
    });
  }

  onCountryChange(country: string) {
    if (!country) return;

    this.cities = [];

    this.registroService.getCitiesByCountry(country)
      .subscribe(cities => {
        this.cities = cities;
        this.cdr.detectChanges();
      });
  }

  loadTeams() {
    this.registroService.getTeams().subscribe({
      next: res => {
        this.teams = res;
        this.filterByGroup('Todos');
        this.getUser();
        this.loadMatches();
        console.log("Equipos cargados:", res);
      },
      error: err => {
        this.isLoading = false;
      }
    });
  }

  loadMatches() {
    this.homeService.getMatches().subscribe({
      next: (res: any[]) => {
        this.allMatches = res;
      },
      error: err => console.log('Error cargando partidos:', err)
    });
  }

  updatePassword() {
    let id: number | null = this.authService.getUserId();
    const body = {
      oldPassword: this.oldPassword,
      newPassword: this.newPassword
    };

    this.homeService.updatePassword(body, id).subscribe({
      next: res => {
        console.log(res);
      },
      error: err => {
        console.log(err);
      }
    });
  }

  updateUser() {
    let id: number | null = this.authService.getUserId();
    const body = {
      userName: this.userName,
      name: this.name,
      email: this.email,
      country: this.country,
      city: this.city,
      favoriteTeam: this.selectedTeam ? this.selectedTeam.name : null,
      avatarUrl: this.imageUrl
    };
    this.homeService.updateUser(body, id).subscribe({
      next: res => {
        console.log(res);
        if (res.token) {
          this.authService.saveToken(res.token);
        }
      },
      error: err => {
        console.log(err);
      }
    });
  }

  ngOnInit(): void {
    this.isLoading = true;
    this.loadTeams();
    this.loadCountries();
    this.userName = this.authService.getUserName() || 'Usuario';
  }
}
