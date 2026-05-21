import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { AuthService } from './security/AuthService';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css'
})
export class App implements OnInit{
  // Iniciamos en false, pero lo encenderemos inmediatamente si hay token
  public isAppLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    // Si el usuario ya está logueado (tiene token)
    if (this.authService.hasToken()) {
      this.isAppLoading = true;
      this.cdr.detectChanges(); // Forzamos a que aparezca el loader negro

      try {
        // Esperamos a que el servicio termine la carga total (Usuario + Equipos + Países)
        await this.authService.cargarDatosIniciales();
      } finally {
        // Solo cuando cargarDatosIniciales termine, apagamos el loader
        this.isAppLoading = false;
        this.cdr.detectChanges(); // Forzamos a Angular a ocultar el loader y mostrar la app
      }
    }
  }
}
