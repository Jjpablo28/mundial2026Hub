import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { AuthService } from './AuthService';


@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {
  }


  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();
    let authRequest = request;

    if (token) {
      authRequest = request.clone({
        headers: request.headers.set('Authorization', `Bearer ${token}`)
      });
    }

    return next.handle(authRequest).pipe(
      catchError((error) => {
        // Si el servidor responde 401 (No autorizado) o 403 (Prohibido)
        if (error.status === 401 || error.status === 403) {
          console.warn('Sesión expirada o inválida. Cerrando sesión...');
          this.authService.logout(); // Esto limpia el storage y redirige al login
        }
        return throwError(() => error);
      })
    );
  }
}
