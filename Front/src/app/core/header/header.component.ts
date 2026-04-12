import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../../security/AuthService'; // Ajusta la ruta si es necesario
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone:false,
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {

  public isLoggedIn: boolean = false;
  public userRole: string | null = null;
  public userName: string | null = null;
  private authSubscription!: Subscription;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // 🔥 Nos suscribimos a tu BehaviorSubject para cambios en tiempo real
    this.authSubscription = this.authService.isAuthenticated$.subscribe(autenticado => {
      this.isLoggedIn = autenticado;

      if (this.isLoggedIn) {
        // Obtenemos el rol (ej: "ROLE_ADMIN") y le quitamos el prefijo igual que en tu Guard
        let rawRole = this.authService.getRole();
        this.userRole = rawRole ? rawRole.replace('ROLE_', '') : null;

        // Obtenemos el nombre usando tu método
        this.userName = this.authService.getUserName();
      } else {
        this.userRole = null;
        this.userName = null;
      }
    });
  }

  ngOnDestroy(): void {
    // Evitamos fugas de memoria limpiando la suscripción si el componente se destruye
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  logout(): void {
    // Tu método logout() ya limpia el token, hace el navigate y actualiza el BehaviorSubject.
    // ¡Así que con solo llamar a esto, el header se actualizará solo!
    this.authService.logout();
  }
}
