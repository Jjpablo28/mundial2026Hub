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
      // 🚀 Ejecutamos todas las peticiones en paralelo para ganar velocidad
      const [user, teams, countries] = await Promise.all([
        id ? firstValueFrom(homeService.getUserById(id)) : Promise.resolve(null),
        firstValueFrom(registroService.getTeams()),
        firstValueFrom(registroService.getCountries())
      ]);

      if (user) this.userData.next(user);
      if (teams) this.teamsData.next(teams);
      if (countries) this.countriesData.next(countries);

    } catch (error) {
      console.error("Error cargando datos de inicio", error);
    }
  }
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
    localStorage.clear();
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
