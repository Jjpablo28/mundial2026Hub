import { Injectable } from '@angular/core';
import {
  CanActivate,
  Router,
  ActivatedRouteSnapshot,
  RouterStateSnapshot
} from '@angular/router';
import { AuthService } from './AuthService';
import { Observable } from 'rxjs';
import { take, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private enrutador: Router
  ) {}

  canActivate(
    ruta: ActivatedRouteSnapshot,
    estado: RouterStateSnapshot
  ): Observable<boolean> {

    return this.authService.isAuthenticated$.pipe(
      take(1),
      map((estaAutenticado) => {

        const urlActual = estado.url;

        if (!estaAutenticado) {
          console.warn('No autenticado → redirect');
          this.enrutador.navigate(['/forbidden'], { queryParams: { returnUrl: urlActual } });
          return false;
        }

        let rolUsuario = this.authService.getRole();

        if (rolUsuario) {
          rolUsuario = rolUsuario.replace('ROLE_', '');
        }

        console.log('ROL USUARIO:', rolUsuario);

        const rolesPermitidos = ruta.data['rolesPermitidos'] as Array<string>;

        if (!rolesPermitidos || rolesPermitidos.length === 0) {
          return true;
        }

        console.log('ROLES PERMITIDOS:', rolesPermitidos);

        if (rolUsuario && rolesPermitidos.includes(rolUsuario)) {
          return true;
        }

        console.warn('Acceso denegado → 403');
        this.enrutador.navigate(['/forbidden']);
        return false;
      })
    );
  }
}
