import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../../security/AuthService'; // Ajusta la ruta si es necesario
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnDestroy {

  public isLoggedIn: boolean = false;
  public userRole: string | null = null;
  public userName: string | null = null;
  public userPoints: number = 0; // NUEVO: Atributo para los puntos

  private authSubscription!: Subscription;
  private pointsSubscription!: Subscription; // NUEVO: Suscripción de puntos

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Nos suscribimos a tu BehaviorSubject para cambios en tiempo real
    this.authSubscription = this.authService.isAuthenticated$.subscribe(autenticado => {
      this.isLoggedIn = autenticado;

      if (this.isLoggedIn) {
        let rawRole = this.authService.getRole();
        this.userRole = rawRole ? rawRole.replace('ROLE_', '') : null;
        this.userName = this.authService.getUserName();
      } else {
        this.userRole = null;
        this.userName = null;
      }
    });

    // NUEVO: Escucha activa de los puntos desde el AuthService
    if (this.authService.totalPoints$) {
      this.pointsSubscription = this.authService.totalPoints$.subscribe(points => {
        this.userPoints = points;
      });
    }
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
    if (this.pointsSubscription) {
      this.pointsSubscription.unsubscribe();
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
