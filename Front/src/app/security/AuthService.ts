import { Injectable, Injector } from '@angular/core';
import { BehaviorSubject, firstValueFrom, Observable } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { HomeComponentService } from '../home.component/home.component.service';
import { RegistroService } from '../registro.component/registro.service';

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
  authorities?: AuthorityPayload[];
  exp?: number;
  iat?: number;
  id?: number;
  email?: string;
  name?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(
    private router: Router,
    private http: HttpClient,
    private injector: Injector
  ) {  }

  private userData = new BehaviorSubject<any>(null);
  public user$ = this.userData.asObservable();

  private teamsData = new BehaviorSubject<any[]>([]);
  public teams$ = this.teamsData.asObservable();

  private countriesData = new BehaviorSubject<string[]>([]);
  public countries$ = this.countriesData.asObservable();

  private totalPointsSubject = new BehaviorSubject<number>(0);
  public totalPoints$: Observable<number> = this.totalPointsSubject.asObservable();

  // 🔥 NUEVO: Control de navegación global por pestañas para la SPA
  private menuActivoSubject = new BehaviorSubject<string>('dashboard');
  public menuActivo$ = this.menuActivoSubject.asObservable();

  cambiarMenuGlobal(menu: string): void {
    const actual = this.menuActivoSubject.getValue();
    if (actual === menu) {
      this.menuActivoSubject.next('dashboard');
    } else {
      this.menuActivoSubject.next(menu);
    }
  }

  actualizarEstadoUsuario(nuevosDatos: any) {
    const usuarioActual = this.userData.getValue();
    if (usuarioActual) {
      const usuarioActualizado = { ...usuarioActual, ...nuevosDatos };
      this.userData.next(usuarioActualizado);
    }
  }

  async cargarDatosIniciales(): Promise<void> {
    const id = this.getUserId();
    const homeService = this.injector.get(HomeComponentService);
    const registroService = this.injector.get(RegistroService);

    try {
      const [user, teams, countries] = await Promise.all([
        id ? firstValueFrom(homeService.getUserById(id)) : Promise.resolve(null),
        firstValueFrom(registroService.getTeams()),
        firstValueFrom(registroService.getCountries())
      ]);

      if (user) {
        this.userData.next(user);
        if (user.totalPoints !== undefined) {
          this.totalPointsSubject.next(user.totalPoints);
        }
      }
      if (teams) {
        this.teamsData.next(teams);
        localStorage.setItem('mundial_teams', JSON.stringify(teams));
      }
      if (countries) {
        this.countriesData.next(countries);
        localStorage.setItem('mundial_countries', JSON.stringify(countries));
      }

      console.log('✅ Todos los datos cargados correctamente.');

    } catch (error) {
      console.error("Error crítico cargando datos iniciales", error);
    }
  }

  getTeamsValue() { return this.teamsData.getValue(); }
  getCountriesValue() { return this.countriesData.getValue(); }

  getUserValue() { return this.userData.getValue(); }

  private avatarSource = new BehaviorSubject<string | null>(null);
  public avatar$ = this.avatarSource.asObservable();

  actualizarAvatar(url: string) { this.avatarSource.next(url); }

  private equipoSource = new BehaviorSubject<any>(null);
  public equipo$ = this.equipoSource.asObservable();

  actualizarEquipo(equipo: any) { this.equipoSource.next(equipo); }

  private tokenKey = 'authToken';

  isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  createAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return token ? new HttpHeaders({ 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }) : new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  redirectError(errorId: any) {
    const rutaActual = this.router.url;

    // Rutas públicas que no deben disparar redirecciones
    const rutasPublicas = ['/', '/login', '/register', '/verify', '/forbidden'];
    const esRutaPublica = rutasPublicas.some(r => rutaActual.startsWith(r));

    if (esRutaPublica) return;

    if (errorId === 403) this.router.navigate(['/forbidden']);
    if (errorId === 401) this.router.navigate(['/login']);
  }

  logout(): void {
    this.removeToken();
    localStorage.clear();
    this.router.navigate(['/']);
    this.isAuthenticatedSubject.next(false);
    this.menuActivoSubject.next('dashboard'); // Resetea el menú al salir
  }

  saveToken(token: string): void { localStorage.setItem(this.tokenKey, token); }
  getToken(): string | null { return localStorage.getItem(this.tokenKey); }
  removeToken(): void { localStorage.removeItem(this.tokenKey); }

  hasToken(): boolean {
    const token = this.getToken();
    if (!token) return false;
    const payload = this.getDecodedToken();
    if (!payload || !payload.exp) return false;
    return payload.exp > Math.floor(Date.now() / 1000);
  }

  getRole(): string | null {
    const payload = this.getDecodedToken();
    return (payload && payload.authorities && payload.authorities.length > 0) ? payload.authorities[0].authority : null;
  }

  getDecodedToken(): JwtTokenPayload | null {
    const token = this.getToken();
    if (token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')));
      } catch { return null; }
    }
    return null;
  }

  getUserId(): number | null { const payload = this.getDecodedToken(); return payload && payload.id ? payload.id : null; }
  getUserName(): string | null { const payload = this.getDecodedToken(); return payload && payload.user ? payload.user : null; }

  setTotalPoints(points: number) {
    this.totalPointsSubject.next(points);
    const usuarioActual = this.userData.getValue();
    if (usuarioActual) {
      usuarioActual.totalPoints = points;
      this.userData.next(usuarioActual);
    }
  }

}
