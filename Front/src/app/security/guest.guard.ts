import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthService} from './AuthService';
import {Observable} from 'rxjs';
import {map, take} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class GuestGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean> {
    return this.authService.isAuthenticated$.pipe(
      take(1),
      map((isAuth) => {
        if (!isAuth) return true; // sin sesión → puede ver login/register/verify

        const role = this.authService.getRole()?.replace('ROLE_', '');
        if (role === 'TEMPORAL') return true; // TEMPORAL puede ver verify

        // Ya autenticado como USER → redirigir al home
        this.router.navigate(['/home']);
        return false;
      })
    );
  }
}
