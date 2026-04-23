import { Injectable, Injector } from '@angular/core';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
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
  authorities?: AuthorityPayload[]; // <-- Así viene en el token
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

  actualizarEstadoUsuario(nuevosDatos: any) {
    const usuarioActual = this.userData.getValue();
    if (usuarioActual) {
      // Fusionamos los datos viejos con los nuevos
      const usuarioActualizado = { ...usuarioActual, ...nuevosDatos };
      this.userData.next(usuarioActualizado);
    }
  }

  async cargarDatosIniciales(): Promise<void> {
    const id = this.getUserId();
    const homeService = this.injector.get(HomeComponentService);
    const registroService = this.injector.get(RegistroService);

    try {
      // 🚀 Usamos Promise.all con 'await' para que la función NO termine
      // hasta que las tres peticiones hayan respondido con éxito.
      const [user, teams, countries] = await Promise.all([
        id ? firstValueFrom(homeService.getUserById(id)) : Promise.resolve(null),
        firstValueFrom(registroService.getTeams()),
        firstValueFrom(registroService.getCountries())
      ]);

      if (user) this.userData.next(user);
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
      // Opcional: Si falla la carga de datos críticos, puedes forzar logout
      // this.logout();
    }
  }

  // 👇 NUEVO MÉTODO: Maneja la caché inteligente
  private async cargarCatalogosEstaticos(registroService: any): Promise<void> {
    // Revisamos si ya los tenemos guardados en la memoria del navegador
    const cachedTeams = localStorage.getItem('mundial_teams');
    const cachedCountries = localStorage.getItem('mundial_countries');

    if (cachedTeams && cachedCountries) {
      // Si ya existen, los cargamos instantáneamente (Toma 0.001 segundos)
      this.teamsData.next(JSON.parse(cachedTeams));
      this.countriesData.next(JSON.parse(cachedCountries));
      console.log('⚡ Equipos y países cargados desde la caché local');
    } else {
      // Si es la primera vez que entra, los descargamos de la base de datos
      try {
        const [teams, countries] = await Promise.all([
          firstValueFrom(registroService.getTeams()),
          firstValueFrom(registroService.getCountries())
        ]);

        // Actualizamos las variables para que el HTML las pueda usar
        if (teams) this.teamsData.next(teams as any[]);
        if (countries) this.countriesData.next(countries as string[]);
        // Los guardamos en localStorage para futuras visitas
        localStorage.setItem('mundial_teams', JSON.stringify(teams));
        localStorage.setItem('mundial_countries', JSON.stringify(countries));
        console.log('☁️ Equipos y países descargados y guardados en caché');

      } catch (error) {
        console.error("Error descargando los catálogos estáticos", error);
      }
    }
  }

  // Actualiza tu método logout() para limpiar la caché si lo deseas

  getTeamsValue() { return this.teamsData.getValue(); }
  getCountriesValue() { return this.countriesData.getValue(); }


  getUserValue() {
    return this.userData.getValue();
  }

  private avatarSource = new BehaviorSubject<string | null>(null);
  public avatar$ = this.avatarSource.asObservable();

  actualizarAvatar(url: string) {
    this.avatarSource.next(url);
  }

  private equipoSource = new BehaviorSubject<any>(null);
  public equipo$ = this.equipoSource.asObservable();

  actualizarEquipo(equipo: any) {
    this.equipoSource.next(equipo);
  }

  private tokenKey = 'authToken';

  isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

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

  redirectError(errorId:any){
    if(errorId==403){
      this.router.navigate(['/forbidden']);
    }
    if (errorId==401){
      this.router.navigate(['/login']);
    }
  }

  logout(): void {
    this.removeToken();
    localStorage.clear(); // Esto borrará los tokens y nuestra nueva caché
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

  getUserId(): number | null {
    const payload = this.getDecodedToken();
    return payload && payload.id ? payload.id : null;
  }

  getUserName(): string | null {
    const payload = this.getDecodedToken();
    return payload && payload.user ? payload.user : null;
  }

  getUserEmail(): string | null {
    const payload = this.getDecodedToken();
    return payload && payload.email ? payload.email : null;
  }
}
